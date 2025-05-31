package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : AppCompatActivity() {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val deletedExpenses = mutableListOf<ExpenseItem>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var languageManager: LanguageManager
    private val gson = Gson()
        override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        
        languageManager = LanguageManager.getInstance(this)
        setupActionBar()
        initViews()
        setupSharedPreferences()
        setupRecyclerView()
        loadDeletedExpenses()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh deleted expenses list when activity resumes
        // This ensures we always show the latest deleted data
        loadDeletedExpenses()
    }
      private fun setupActionBar() {
        supportActionBar?.title = languageManager.getString("deleted_items_history_title", "🗃️ Deleted Items History")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewHistory)
    }
      private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
    }
      private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            deletedExpenses,
            onRestoreClick = { position -> restoreExpenseItem(position) },
            onDeletePermanentlyClick = { position -> showDeletePermanentlyDialog(position) }
        )
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = historyAdapter
    }
    
    private fun loadDeletedExpenses() {
        val expensesJson = sharedPreferences.getString("expenses", "[]")
        val type = object : TypeToken<List<ExpenseItem>>() {}.type
        val allExpenses: List<ExpenseItem> = gson.fromJson(expensesJson, type) ?: emptyList()
        
        deletedExpenses.clear()
        deletedExpenses.addAll(allExpenses.filter { it.isDeleted })
        historyAdapter.notifyDataSetChanged()
    }
      private fun saveAllExpenses() {
        val expensesJson = sharedPreferences.getString("expenses", "[]")
        val type = object : TypeToken<MutableList<ExpenseItem>>() {}.type
        val allExpenses: MutableList<ExpenseItem> = gson.fromJson(expensesJson, type) ?: mutableListOf()
        
        // Update all expenses in the main list
        allExpenses.forEach { expense ->
            val deletedExpense = deletedExpenses.find { it.id == expense.id }
            if (deletedExpense != null) {
                expense.isDeleted = deletedExpense.isDeleted
                expense.deletedAt = deletedExpense.deletedAt
            }
        }
        
        val updatedExpensesJson = gson.toJson(allExpenses)
        sharedPreferences.edit().putString("expenses", updatedExpensesJson).apply()
    }
    
    // New function to save a restored expense specifically
    private fun saveRestoredExpense(restoredExpense: ExpenseItem) {
        // Load all existing expenses from storage
        val json = sharedPreferences.getString("expenses", null)
        val allExpenses = mutableListOf<ExpenseItem>()
        
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<ExpenseItem>>() {}.type
            val savedExpenses: List<ExpenseItem> = gson.fromJson(json, type)
            allExpenses.addAll(savedExpenses)
        }
        
        // Update the restored expense
        val existingIndex = allExpenses.indexOfFirst { it.id == restoredExpense.id }
        if (existingIndex != -1) {
            // Update existing expense
            allExpenses[existingIndex] = restoredExpense
        } else {
            // Add restored expense if not found (shouldn't happen, but safety check)
            allExpenses.add(restoredExpense)
        }
        
        // Save the complete list
        val editor = sharedPreferences.edit()
        val allExpensesJson = gson.toJson(allExpenses)
        editor.putString("expenses", allExpensesJson)
        editor.apply()
    }    private fun restoreExpenseItem(position: Int) {
        if (position >= 0 && position < deletedExpenses.size) {
            val expense = deletedExpenses[position]
            
            AlertDialog.Builder(this)
                .setTitle("✅ Restore Expense")
                .setMessage("Are you sure you want to restore '${expense.name}'?")
                .setPositiveButton("Restore") { _, _ ->
                    // Mark as not deleted
                    expense.isDeleted = false
                    expense.deletedAt = null
                    
                    // Remove from deleted list
                    deletedExpenses.removeAt(position)
                    historyAdapter.notifyItemRemoved(position)
                    historyAdapter.notifyItemRangeChanged(position, deletedExpenses.size)
                      // Save the restored expense specifically
                    saveRestoredExpense(expense)
                    
                    Toast.makeText(this, languageManager.getString("expense_restored_successfully", "✅ Expense restored successfully!"), Toast.LENGTH_SHORT).show()
                    
                    // Set result to notify MainActivity to refresh
                    setResult(RESULT_OK)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    
    private fun showDeletePermanentlyDialog(position: Int) {
        if (position >= 0 && position < deletedExpenses.size) {
            val expense = deletedExpenses[position]
            
            AlertDialog.Builder(this)
                .setTitle("⚠️ Delete Permanently")
                .setMessage("This will permanently delete '${expense.name}'. This action cannot be undone. Are you sure?")
                .setPositiveButton("Delete Forever") { _, _ ->
                    deletePermanently(position)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    
    private fun deletePermanently(position: Int) {
        if (position >= 0 && position < deletedExpenses.size) {
            val expense = deletedExpenses[position]
            
            // Remove from deleted list
            deletedExpenses.removeAt(position)
            historyAdapter.notifyItemRemoved(position)
            historyAdapter.notifyItemRangeChanged(position, deletedExpenses.size)
            
            // Remove from all expenses permanently            removeExpensePermanently(expense.id)
            
            Toast.makeText(this, languageManager.getString("item_permanently_deleted", "🗑️ Item permanently deleted"), Toast.LENGTH_SHORT).show()
        }
    }
    
    private fun removeExpensePermanently(expenseId: Long) {
        val expensesJson = sharedPreferences.getString("expenses", "[]")
        val type = object : TypeToken<MutableList<ExpenseItem>>() {}.type
        val allExpenses: MutableList<ExpenseItem> = gson.fromJson(expensesJson, type) ?: mutableListOf()
        
        // Remove the expense with matching ID
        allExpenses.removeAll { it.id == expenseId }
        
        // Save the updated list
        val updatedExpensesJson = gson.toJson(allExpenses)
        sharedPreferences.edit().putString("expenses", updatedExpensesJson).apply()
    }
    
    private fun applyTheme() {
        val themePrefs = getSharedPreferences(ThemeActivity.THEME_PREFS, Context.MODE_PRIVATE)
        val savedTheme = themePrefs.getString(ThemeActivity.THEME_KEY, ThemeActivity.THEME_SYSTEM)
        
        when (savedTheme) {
            ThemeActivity.THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeActivity.THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            ThemeActivity.THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}

class HistoryAdapter(
    private val deletedExpenses: MutableList<ExpenseItem>,
    private val onRestoreClick: (Int) -> Unit,
    private val onDeletePermanentlyClick: (Int) -> Unit
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private lateinit var currencyManager: CurrencyManager

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val textViewName: TextView = view.findViewById(R.id.textViewName)
        val textViewPrice: TextView = view.findViewById(R.id.textViewPrice)
        val textViewDescription: TextView = view.findViewById(R.id.textViewDescription)
        val textViewDateTime: TextView = view.findViewById(R.id.textViewDateTime)
        val textViewDeletedDate: TextView = view.findViewById(R.id.textViewDeletedDate)
        val buttonRestore: Button = view.findViewById(R.id.buttonRestore)
        val buttonDeletePermanently: Button = view.findViewById(R.id.buttonDeletePermanently)
    }    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        
        // Initialize CurrencyManager if not already done
        if (!::currencyManager.isInitialized) {
            currencyManager = CurrencyManager.getInstance(parent.context)
        }
        
        return HistoryViewHolder(view)
    }    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val expense = deletedExpenses[position]
        
        holder.textViewName.text = expense.name
        
        // Format price with currency using CurrencyManager
        val currentCurrency = currencyManager.getCurrentCurrency()
        val displayAmount = if (currentCurrency == CurrencyManager.CURRENCY_MMK) {
            currencyManager.convertFromUsd(expense.price)
        } else {
            expense.price
        }
        holder.textViewPrice.text = currencyManager.formatCurrency(displayAmount)
        
        holder.textViewDescription.text = if (expense.description.isNotEmpty()) expense.description else "No description"
        holder.textViewDateTime.text = "${expense.date} at ${expense.time}"
        holder.textViewDeletedDate.text = "Deleted on ${expense.deletedAt ?: "Unknown date"}"
        
        holder.buttonRestore.setOnClickListener {
            onRestoreClick(position)
        }
        
        holder.buttonDeletePermanently.setOnClickListener {
            onDeletePermanentlyClick(position)
        }
    }

    override fun getItemCount(): Int = deletedExpenses.size
}
