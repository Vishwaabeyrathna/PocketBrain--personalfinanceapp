package com.example.pocketbrain.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketbrain.R
import com.example.pocketbrain.models.Category
import com.example.pocketbrain.utils.CurrencyUtils

class CategoryAdapter(
    private val context: Context,
    private var categories: List<Category>,
    private var categorySpendings: Map<String, Double>,
    private val currencyCode: String,
    private val onItemClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.textCategoryName)
        val amountTextView: TextView = view.findViewById(R.id.textCategoryAmount)
        val colorView: View = view.findViewById(R.id.viewCategoryColor)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = categories[position]

        holder.nameTextView.text = category.name

        // Set category color
        holder.colorView.setBackgroundColor(category.color)

        // Show spending amount if available
        val spending = categorySpendings[category.name] ?: 0.0
        holder.amountTextView.text = CurrencyUtils.formatAmount(spending, currencyCode)

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(category)
        }
    }

    override fun getItemCount() = categories.size

    fun updateData(newCategories: List<Category>, newCategorySpendings: Map<String, Double>) {
        this.categories = newCategories
        this.categorySpendings = newCategorySpendings
        notifyDataSetChanged()
    }
}