package com.example.pocketbrain.utils

import android.content.Context
import android.content.SharedPreferences
import com.example.pocketbrain.models.Budget
import com.example.pocketbrain.models.Category
import com.example.pocketbrain.models.Transaction
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.File
import java.io.FileOutputStream
import java.io.FileReader
import java.util.Calendar
import java.util.Date

/**
 * Utility class to manage data persistence using SharedPreferences
 */
class DataManager private constructor(context: Context) {
    private val sharedPreferences: SharedPreferences
    private val gson = Gson()
    private val appContext: Context = context.applicationContext

    companion object {
        private const val PREFS_NAME = "PocketBrainPrefs"
        private const val KEY_TRANSACTIONS = "transactions"
        private const val KEY_CATEGORIES = "categories"
        private const val KEY_BUDGET = "budget"
        private const val KEY_CURRENCY = "currency"

        @Volatile
        private var instance: DataManager? = null

        fun getInstance(context: Context): DataManager {
            return instance ?: synchronized(this) {
                instance ?: DataManager(context).also { instance = it }
            }
        }
    }

    init {
        sharedPreferences = appContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

        // Initialize with default categories if none exist
        if (getCategories().isEmpty()) {
            saveCategories(getDefaultCategories())
        }
    }

    // Transaction methods
    fun saveTransaction(transaction: Transaction) {
        val transactions = getTransactions().toMutableList()

        // Check if this transaction already exists (for edits)
        val existingIndex = transactions.indexOfFirst { it.id == transaction.id }
        if (existingIndex >= 0) {
            transactions[existingIndex] = transaction
        } else {
            transactions.add(transaction)
        }

        saveTransactions(transactions)
    }

    fun deleteTransaction(transactionId: String) {
        val transactions = getTransactions().toMutableList()
        transactions.removeAll { it.id == transactionId }
        saveTransactions(transactions)
    }

    fun getTransactions(): List<Transaction> {
        val json = sharedPreferences.getString(KEY_TRANSACTIONS, "[]")
        val type = object : TypeToken<List<Transaction>>() {}.type
        return gson.fromJson(json, type) ?: listOf()
    }

    private fun saveTransactions(transactions: List<Transaction>) {
        val json = gson.toJson(transactions)
        sharedPreferences.edit().putString(KEY_TRANSACTIONS, json).apply()
    }

    // Category methods
    fun saveCategory(category: Category) {
        val categories = getCategories().toMutableList()
        val existingIndex = categories.indexOfFirst { it.id == category.id }

        if (existingIndex >= 0) {
            categories[existingIndex] = category
        } else {
            categories.add(category)
        }

        saveCategories(categories)
    }

    fun getCategories(): List<Category> {
        val json = sharedPreferences.getString(KEY_CATEGORIES, "[]")
        val type = object : TypeToken<List<Category>>() {}.type
        return gson.fromJson(json, type) ?: listOf()
    }

    private fun saveCategories(categories: List<Category>) {
        val json = gson.toJson(categories)
        sharedPreferences.edit().putString(KEY_CATEGORIES, json).apply()
    }

    private fun getDefaultCategories(): List<Category> {
        return listOf(
            Category("1", "Food", android.graphics.Color.parseColor("#FF5722")),
            Category("2", "Transport", android.graphics.Color.parseColor("#2196F3")),
            Category("3", "Bills", android.graphics.Color.parseColor("#F44336")),
            Category("4", "Entertainment", android.graphics.Color.parseColor("#9C27B0")),
            Category("5", "Shopping", android.graphics.Color.parseColor("#4CAF50")),
            Category("6", "Health", android.graphics.Color.parseColor("#FF9800")),
            Category("7", "Salary", android.graphics.Color.parseColor("#8BC34A"), false),
            Category("8", "Gifts", android.graphics.Color.parseColor("#3F51B5"), false),
            Category("9", "Other Income", android.graphics.Color.parseColor("#009688"), false)
        )
    }

    // Budget methods
    fun saveBudget(budget: Budget) {
        val json = gson.toJson(budget)
        sharedPreferences.edit().putString(KEY_BUDGET, json).apply()
    }

    fun getBudget(): Budget? {
        val json = sharedPreferences.getString(KEY_BUDGET, null) ?: return null
        return gson.fromJson(json, Budget::class.java)
    }

    // Currency methods
    fun setCurrency(currency: String) {
        sharedPreferences.edit().putString(KEY_CURRENCY, currency).apply()
    }

    fun getCurrency(): String {
        return sharedPreferences.getString(KEY_CURRENCY, "USD") ?: "USD"
    }

    // Backup methods
    fun exportData(): File {
        val calendar = Calendar.getInstance()
        val fileName = "pocketbrain_backup_${calendar.timeInMillis}.json"
        val file = File(appContext.filesDir, fileName)

        val data = mapOf(
            "transactions" to getTransactions(),
            "categories" to getCategories(),
            "budget" to getBudget(),
            "currency" to getCurrency()
        )

        val json = gson.toJson(data)
        FileOutputStream(file).use { it.write(json.toByteArray()) }

        return file
    }

    fun importData(file: File): Boolean {
        return try {
            val json = FileReader(file).readText()
            val type = object : TypeToken<Map<String, Any>>() {}.type
            val data: Map<String, Any> = gson.fromJson(json, type)

            // Save the imported data
            data["transactions"]?.let {
                val transactionsJson = gson.toJson(it)
                sharedPreferences.edit().putString(KEY_TRANSACTIONS, transactionsJson).apply()
            }

            data["categories"]?.let {
                val categoriesJson = gson.toJson(it)
                sharedPreferences.edit().putString(KEY_CATEGORIES, categoriesJson).apply()
            }

            data["budget"]?.let {
                val budgetJson = gson.toJson(it)
                sharedPreferences.edit().putString(KEY_BUDGET, budgetJson).apply()
            }

            data["currency"]?.let {
                setCurrency(it.toString())
            }

            true
        } catch (e: Exception) {
            e.printStackTrace()
            false
        }
    }

    // Analysis methods
    fun getMonthlyTransactions(month: Int, year: Int): List<Transaction> {
        val cal = Calendar.getInstance()

        return getTransactions().filter { transaction ->
            cal.time = transaction.date
            cal.get(Calendar.MONTH) == month && cal.get(Calendar.YEAR) == year
        }
    }

    fun getCategorySpending(month: Int, year: Int): Map<String, Double> {
        val transactions = getMonthlyTransactions(month, year)
            .filter { it.isExpense }

        return transactions.groupBy { it.category }
            .mapValues { entry ->
                entry.value.sumOf { it.amount }
            }
    }

    fun getTotalIncome(month: Int, year: Int): Double {
        return getMonthlyTransactions(month, year)
            .filter { !it.isExpense }
            .sumOf { it.amount }
    }

    fun getTotalExpense(month: Int, year: Int): Double {
        return getMonthlyTransactions(month, year)
            .filter { it.isExpense }
            .sumOf { it.amount }
    }

    fun isOverBudget(month: Int, year: Int): Boolean {
        val budget = getBudget() ?: return false

        // Check if budget is for the current month/year
        if (budget.month != month || budget.year != year) {
            return false
        }

        val totalExpense = getTotalExpense(month, year)
        return totalExpense > budget.amount
    }

    fun getBudgetRemainingPercentage(month: Int, year: Int): Float {
        val budget = getBudget() ?: return 0f

        // Check if budget is for the current month/year
        if (budget.month != month || budget.year != year) {
            return 0f
        }

        val totalExpense = getTotalExpense(month, year)
        val remaining = budget.amount - totalExpense

        return (remaining / budget.amount * 100).toFloat().coerceIn(0f, 100f)
    }
}