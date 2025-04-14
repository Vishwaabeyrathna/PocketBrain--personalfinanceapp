package com.example.pocketbrain.activities

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketbrain.R
import com.example.pocketbrain.databinding.ActivityAddEditTransactionBinding
import com.example.pocketbrain.models.Category
import com.example.pocketbrain.models.Transaction
import com.example.pocketbrain.utils.DataManager
import com.example.pocketbrain.utils.DateUtils
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog
import java.util.Calendar
import java.util.Date

class AddEditTransactionActivity : AppCompatActivity(), DatePickerDialog.OnDateSetListener {

    private lateinit var binding: ActivityAddEditTransactionBinding
    private lateinit var dataManager: DataManager

    private var isEdit = false
    private var currentTransaction: Transaction? = null
    private var selectedDate = Date()
    private var isExpense = true

    private var categories: List<Category> = emptyList()
    private var expenseCategories: List<Category> = emptyList()
    private var incomeCategories: List<Category> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityAddEditTransactionBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dataManager = DataManager.getInstance(this)

        // Initialize categories
        categories = dataManager.getCategories()
        expenseCategories = categories.filter { it.isExpense }
        incomeCategories = categories.filter { !it.isExpense }

        // Check if we're editing an existing transaction
        isEdit = intent.getBooleanExtra("isEdit", false)
        if (isEdit) {
            currentTransaction = intent.getSerializableExtra("transaction") as? Transaction

            if (currentTransaction != null) {
                setupEditMode(currentTransaction!!)
            } else {
                Toast.makeText(this, "Error loading transaction", Toast.LENGTH_SHORT).show()
                finish()
                return
            }
        } else {
            setupAddMode()
        }

        // Set up listeners
        setupListeners()
    }

    private fun setupAddMode() {
        supportActionBar?.title = getString(R.string.add_transaction)
        binding.radioExpense.isChecked = true
        isExpense = true
        selectedDate = Date() // Today
        binding.editDate.setText(DateUtils.formatDate(selectedDate))
        updateCategoryDropdown()
    }

    private fun setupEditMode(transaction: Transaction) {
        supportActionBar?.title = getString(R.string.edit_transaction)

        // Populate fields
        binding.editTitle.setText(transaction.title)
        binding.editAmount.setText(transaction.amount.toString())
        binding.editDate.setText(DateUtils.formatDate(transaction.date))
        binding.editNotes.setText(transaction.notes)

        // Set transaction type
        isExpense = transaction.isExpense
        binding.radioExpense.isChecked = isExpense
        binding.radioIncome.isChecked = !isExpense

        // Set date
        selectedDate = transaction.date

        // Update category dropdown based on transaction type
        updateCategoryDropdown()

        // Select the correct category
        val categoryAdapter = binding.spinnerCategory.adapter as ArrayAdapter<String>
        val position = (0 until categoryAdapter.count).firstOrNull {
            categoryAdapter.getItem(it) == transaction.category
        } ?: 0
        binding.spinnerCategory.setText(transaction.category, false)
    }

    private fun setupListeners() {
        // Date picker
        binding.editDate.setOnClickListener {
            showDatePicker()
        }

        // Transaction type toggle
        binding.radioGroupTransactionType.setOnCheckedChangeListener { _, checkedId ->
            isExpense = checkedId == R.id.radioExpense
            updateCategoryDropdown()
        }

        // Save button
        binding.buttonSave.setOnClickListener {
            saveTransaction()
        }
    }

    private fun updateCategoryDropdown() {
        val categoryList = if (isExpense) expenseCategories else incomeCategories
        val categoryNames = categoryList.map { it.name }

        val adapter = ArrayAdapter(this, R.layout.item_dropdown, categoryNames)
        binding.spinnerCategory.setAdapter(adapter)
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        calendar.time = selectedDate

        val datePickerDialog = DatePickerDialog.newInstance(
            this,
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )

        datePickerDialog.show(supportFragmentManager, "DatePickerDialog")
    }

    override fun onDateSet(view: DatePickerDialog?, year: Int, monthOfYear: Int, dayOfMonth: Int) {
        val calendar = Calendar.getInstance()
        calendar.set(year, monthOfYear, dayOfMonth)
        selectedDate = calendar.time
        binding.editDate.setText(DateUtils.formatDate(selectedDate))
    }

    private fun saveTransaction() {
        // Validate input
        val title = binding.editTitle.text.toString().trim()
        if (title.isEmpty()) {
            binding.editTitle.error = getString(R.string.title_required)
            return
        }

        val amountStr = binding.editAmount.text.toString().trim()
        if (amountStr.isEmpty()) {
            binding.editAmount.error = getString(R.string.invalid_amount)
            return
        }

        val amount = try {
            amountStr.toDouble()
        } catch (e: Exception) {
            binding.editAmount.error = getString(R.string.invalid_amount)
            return
        }

        if (amount <= 0) {
            binding.editAmount.error = getString(R.string.invalid_amount)
            return
        }

        val category = binding.spinnerCategory.text.toString()
        if (category.isEmpty()) {
            Toast.makeText(this, getString(R.string.category_required), Toast.LENGTH_SHORT).show()
            return
        }

        val notes = binding.editNotes.text.toString().trim()

        // Create or update transaction
        if (isEdit && currentTransaction != null) {
            // Update existing transaction
            currentTransaction?.apply {
                this.title = title
                this.amount = amount
                this.category = category
                this.date = selectedDate
                this.isExpense = this@AddEditTransactionActivity.isExpense
                this.notes = notes
            }

            dataManager.saveTransaction(currentTransaction!!)
            Toast.makeText(this, "Transaction updated", Toast.LENGTH_SHORT).show()
        } else {
            // Create new transaction
            val newTransaction = Transaction(
                title = title,
                amount = amount,
                category = category,
                date = selectedDate,
                isExpense = isExpense,
                notes = notes
            )

            dataManager.saveTransaction(newTransaction)
            Toast.makeText(this, "Transaction added", Toast.LENGTH_SHORT).show()
        }

        setResult(Activity.RESULT_OK)
        finish()
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                onBackPressed()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onBackPressed() {
        // Show confirmation dialog if there are unsaved changes
        if (hasUnsavedChanges()) {
            AlertDialog.Builder(this)
                .setTitle("Discard Changes")
                .setMessage("You have unsaved changes. Are you sure you want to discard them?")
                .setPositiveButton("Discard") { _, _ -> super.onBackPressed() }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            super.onBackPressed()
        }
    }

    private fun hasUnsavedChanges(): Boolean {
        // If we're in edit mode, check if any fields have changed
        if (isEdit && currentTransaction != null) {
            val title = binding.editTitle.text.toString().trim()
            val amountStr = binding.editAmount.text.toString().trim()
            val amount = try { amountStr.toDouble() } catch (e: Exception) { 0.0 }
            val category = binding.spinnerCategory.text.toString()
            val notes = binding.editNotes.text.toString().trim()

            return title != currentTransaction?.title ||
                    amount != currentTransaction?.amount ||
                    category != currentTransaction?.category ||
                    notes != currentTransaction?.notes ||
                    isExpense != currentTransaction?.isExpense ||
                    selectedDate.time != currentTransaction?.date?.time
        }

        // In add mode, check if any fields have been filled
        val title = binding.editTitle.text.toString().trim()
        val amount = binding.editAmount.text.toString().trim()
        val notes = binding.editNotes.text.toString().trim()

        return title.isNotEmpty() || amount.isNotEmpty() || notes.isNotEmpty()
    }
}