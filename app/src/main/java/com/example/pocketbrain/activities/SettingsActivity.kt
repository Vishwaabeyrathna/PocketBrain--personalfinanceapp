package com.example.pocketbrain.activities

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.FileProvider
import com.example.pocketbrain.BuildConfig // Explicit import
import com.example.pocketbrain.R
import com.example.pocketbrain.databinding.ActivitySettingsBinding
import com.example.pocketbrain.utils.DataManager
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

        // Access version name with BuildConfig
        val versionName = try {
            BuildConfig.VERSION_NAME // Uses manual or auto-generated BuildConfig
        } catch (e: Exception) {
            "1.0" // Fallback if BuildConfig fails
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
            importLauncher.launch("*/*")
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
            val file = dataManager.exportData()
            val authority = "${applicationContext.packageName}.fileprovider"
            val uri = FileProvider.getUriForFile(this, authority, file)

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
            Toast.makeText(this, getString(R.string.backup_failed), Toast.LENGTH_SHORT).show()
        }
    }

    private fun importData(uri: Uri) {
        try {
            val inputStream = contentResolver.openInputStream(uri)
            val tempFile = File(cacheDir, "import_data.json")

            inputStream?.use { input ->
                tempFile.outputStream().use { output ->
                    input.copyTo(output)
                }
            }

            val success = dataManager.importData(tempFile)

            val message = if (success) R.string.data_restored else R.string.restore_failed
            Toast.makeText(this, getString(message), Toast.LENGTH_SHORT).show()

            tempFile.delete()
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, getString(R.string.restore_failed), Toast.LENGTH_SHORT).show()
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == android.R.id.home) {
            onBackPressedDispatcher.onBackPressed()
            true
        } else super.onOptionsItemSelected(item)
    }
}