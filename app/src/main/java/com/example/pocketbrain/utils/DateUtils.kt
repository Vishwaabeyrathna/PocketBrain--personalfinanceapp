package com.example.pocketbrain.utils

import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

/**
 * Utility class for date formatting and manipulation
 */
object DateUtils {
    private val displayFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
    private val dbFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    private val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())

    fun formatDate(date: Date): String {
        return displayFormat.format(date)
    }

    fun parseDate(dateStr: String): Date? {
        return try {
            dbFormat.parse(dateStr)
        } catch (e: Exception) {
            null
        }
    }

    fun getCurrentMonth(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.MONTH)
    }

    fun getCurrentYear(): Int {
        val calendar = Calendar.getInstance()
        return calendar.get(Calendar.YEAR)
    }

    fun getMonthYearString(month: Int, year: Int): String {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)
        return monthYearFormat.format(calendar.time)
    }

    fun getPreviousMonth(month: Int, year: Int): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)
        calendar.add(Calendar.MONTH, -1)

        return Pair(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))
    }

    fun getNextMonth(month: Int, year: Int): Pair<Int, Int> {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)
        calendar.add(Calendar.MONTH, 1)

        return Pair(calendar.get(Calendar.MONTH), calendar.get(Calendar.YEAR))
    }

    fun getStartOfMonth(month: Int, year: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.DAY_OF_MONTH, 1)
        calendar.set(Calendar.HOUR_OF_DAY, 0)
        calendar.set(Calendar.MINUTE, 0)
        calendar.set(Calendar.SECOND, 0)
        calendar.set(Calendar.MILLISECOND, 0)

        return calendar.time
    }

    fun getEndOfMonth(month: Int, year: Int): Date {
        val calendar = Calendar.getInstance()
        calendar.set(Calendar.MONTH, month)
        calendar.set(Calendar.YEAR, year)
        calendar.set(Calendar.DAY_OF_MONTH, calendar.getActualMaximum(Calendar.DAY_OF_MONTH))
        calendar.set(Calendar.HOUR_OF_DAY, 23)
        calendar.set(Calendar.MINUTE, 59)
        calendar.set(Calendar.SECOND, 59)
        calendar.set(Calendar.MILLISECOND, 999)

        return calendar.time
    }
}