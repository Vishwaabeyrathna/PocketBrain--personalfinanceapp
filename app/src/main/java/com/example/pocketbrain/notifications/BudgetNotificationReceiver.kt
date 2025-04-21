package com.example.pocketbrain.notifications

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import com.example.pocketbrain.utils.CurrencyUtils
import com.example.pocketbrain.utils.DataManager
import com.example.pocketbrain.utils.DateUtils

class BudgetNotificationReceiver : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val notificationHelper = NotificationHelper(context)
        val dataManager = DataManager.getInstance(context)

        when (intent.action) {
            "com.example.pocketbrain.BUDGET_CHECK" -> {
                val month = DateUtils.getCurrentMonth()
                val year = DateUtils.getCurrentYear()
                val budget = dataManager.getBudget()

                if (budget != null && budget.month == month && budget.year == year) {
                    val totalExpense = dataManager.getTotalExpense(month, year)
                    val remaining = budget.amount - totalExpense
                    val currencyCode = dataManager.getCurrency()
                    val currencySymbol = CurrencyUtils.getCurrencySymbol(currencyCode)

                    if (remaining < 0) {
                        // Budget exceeded
                        notificationHelper.showBudgetExceededNotification(
                            Math.abs(remaining),
                            currencySymbol
                        )
                    } else if (remaining < (budget.amount * 0.2)) {
                        // Budget warning (less than 20% remaining)
                        notificationHelper.showBudgetWarningNotification(
                            remaining,
                            currencySymbol
                        )
                    }
                }
            }
            "com.example.pocketbrain.DAILY_REMINDER" -> {
                notificationHelper.showExpenseReminderNotification()
            }
        }
    }
}