package com.example.pocketbrain.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.example.pocketbrain.R
import com.example.pocketbrain.models.Transaction
import com.example.pocketbrain.utils.CurrencyUtils
import com.example.pocketbrain.utils.DateUtils

class TransactionAdapter(
    private val context: Context,
    private var transactions: List<Transaction>,
    private val currencyCode: String,
    private val onItemClick: (Transaction) -> Unit
) : RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val titleTextView: TextView = view.findViewById(R.id.textTransactionTitle)
        val amountTextView: TextView = view.findViewById(R.id.textTransactionAmount)
        val categoryTextView: TextView = view.findViewById(R.id.textTransactionCategory)
        val dateTextView: TextView = view.findViewById(R.id.textTransactionDate)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val transaction = transactions[position]

        holder.titleTextView.text = transaction.title

        // Format amount with currency and color (red for expense, green for income)
        val formattedAmount = CurrencyUtils.formatAmount(transaction.amount, currencyCode)
        holder.amountTextView.text = if (transaction.isExpense) "-$formattedAmount" else formattedAmount

        val textColor = if (transaction.isExpense)
            ContextCompat.getColor(context, R.color.expense_color)
        else
            ContextCompat.getColor(context, R.color.income_color)

        holder.amountTextView.setTextColor(textColor)

        holder.categoryTextView.text = transaction.category
        holder.dateTextView.text = DateUtils.formatDate(transaction.date)

        // Set click listener
        holder.itemView.setOnClickListener {
            onItemClick(transaction)
        }
    }

    override fun getItemCount() = transactions.size

    fun updateData(newTransactions: List<Transaction>) {
        this.transactions = newTransactions
        notifyDataSetChanged()
    }
}