package com.example.pocketbrain.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.pocketbrain.R
import com.example.pocketbrain.activities.MainActivity

/**
 * Helper class to manage notifications for budget alerts
 */
class NotificationHelper(private val context: Context) {

    companion object {
        private const val CHANNEL_ID = "pocketbrain_channel"
        private const val BUDGET_WARNING_NOTIFICATION_ID = 1
        private const val BUDGET_EXCEEDED_NOTIFICATION_ID = 2
        private const val EXPENSE_REMINDER_NOTIFICATION_ID = 3
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "PocketBrain Notifications"
            val descriptionText = "Notifications for budget alerts and reminders"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
            }

            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showBudgetWarningNotification(remainingBudget: Double, currency: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Budget Warning")
            .setContentText("You're approaching your monthly budget limit. Remaining: $currency$remainingBudget")
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(BUDGET_WARNING_NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                // Handle the case when notification permission is not granted
                e.printStackTrace()
            }
        }
    }

    fun showBudgetExceededNotification(overBudgetAmount: Double, currency: String) {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Budget Exceeded")
            .setContentText("You've exceeded your monthly budget by $currency$overBudgetAmount")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(BUDGET_EXCEEDED_NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                // Handle the case when notification permission is not granted
                e.printStackTrace()
            }
        }
    }

    fun showExpenseReminderNotification() {
        val intent = Intent(context, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            context, 0, intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle("Expense Reminder")
            .setContentText("Don't forget to record your daily expenses in PocketBrain")
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        with(NotificationManagerCompat.from(context)) {
            try {
                notify(EXPENSE_REMINDER_NOTIFICATION_ID, notification)
            } catch (e: SecurityException) {
                // Handle the case when notification permission is not granted
                e.printStackTrace()
            }
        }
    }
}