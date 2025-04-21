package com.example.pocketbrain.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.pocketbrain.BuildConfig
import com.example.pocketbrain.R
import com.example.pocketbrain.databinding.ActivitySettingsBinding
import com.example.pocketbrain.utils.DataManager
import com.google.gson.Gson
import java.io.File

class SettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySettingsBinding
    private lateinit var dataManager: DataManager

    private val importLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let { importData(it) }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivitySettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dataManager = DataManager.getInstance(this)

        val versionName = try {
            BuildConfig.VERSION_NAME
        } catch (e: Exception) {
            "1.0"
        }

        binding.textAppVersion.text = getString(R.string.version, versionName)
        binding.textCurrentCurrency.text = dataManager.getCurrency()

        binding.layoutBudgetSettings.setOnClickListener {
            startActivity(Intent(this, BudgetSettingsActivity::class.java))
        }

        binding.layoutCurrencySettings.setOnClickListener {
            showCurrencySelector()
        }

        binding.layoutExportData.setOnClickListener {
            exportData()
        }

        binding.layoutImportData.setOnClickListener {
            Toast.makeText(
                this,
                "Select a PocketBrain backup file (.json) from Downloads or other folders",
                Toast.LENGTH_LONG
            ).show()
            importLauncher.launch("application/json")
        }
    }

    private fun showCurrencySelector() {
        val currencies = com.example.pocketbrain.utils.CurrencyUtils.getAvailableCurrencies()
        val currentCurrency = dataManager.getCurrency()
        val currentIndex = currencies.indexOf(currentCurrency)

        AlertDialog.Builder(this)
            .setTitle("Select Currency")
            .setSingleChoiceItems(currencies.toTypedArray(), currentIndex) { dialog, which ->
                val selectedCurrency = currencies[which]
                dataManager.setCurrency(selectedCurrency)
                binding.textCurrentCurrency.text = selectedCurrency
                dialog.dismiss()
                Toast.makeText(this, "Currency updated to $selectedCurrency", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun exportData() {
        try {
            // Create a file in the cache directory
            val calendar = java.util.Calendar.getInstance()
            val fileName = "pocketbrain_backup_${calendar.timeInMillis}.json"
            val file = File(cacheDir, fileName)
            Log.d("SettingsActivity", "Export file path: ${file.absolutePath}")

            // Export data using DataManager
            val exportedFile = dataManager.exportData(file)
            if (!exportedFile.exists()) {
                throw Exception("Failed to create backup file")
            }

            // Share the file via FileProvider
            val authority = "${packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(this, authority, exportedFile)
            Log.d("SettingsActivity", "File URI: $uri")

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/json"
                putExtra(Intent.EXTRA_SUBJECT, "PocketBrain Backup")
                putExtra(Intent.EXTRA_TEXT, "Backup of your PocketBrain data is attached.")
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Share Backup File"))
            Toast.makeText(this, getString(R.string.backup_created), Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("SettingsActivity", "Export failed: ${e.message}", e)
            Toast.makeText(this, "Backup failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    private fun importData(uri: Uri) {
        try {
            // Verify the file is a JSON file
            val mimeType = contentResolver.getType(uri)
            if (mimeType != "application/json") {
                Toast.makeText(this, "Please select a JSON file", Toast.LENGTH_SHORT).show()
                return
            }

            // Create a temporary file in cache
            val tempFile = File(cacheDir, "import_data.json")
            contentResolver.openInputStream(uri)?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            } ?: throw Exception("Failed to open input stream")

            // Import data
            val success = dataManager.importData(tempFile)
            val message = if (success) R.string.data_restored else R.string.restore_failed
            Toast.makeText(this, getString(message), Toast.LENGTH_SHORT).show()

            // Clean up
            tempFile.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            Log.e("SettingsActivity", "Import failed: ${e.message}", e)
            Toast.makeText(this, "Import failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            true
        } else super.onOptionsItemSelected(item)
    }
}