package com.example.pocketbrain.utils

import java.text.NumberFormat
import java.util.Currency
import java.util.Locale

/**
 * Utility class for currency formatting
 */
object CurrencyUtils {

    fun formatAmount(amount: Double, currencyCode: String): String {
        val locale = when (currencyCode) {
            "EUR" -> Locale.GERMANY
            "GBP" -> Locale.UK
            "JPY" -> Locale.JAPAN
            "INR" -> Locale("en", "IN")
            "LKR" -> Locale("en", "LK")  // Added Sri Lankan Rupee
            else -> Locale.US  // Default to USD
        }

        val format = NumberFormat.getCurrencyInstance(locale)
        format.currency = Currency.getInstance(currencyCode)

        return format.format(amount)
    }

    fun getAvailableCurrencies(): List<String> {
        return listOf("USD", "EUR", "GBP", "JPY", "INR", "LKR") // Added LKR
    }

    fun getCurrencySymbol(currencyCode: String): String {
        return Currency.getInstance(currencyCode).symbol
    }
}
