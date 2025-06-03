package com.example.myapplication

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter(
    private val expenseList: MutableList<ExpenseItem>,
    private val onDeleteClick: (Int) -> Unit,
    private val onEditClick: (Int) -> Unit,
    private val onItemClick: (Int) -> Unit,
    private val languageManager: LanguageManager
) : RecyclerView.Adapter<ExpenseAdapter.ExpenseViewHolder>() {
    
    private lateinit var currencyManager: CurrencyManager
    
    // Function to convert 24-hour format (HH:mm) to 12-hour format with AM/PM
    private fun convertTo12HourFormat(time24: String): String {
        return try {
            val inputFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            val outputFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
            val date = inputFormat.parse(time24)
            if (date != null) {
                outputFormat.format(date)
            } else {
                time24 // Return original if parsing fails
            }
        } catch (e: Exception) {
            time24 // Return original if conversion fails
        }
    }
    
    class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
        val textViewDateTimeHeader: TextView = itemView.findViewById(R.id.textViewDateTimeHeader)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        val textViewDateTime: TextView = itemView.findViewById(R.id.textViewDateTime)
        val buttonEdit: Button = itemView.findViewById(R.id.buttonEdit)
        val buttonDelete: Button = itemView.findViewById(R.id.buttonDelete)
        val layoutDetails: LinearLayout = itemView.findViewById(R.id.layoutDetails)
        val layoutButtons: LinearLayout = itemView.findViewById(R.id.layoutButtons)
    }    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        
        // Initialize CurrencyManager if not already done
        if (!::currencyManager.isInitialized) {
            currencyManager = CurrencyManager.getInstance(parent.context)
        }
        
        return ExpenseViewHolder(view)
    }    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val expenseItem = expenseList[position]
        holder.textViewName.text = expenseItem.name
          // Use CurrencyManager's new method for display
        val displayAmount = currencyManager.getDisplayAmountFromStored(expenseItem.price, expenseItem.currency)
        holder.textViewPrice.text = currencyManager.formatCurrency(displayAmount)
        
        // Convert time to 12-hour format with AM/PM
        val formattedTime = convertTo12HourFormat(expenseItem.time)
        
        // Set date and time in header (always visible)
        holder.textViewDateTimeHeader.text = "📅 ${expenseItem.date} • 🕐 $formattedTime"
          holder.textViewDescription.text = if (expenseItem.description.isNotEmpty()) {
            expenseItem.description
        } else {
            languageManager.getString("no_description")
        }
        
        holder.textViewDateTime.text = "📅 ${expenseItem.date} • 🕐 $formattedTime"
        
        // Set button text using language manager
        holder.buttonEdit.text = languageManager.getString("edit_button")
        holder.buttonDelete.text = languageManager.getString("delete_button")
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
    }    override fun getItemCount(): Int = expenseList.size
    
    fun refreshTranslations() {
        notifyDataSetChanged()
    }
}
