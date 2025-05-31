package com.example.myapplication

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class CurrencyExchangeAdapter(
    private val expensesList: List<ExpenseItem>,
    private val currencyManager: CurrencyManager
) : RecyclerView.Adapter<CurrencyExchangeAdapter.CurrencyExchangeViewHolder>() {
    
    class CurrencyExchangeViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textExpenseName: TextView = itemView.findViewById(R.id.textExpenseName)
        val textOriginalAmount: TextView = itemView.findViewById(R.id.textOriginalAmount)
        val textConvertedAmount: TextView = itemView.findViewById(R.id.textConvertedAmount)
        val textExpenseDate: TextView = itemView.findViewById(R.id.textExpenseDate)
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CurrencyExchangeViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_currency_exchange, parent, false)
        return CurrencyExchangeViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: CurrencyExchangeViewHolder, position: Int) {
        val expense = expensesList[position]
        val currentCurrency = currencyManager.getCurrentCurrency()
        
        holder.textExpenseName.text = expense.name
        holder.textExpenseDate.text = "${expense.date} • ${expense.time}"
        
        // Original amount (always stored in USD)
        holder.textOriginalAmount.text = "Original: $${String.format("%.2f", expense.price)} USD"
        
        // Converted amount based on current currency selection
        when (currentCurrency) {
            CurrencyManager.CURRENCY_USD -> {
                holder.textConvertedAmount.text = "Display: $${String.format("%.2f", expense.price)} USD"
                holder.textConvertedAmount.setTextColor(holder.itemView.context.getColor(android.R.color.holo_blue_dark))
            }
            CurrencyManager.CURRENCY_MMK -> {
                val convertedAmount = currencyManager.convertFromUsd(expense.price)
                holder.textConvertedAmount.text = "Display: ${String.format("%.2f", convertedAmount)} MMK"
                holder.textConvertedAmount.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
            }
        }
    }
    
    override fun getItemCount(): Int = expensesList.size
}
