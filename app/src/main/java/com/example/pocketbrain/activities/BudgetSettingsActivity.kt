package com.example.pocketbrain.activities

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.pocketbrain.R
import com.example.pocketbrain.databinding.ActivityBudgetSettingsBinding
import com.example.pocketbrain.models.Budget
import com.example.pocketbrain.notifications.BudgetNotificationReceiver
import com.example.pocketbrain.notifications.NotificationHelper
import com.example.pocketbrain.utils.CurrencyUtils
import com.example.pocketbrain.utils.DataManager
import com.example.pocketbrain.utils.DateUtils
import java.util.Calendar

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

        // Load notification preferences
        loadNotificationPreferences()

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

    private fun loadNotificationPreferences() {
        val (enableNotifications, enableReminder) = dataManager.getNotificationPreferences()
        binding.switchEnableNotifications.isChecked = enableNotifications
        binding.switchExpenseReminder.isChecked = enableReminder
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

        // Save notification preferences
        dataManager.setNotificationPreferences(enableNotifications, enableReminder)

        // Immediate notifications for instant feedback
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
            notificationHelper.showExpenseReminderNotification()
        }

        // Schedule or cancel notifications
        if (enableNotifications) {
            scheduleBudgetCheckNotification()
        } else {
            cancelBudgetCheckNotification()
        }

        if (enableReminder) {
            scheduleDailyReminderNotification()
        } else {
            cancelDailyReminderNotification()
        }

        Toast.makeText(this, "Budget settings saved", Toast.LENGTH_SHORT).show()

        setResult(RESULT_OK)
        finish()
    }

    private fun scheduleBudgetCheckNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BudgetNotificationReceiver::class.java).apply {
            action = "com.example.pocketbrain.BUDGET_CHECK"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule daily at 8 PM
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 20) // 8 PM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelBudgetCheckNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BudgetNotificationReceiver::class.java).apply {
            action = "com.example.pocketbrain.BUDGET_CHECK"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, 0, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
    }

    private fun scheduleDailyReminderNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BudgetNotificationReceiver::class.java).apply {
            action = "com.example.pocketbrain.DAILY_REMINDER"
        }

        val pendingIntent = PendingIntent.getBroadcast(
            this, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Schedule daily at 9 AM
        val calendar = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 9) // 9 AM
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) {
                add(Calendar.DAY_OF_MONTH, 1)
            }
        }

        alarmManager.setRepeating(
            AlarmManager.RTC_WAKEUP,
            calendar.timeInMillis,
            AlarmManager.INTERVAL_DAY,
            pendingIntent
        )
    }

    private fun cancelDailyReminderNotification() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, BudgetNotificationReceiver::class.java).apply {
            action = "com.example.pocketbrain.DAILY_REMINDER"
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this, 1, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.cancel(pendingIntent)
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