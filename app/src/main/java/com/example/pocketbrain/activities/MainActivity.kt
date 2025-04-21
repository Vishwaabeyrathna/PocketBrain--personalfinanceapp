package com.example.pocketbrain.activities

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketbrain.R
import com.example.pocketbrain.adapters.TransactionAdapter
import com.example.pocketbrain.databinding.ActivityMainBinding
import com.example.pocketbrain.models.Budget
import com.example.pocketbrain.models.Transaction
import com.example.pocketbrain.utils.CurrencyUtils
import com.example.pocketbrain.utils.DataManager
import com.example.pocketbrain.utils.DateUtils

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var dataManager: DataManager
    private lateinit var transactionAdapter: TransactionAdapter

    private var currentMonth = DateUtils.getCurrentMonth()
    private var currentYear = DateUtils.getCurrentYear()
    private var currencyCode = "USD"

    // Activity result launchers for modern API
    private val addTransactionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            updateUI()
        }
    }

    private val editTransactionLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            updateUI()
        }
    }

    private val budgetSettingsLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == RESULT_OK) {
            updateUI()
        }
    }

    private val requestNotificationPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) {
            Toast.makeText(this, "Notification permission denied. Some features may not work.", Toast.LENGTH_LONG).show()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        setSupportActionBar(binding.toolbar)

        dataManager = DataManager.getInstance(this)
        currencyCode = dataManager.getCurrency()

        // Request notification permission
        requestNotificationPermission()

        // Setup month navigation
        binding.textCurrentMonth.text = DateUtils.getMonthYearString(currentMonth, currentYear)

        binding.btnPrevMonth.setOnClickListener {
            val prev = DateUtils.getPreviousMonth(currentMonth, currentYear)
            currentMonth = prev.first
            currentYear = prev.second
            updateUI()
        }

        binding.btnNextMonth.setOnClickListener {
            val next = DateUtils.getNextMonth(currentMonth, currentYear)
            currentMonth = next.first
            currentYear = next.second
            updateUI()
        }

        // Setup RecyclerView
        binding.recyclerTransactions.layoutManager = LinearLayoutManager(this)
        transactionAdapter = TransactionAdapter(
            this,
            emptyList(),
            currencyCode
        ) { transaction ->
            // Transaction click listener
            val intent = Intent(this, AddEditTransactionActivity::class.java).apply {
                putExtra("transaction", transaction)
                putExtra("isEdit", true)
            }
            editTransactionLauncher.launch(intent)
        }
        binding.recyclerTransactions.adapter = transactionAdapter

        // Setup FAB
        binding.fabAddTransaction.setOnClickListener {
            val intent = Intent(this, AddEditTransactionActivity::class.java)
            addTransactionLauncher.launch(intent)
        }

        // Setup budget card click
        binding.cardBudget.setOnClickListener {
            val intent = Intent(this, BudgetSettingsActivity::class.java)
            budgetSettingsLauncher.launch(intent)
        }

        updateUI()
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                requestNotificationPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
    }

    override fun onResume() {
        super.onResume()
        currencyCode = dataManager.getCurrency()
        updateUI()
    }

    private fun updateUI() {
        // Update month display
        binding.textCurrentMonth.text = DateUtils.getMonthYearString(currentMonth, currentYear)

        // Get transactions for current month
        val transactions = dataManager.getMonthlyTransactions(currentMonth, currentYear)

        // Update transactions list
        transactionAdapter.updateData(transactions)  // Use updateData instead of creating new adapter

        // Show/hide empty state
        binding.textNoTransactions.visibility = if (transactions.isEmpty()) View.VISIBLE else View.GONE

        // Update summary
        val totalIncome = dataManager.getTotalIncome(currentMonth, currentYear)
        val totalExpense = dataManager.getTotalExpense(currentMonth, currentYear)
        val balance = totalIncome - totalExpense

        binding.textTotalIncome.text = CurrencyUtils.formatAmount(totalIncome, currencyCode)
        binding.textTotalExpenses.text = CurrencyUtils.formatAmount(totalExpense, currencyCode)
        binding.textBalance.text = CurrencyUtils.formatAmount(balance, currencyCode)

        // Text color for balance
        binding.textBalance.setTextColor(
            if (balance >= 0) resources.getColor(R.color.income_color, theme)
            else resources.getColor(R.color.expense_color, theme)
        )

        // Update budget info
        val budget = dataManager.getBudget()

        if (budget != null && budget.month == currentMonth && budget.year == currentYear) {
            binding.cardBudget.visibility = View.VISIBLE
            binding.textBudgetAmount.text = CurrencyUtils.formatAmount(budget.amount, currencyCode)

            val usedPercentage = if (budget.amount > 0) {
                ((totalExpense / budget.amount) * 100).toInt().coerceIn(0, 100)
            } else {
                0
            }

            binding.progressBudget.progress = usedPercentage
            binding.textBudgetStatus.text = getString(R.string.budget_status, usedPercentage)

            // Set color based on budget status
            when {
                totalExpense > budget.amount -> {
                    binding.progressBudget.progressDrawable.setTint(resources.getColor(R.color.budget_danger, theme))
                    binding.textBudgetStatus.setTextColor(resources.getColor(R.color.budget_danger, theme))
                }
                usedPercentage > 80 -> {
                    binding.progressBudget.progressDrawable.setTint(resources.getColor(R.color.budget_warning, theme))
                    binding.textBudgetStatus.setTextColor(resources.getColor(R.color.budget_warning, theme))
                }
                else -> {
                    binding.progressBudget.progressDrawable.setTint(resources.getColor(R.color.budget_safe, theme))
                    binding.textBudgetStatus.setTextColor(resources.getColor(R.color.budget_safe, theme))
                }
            }
        } else {
            // No budget set for this month
            binding.cardBudget.visibility = View.GONE
        }
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_statistics -> {
                val intent = Intent(this, StatisticsActivity::class.java)
                startActivity(intent)
                true
            }
            R.id.action_settings -> {
                val intent = Intent(this, SettingsActivity::class.java)
                startActivity(intent)
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
}