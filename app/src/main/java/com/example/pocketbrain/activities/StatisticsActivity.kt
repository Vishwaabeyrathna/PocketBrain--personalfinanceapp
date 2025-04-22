package com.example.pocketbrain.activities

import android.graphics.Color
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.pocketbrain.R
import com.example.pocketbrain.adapters.CategoryAdapter
import com.example.pocketbrain.databinding.FragmentStatisticsBinding
import com.example.pocketbrain.models.Category
import com.example.pocketbrain.utils.CurrencyUtils
import com.example.pocketbrain.utils.DataManager
import com.example.pocketbrain.utils.DateUtils
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.formatter.PercentFormatter

class StatisticsActivity : AppCompatActivity() {

    private lateinit var binding: FragmentStatisticsBinding
    private lateinit var dataManager: DataManager
    private lateinit var categoryAdapter: CategoryAdapter

    private var currentMonth = DateUtils.getCurrentMonth()
    private var currentYear = DateUtils.getCurrentYear()
    private var currencyCode = "USD"

    private var categories: List<Category> = emptyList()
    private var categorySpendings: Map<String, Double> = emptyMap()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = FragmentStatisticsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Set the custom toolbar as the action bar
        setSupportActionBar(binding.toolbar)
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "Statistics"

        dataManager = DataManager.getInstance(this)
        currencyCode = dataManager.getCurrency()
        categories = dataManager.getCategories().filter { it.isExpense }

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
        binding.recyclerCategories.layoutManager = LinearLayoutManager(this)
        categoryAdapter = CategoryAdapter(
            this,
            categories,
            categorySpendings,
            currencyCode
        ) { category ->
            // Category click listener - could show transactions for this category
        }
        binding.recyclerCategories.adapter = categoryAdapter

        // Setup pie chart
        setupPieChart()

        updateUI()
    }

    private fun setupPieChart() {
        binding.pieChart.apply {
            description.isEnabled = false
            isDrawHoleEnabled = true
            setHoleColor(Color.WHITE)
            holeRadius = 58f
            setDrawEntryLabels(false)
            setEntryLabelTextSize(12f)
            setEntryLabelColor(Color.BLACK)
            legend.isEnabled = true
            legend.textSize = 12f
            legend.textColor = Color.BLACK
        }
    }

    private fun updateUI() {
        // Update month display
        binding.textCurrentMonth.text = DateUtils.getMonthYearString(currentMonth, currentYear)

        // Get category spending data
        categorySpendings = dataManager.getCategorySpending(currentMonth, currentYear)

        // Update category list
        categoryAdapter.updateData(categories, categorySpendings)

        // Show/hide empty state
        binding.textNoCategories.visibility = if (categorySpendings.isEmpty()) View.VISIBLE else View.GONE

        // Update pie chart
        updatePieChart()
    }

    private fun updatePieChart() {
        if (categorySpendings.isEmpty()) {
            binding.pieChart.visibility = View.GONE
            binding.textNoDataForChart.visibility = View.VISIBLE
            return
        }

        binding.pieChart.visibility = View.VISIBLE
        binding.textNoDataForChart.visibility = View.GONE

        val entries = ArrayList<PieEntry>()
        val colors = ArrayList<Int>()

        // Create entries for each category
        val sortedCategories = categorySpendings.entries.sortedByDescending { it.value }

        // Show top categories and group small ones as "Other"
        val topCategories = sortedCategories.take(5)
        val otherCategories = sortedCategories.drop(5)

        for ((categoryName, amount) in topCategories) {
            entries.add(PieEntry(amount.toFloat(), categoryName))

            // Find the category color
            val category = categories.find { it.name == categoryName }
            val color = category?.color ?: Color.GRAY
            colors.add(color)
        }

        // Add "Other" category if needed
        if (otherCategories.isNotEmpty()) {
            val otherAmount = otherCategories.sumOf { it.value }
            entries.add(PieEntry(otherAmount.toFloat(), "Other"))
            colors.add(Color.GRAY)
        }

        val dataSet = PieDataSet(entries, "Categories")
        dataSet.colors = colors
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.valueTextSize = 12f
        dataSet.valueTextColor = Color.WHITE

        val pieData = PieData(dataSet)
        pieData.setValueFormatter(PercentFormatter(binding.pieChart))

        binding.pieChart.data = pieData
        binding.pieChart.invalidate()
        binding.pieChart.animateY(1400, Easing.EaseInOutQuad)
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