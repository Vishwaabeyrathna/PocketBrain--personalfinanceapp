package com.example.pocketbrain.activities

import android.app.Activity
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketbrain.R
import com.example.pocketbrain.databinding.ActivityBudgetSettingsBinding
import com.example.pocketbrain.models.Budget
import com.example.pocketbrain.notifications.NotificationHelper
import com.example.pocketbrain.utils.CurrencyUtils
import com.example.pocketbrain.utils.DataManager
import com.example.pocketbrain.utils.DateUtils

class BudgetSettingsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBudgetSettingsBinding
    private lateinit var dataManager: DataManager
    private lateinit var notificationHelper: NotificationHelper

    private var currentMonth = DateUtils.getCurrentMonth()
    private var currentYear = DateUtils.getCurrentYear()
    private var currencyCode = "USD"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityBudgetSettingsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)

        dataManager = DataManager.getInstance(this)
        notificationHelper = NotificationHelper(this)

        // Initialize currency
        currencyCode = dataManager.getCurrency()

        // Setup currency dropdown
        setupCurrencyDropdown()

        // Setup month display
        binding.textSelectedMonth.text = "Budget for ${DateUtils.getMonthYearString(currentMonth, currentYear)}"

        // Load existing budget
        loadBudget()

        // Setup save button
        binding.buttonSaveBudget.setOnClickListener {
            saveBudget()
        }
    }

    private fun setupCurrencyDropdown() {
        val currencies = CurrencyUtils.getAvailableCurrencies()
        val adapter = ArrayAdapter(this, R.layout.item_dropdown, currencies)
        binding.spinnerCurrency.setAdapter(adapter)

        // Set the current currency
        binding.spinnerCurrency.setText(currencyCode, false)
    }

    private fun loadBudget() {
        val budget = dataManager.getBudget()

        if (budget != null && budget.month == currentMonth && budget.year == currentYear) {
            binding.editBudgetAmount.setText(budget.amount.toString())
        } else {
            // No budget set for this month
            binding.editBudgetAmount.setText("")
        }
    }

    private fun saveBudget() {
        // Validate input
        val amountStr = binding.editBudgetAmount.text.toString().trim()
        if (amountStr.isEmpty()) {
            binding.editBudgetAmount.error = getString(R.string.invalid_amount)
            return
        }

        val amount = try {
            amountStr.toDouble()
        } catch (e: Exception) {
            binding.editBudgetAmount.error = getString(R.string.invalid_amount)
            return
        }

        if (amount <= 0) {
            binding.editBudgetAmount.error = getString(R.string.invalid_amount)
            return
        }

        // Save currency
        currencyCode = binding.spinnerCurrency.text.toString()
        if (currencyCode.isEmpty()) {
            currencyCode = "USD"
        }
        dataManager.setCurrency(currencyCode)

        // Save budget
        val budget = Budget(
            amount = amount,
            month = currentMonth,
            year = currentYear
        )
        dataManager.saveBudget(budget)

        // Handle notifications
        val enableNotifications = binding.switchEnableNotifications.isChecked
        val enableReminder = binding.switchExpenseReminder.isChecked

        // Here you would typically store these preferences and setup alarms
        // For simplicity, just show a test notification if enabled
        if (enableNotifications) {
            val totalExpense = dataManager.getTotalExpense(currentMonth, currentYear)
            val remaining = amount - totalExpense

            if (remaining < 0) {
                // Budget exceeded
                notificationHelper.showBudgetExceededNotification(
                    Math.abs(remaining),
                    CurrencyUtils.getCurrencySymbol(currencyCode)
                )
            } else if (remaining < (amount * 0.2)) {
                // Budget warning (less than 20% remaining)
                notificationHelper.showBudgetWarningNotification(
                    remaining,
                    CurrencyUtils.getCurrencySymbol(currencyCode)
                )
            }
        }

        if (enableReminder) {
            // In a real app, you'd schedule a daily alarm here
            notificationHelper.showExpenseReminderNotification()
        }

        Toast.makeText(this, "Budget settings saved", Toast.LENGTH_SHORT).show()

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
}