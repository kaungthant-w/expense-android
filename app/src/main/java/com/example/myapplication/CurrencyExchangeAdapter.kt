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
        
        // Show original amount in its stored currency
        holder.textOriginalAmount.text = "Original: ${currencyManager.formatCurrency(expense.price)} ${expense.currency}"
        
        // Show display amount using the new method
        val displayAmount = currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        when (currentCurrency) {
            CurrencyManager.CURRENCY_USD -> {
                holder.textConvertedAmount.text = "Display: ${currencyManager.formatCurrency(displayAmount)} USD"
                holder.textConvertedAmount.setTextColor(holder.itemView.context.getColor(android.R.color.holo_blue_dark))
            }
            CurrencyManager.CURRENCY_MMK -> {
                holder.textConvertedAmount.text = "Display: ${currencyManager.formatCurrency(displayAmount)} MMK"
                holder.textConvertedAmount.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
            }
        }
    }
    
    override fun getItemCount(): Int = expensesList.size
}
