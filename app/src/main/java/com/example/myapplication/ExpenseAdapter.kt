package com.example.myapplication

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.NumberFormat
import java.util.*

class ExpenseAdapter(
    private val expenseList: MutableList<ExpenseItem>,
    private val onDeleteClick: (Int) -> Unit,
    private val onEditClick: (Int) -> Unit,
    private val onItemClick: (Int) -> Unit
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
        val textViewDateTimeHeader: TextView = itemView.findViewById(R.id.textViewDateTimeHeader)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        val textViewDateTime: TextView = itemView.findViewById(R.id.textViewDateTime)
        val buttonEdit: Button = itemView.findViewById(R.id.buttonEdit)
        val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)
        val layoutDetails: LinearLayout = itemView.findViewById(R.id.layoutDetails)
        val layoutButtons: LinearLayout = itemView.findViewById(R.id.layoutButtons)
    }override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expenseItem = expenseList[position]
          holder.textViewName.text = expenseItem.name
        
        // Format price with currency
        val currencyFormat = NumberFormat.getCurrencyInstance(Locale.US)
        holder.textViewPrice.text = currencyFormat.format(expenseItem.price)
        
        // Set date and time in header (always visible)
        holder.textViewDateTimeHeader.text = "📅 ${expenseItem.date} • 🕐 ${expenseItem.time}"
        
        holder.textViewDescription.text = if (expenseItem.description.isNotEmpty()) {
            expenseItem.description
        } else {
            "No description"
        }
          holder.textViewDateTime.text = "📅 ${expenseItem.date} • 🕐 ${expenseItem.time}"
          // Details and buttons are hidden by default - only name and price shown
        holder.layoutDetails.visibility = View.GONE
        holder.layoutButtons.visibility = View.GONE
        
        // Set click listener for the entire item to navigate to detail activity
        holder.itemView.setOnClickListener {
            onItemClick(position)
        }
        
        holder.buttonEdit.setOnClickListener {
            onEditClick(position)
        }
        
        holder.buttonDelete.setOnClickListener {
            onDeleteClick(position)
        }
    }

    override fun getItemCount(): Int = expenseList.size
}
