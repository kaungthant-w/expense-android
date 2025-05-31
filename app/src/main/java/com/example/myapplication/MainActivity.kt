package com.example.myapplication

import android.app.Activity
import android.util.Log
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class MainActivity : AppCompatActivity() {private lateinit var editTextName: EditText
    private lateinit var editTextPrice: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextDate: EditText
    private lateinit var editTextTime: EditText
    private lateinit var addButton: Button
    private lateinit var buttonSeeMoreInputOptions: TextView
    private lateinit var layoutAdditionalOptions: LinearLayout
    private var isAdditionalOptionsVisible = false
    private lateinit var recyclerView: RecyclerView
    private lateinit var expenseAdapter: ExpenseAdapter
    private val expenseList = mutableListOf<ExpenseItem>()
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    private lateinit var fabMain: FloatingActionButton
    private var fabMenuOverlay: View? = null    // Activity result launcher for expense detail activity
    private val expenseDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {            val data = result.data
            val shouldDelete = data?.getBooleanExtra("delete_expense", false) ?: false
            if (shouldDelete) {
                // Handle delete from detail activity - no confirmation needed as it was already confirmed
                val expenseId = data?.getLongExtra("expense_id", -1L) ?: -1L
                if (expenseId != -1L) {
                    // Find the expense in the list and soft delete it directly
                    val position = expenseList.indexOfFirst { it.id == expenseId }
                    if (position != -1) {
                        val expenseToDelete = expenseList[position]
                        
                        // Mark as deleted with timestamp
                        expenseToDelete.isDeleted = true
                        expenseToDelete.deletedAt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                        
                        // Remove from main list
                        expenseList.removeAt(position)
                        expenseAdapter.notifyItemRemoved(position)
                        expenseAdapter.notifyItemRangeChanged(position, expenseList.size)
                        
                        // Save the deleted expense specifically
                        saveDeletedExpense(expenseToDelete)
                        
                        Toast.makeText(this, "💾 Expense moved to history. Check History to restore.", Toast.LENGTH_LONG).show()
                    }
                }            } else {
                // Handle save/edit from detail activity
                val data = result.data
                if (data != null) {
                    val expenseId = data.getLongExtra("expense_id", -1L)
                    val isNewExpense = data.getBooleanExtra("is_new_expense", false)
                    
                    if (expenseId != -1L) {
                        val name = data.getStringExtra("expense_name") ?: ""
                        val price = data.getDoubleExtra("expense_price", 0.0)
                        val description = data.getStringExtra("expense_description") ?: ""
                        val date = data.getStringExtra("expense_date") ?: ""
                        val time = data.getStringExtra("expense_time") ?: ""
                        
                        if (isNewExpense) {
                            // Add new expense
                            val newExpense = ExpenseItem(
                                id = expenseId,
                                name = name,
                                price = price,
                                description = description,
                                date = date,
                                time = time
                            )
                            expenseList.add(newExpense)
                            expenseAdapter.notifyItemInserted(expenseList.size - 1)
                            saveAllExpenses()
                            Toast.makeText(this, "✅ Expense added successfully!", Toast.LENGTH_SHORT).show()
                        } else {
                            // Update existing expense
                            val position = expenseList.indexOfFirst { it.id == expenseId }
                            if (position != -1) {
                                val expense = expenseList[position]
                                expense.name = name
                                expense.price = price
                                expense.description = description
                                expense.date = date
                                expense.time = time
                                
                                expenseAdapter.notifyItemChanged(position)
                                saveAllExpenses()
                                Toast.makeText(this, "✅ Expense updated successfully!", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                } else {
                    // Fallback: Refresh the expense list for other operations
                    loadExpenses()
                }
            }
        }
    }
      // Activity result launcher for history activity
    private val historyActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Refresh the expense list when returning from history activity
        // This will update the main list in case any expenses were restored
        loadExpenses()
    }
      // Activity result launcher for all list activity
    private val allListActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        // Refresh the expense list when returning from all list activity
        // This will update the main list in case any expenses were restored
        loadExpenses()    }
    
    override fun onCreate(savedInstanceState: Bundle?) {
        // Apply theme before calling super.onCreate()
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        
        initViews()
        setupSharedPreferences()
        setupRecyclerView()
        setupClickListeners()
        setupFabMenu()
        setCurrentDateTime()
        loadExpenses()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh expenses when returning to MainActivity
        // This ensures restored items appear in the list
        loadExpenses()
    }
    
    private fun initViews() {
        editTextName = findViewById(R.id.editTextName)
        editTextPrice = findViewById(R.id.editTextPrice)
        editTextDescription = findViewById(R.id.editTextDescription)
        editTextDate = findViewById(R.id.editTextDate)
        editTextTime = findViewById(R.id.editTextTime)
        addButton = findViewById(R.id.buttonAdd)
        buttonSeeMoreInputOptions = findViewById(R.id.buttonSeeMoreInputOptions)
        layoutAdditionalOptions = findViewById(R.id.layoutAdditionalOptions)
        recyclerView = findViewById(R.id.recyclerViewExpenses)
        fabMain = findViewById(R.id.fabMain)
    }
      private fun setupRecyclerView() {
        expenseAdapter = ExpenseAdapter(expenseList,
            onDeleteClick = { position -> deleteExpenseItem(position) },
            onEditClick = { position -> editExpenseItem(position) },
            onItemClick = { position -> openExpenseDetail(position) }
        )
        recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MainActivity)
            adapter = expenseAdapter
        }
    }
      private fun setupClickListeners() {
        addButton.setOnClickListener {
            addExpenseItem()
        }
        
        buttonSeeMoreInputOptions.setOnClickListener {
            toggleAdditionalOptions()
        }
        
        editTextDate.setOnClickListener {
            showDatePicker()
        }
        
        editTextTime.setOnClickListener {
            showTimePicker()
        }
    }
    
    private fun setCurrentDateTime() {
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        
        editTextDate.setText(dateFormat.format(calendar.time))
        editTextTime.setText(timeFormat.format(calendar.time))
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                editTextDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
      private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                editTextTime.setText(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }
      private fun toggleAdditionalOptions() {
        isAdditionalOptionsVisible = !isAdditionalOptionsVisible
        
        if (isAdditionalOptionsVisible) {
            layoutAdditionalOptions.visibility = View.VISIBLE
            buttonSeeMoreInputOptions.text = "📋 See Less"
        } else {
            layoutAdditionalOptions.visibility = View.GONE
            buttonSeeMoreInputOptions.text = "📋 See More"
        }
    }
    
    private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
    }    private fun loadExpenses() {
        val json = sharedPreferences.getString("expenses", null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<ExpenseItem>>() {}.type
            val savedExpenses: List<ExpenseItem> = gson.fromJson(json, type)
            expenseList.clear()
            // Only load non-deleted expenses
            val activeExpenses = savedExpenses.filter { !it.isDeleted }
            Log.d("MainActivity", "Loading expenses: total=${savedExpenses.size}, active=${activeExpenses.size}")
            expenseList.addAll(activeExpenses)
            expenseAdapter.notifyDataSetChanged()
        } else {
            Log.d("MainActivity", "No expenses found in storage")
        }
    }
    
    private fun saveExpenses() {
        val editor = sharedPreferences.edit()
        val json = gson.toJson(expenseList)
        editor.putString("expenses", json)
        editor.apply()
    }    private fun saveAllExpenses() {
        // Load all existing expenses from storage
        val json = sharedPreferences.getString("expenses", null)
        val allExpenses = mutableListOf<ExpenseItem>()
        
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<ExpenseItem>>() {}.type
            val savedExpenses: List<ExpenseItem> = gson.fromJson(json, type)
            allExpenses.addAll(savedExpenses)
        }
        
        // Update the all expenses list with current active expenses
        // First, remove any existing active expenses (non-deleted)
        allExpenses.removeAll { !it.isDeleted }
        // Then add the current active expenses
        allExpenses.addAll(expenseList)
        
        // Save the complete list (both active and deleted)
        val editor = sharedPreferences.edit()
        val allExpensesJson = gson.toJson(allExpenses)
        editor.putString("expenses", allExpensesJson)
        editor.apply()
    }
    
    // New function to save a deleted expense specifically
    private fun saveDeletedExpense(deletedExpense: ExpenseItem) {
        // Load all existing expenses from storage
        val json = sharedPreferences.getString("expenses", null)
        val allExpenses = mutableListOf<ExpenseItem>()
        
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<ExpenseItem>>() {}.type
            val savedExpenses: List<ExpenseItem> = gson.fromJson(json, type)
            allExpenses.addAll(savedExpenses)
        }
        
        // Update or add the deleted expense
        val existingIndex = allExpenses.indexOfFirst { it.id == deletedExpense.id }
        if (existingIndex != -1) {
            // Update existing expense
            allExpenses[existingIndex] = deletedExpense
        } else {
            // Add new deleted expense
            allExpenses.add(deletedExpense)
        }
        
        // Save the complete list
        val editor = sharedPreferences.edit()
        val allExpensesJson = gson.toJson(allExpenses)
        editor.putString("expenses", allExpensesJson)
        editor.apply()
    }private fun addExpenseItem() {
        val name = editTextName.text.toString().trim()
        val priceText = editTextPrice.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val date = editTextDate.text.toString().trim()
        val time = editTextTime.text.toString().trim()
        
        if (name.isEmpty()) {
            Toast.makeText(this, "Please enter expense name", Toast.LENGTH_SHORT).show()
            return
        }
        
        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0) {
            Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
            return
        }
        
        val expenseItem = ExpenseItem(
            id = System.currentTimeMillis(),
            name = name,
            price = price,
            description = description,
            date = date,
            time = time
        )
          expenseList.add(expenseItem)
        expenseAdapter.notifyItemInserted(expenseList.size - 1)
        clearFields()
        setCurrentDateTime()
        saveAllExpenses() // Use saveAllExpenses to preserve deleted items
    }    private fun editExpenseItem(position: Int) {
        val expense = expenseList[position]
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_expense, null)
        
        val editName = dialogView.findViewById<EditText>(R.id.editDialogName)
        val editPrice = dialogView.findViewById<EditText>(R.id.editDialogPrice)
        val editDescription = dialogView.findViewById<EditText>(R.id.editDialogDescription)
        val editDate = dialogView.findViewById<EditText>(R.id.editDialogDate)
        val editTime = dialogView.findViewById<EditText>(R.id.editDialogTime)
        
        editName.setText(expense.name)
        editPrice.setText(expense.price.toString())
        editDescription.setText(expense.description)
        editDate.setText(expense.date)
        editTime.setText(expense.time)
        
        editDate.setOnClickListener {
            showDatePickerForDialog(editDate)
        }
        
        editTime.setOnClickListener {
            showTimePickerForDialog(editTime)
        }
        
        AlertDialog.Builder(this)
            .setTitle("Edit Expense")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val newName = editName.text.toString().trim()
                val newPriceText = editPrice.text.toString().trim()
                val newDescription = editDescription.text.toString().trim()
                val newDate = editDate.text.toString().trim()
                val newTime = editTime.text.toString().trim()
                
                if (newName.isEmpty()) {
                    Toast.makeText(this, "Please enter expense name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                
                val newPrice = newPriceText.toDoubleOrNull()
                if (newPrice == null || newPrice <= 0) {
                    Toast.makeText(this, "Please enter a valid price", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                  expense.name = newName
                expense.price = newPrice
                expense.description = newDescription
                expense.date = newDate
                expense.time = newTime
                
                expenseAdapter.notifyItemChanged(position)
                saveAllExpenses() // Use saveAllExpenses to preserve deleted items
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showDatePickerForDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                editText.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    
    private fun showTimePickerForDialog(editText: EditText) {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                editText.setText(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }    private fun deleteExpenseItem(position: Int) {
        if (position >= 0 && position < expenseList.size) {
            val expenseToDelete = expenseList[position]
            
            AlertDialog.Builder(this)
                .setTitle("🗑️ Delete Expense")
                .setMessage("Are you sure you want to delete '${expenseToDelete.name}'? It will be moved to history.")
                .setPositiveButton("Delete") { _, _ ->
                    // Mark as deleted with timestamp
                    expenseToDelete.isDeleted = true
                    expenseToDelete.deletedAt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                    
                    // Remove from main list
                    expenseList.removeAt(position)
                    expenseAdapter.notifyItemRemoved(position)
                    expenseAdapter.notifyItemRangeChanged(position, expenseList.size)
                    
                    // Save the deleted expense specifically
                    saveDeletedExpense(expenseToDelete)
                    
                    Toast.makeText(this, "💾 Expense moved to history. Check History to restore.", Toast.LENGTH_LONG).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
  
    
    private fun setupFabMenu() {
        fabMain.setOnClickListener {
            showFabMenu()
        }
    }
    
    private fun showFabMenu() {
        if (fabMenuOverlay != null) {
            hideFabMenu()
            return
        }
        
        val inflater = layoutInflater
        fabMenuOverlay = inflater.inflate(R.layout.fab_menu_overlay, null)
        
        val rootView = findViewById<View>(android.R.id.content)
        if (rootView is android.view.ViewGroup) {
            rootView.addView(fabMenuOverlay)
        }
        
        // Set up click listeners for menu items
        fabMenuOverlay?.findViewById<FloatingActionButton>(R.id.fabSummary)?.setOnClickListener {
            hideFabMenu()
            startActivity(Intent(this, SummaryActivity::class.java))
        }
          fabMenuOverlay?.findViewById<FloatingActionButton>(R.id.fabAnalytics)?.setOnClickListener {
            hideFabMenu()
            startActivity(Intent(this, AnalyticsActivity::class.java))
        }
          fabMenuOverlay?.findViewById<FloatingActionButton>(R.id.fabAllList)?.setOnClickListener {
            hideFabMenu()
            allListActivityLauncher.launch(Intent(this, AllListActivity::class.java))
        }
          fabMenuOverlay?.findViewById<FloatingActionButton>(R.id.fabCurrency)?.setOnClickListener {
            hideFabMenu()
            startActivity(Intent(this, CurrencyExchangeActivity::class.java))
        }
            
        fabMenuOverlay?.findViewById<FloatingActionButton>(R.id.fabHistory)?.setOnClickListener {
            hideFabMenu()
            historyActivityLauncher.launch(Intent(this, HistoryActivity::class.java))
        }
        
        fabMenuOverlay?.findViewById<FloatingActionButton>(R.id.fabSettings)?.setOnClickListener {
            hideFabMenu()
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        
        // Hide menu when clicking on overlay
        fabMenuOverlay?.setOnClickListener {
            hideFabMenu()
        }
    }
    
    private fun hideFabMenu() {
        fabMenuOverlay?.let { overlay ->
            val rootView = findViewById<View>(android.R.id.content)
            if (rootView is android.view.ViewGroup) {
                rootView.removeView(overlay)
            }
        }
        fabMenuOverlay = null
    }
    
    override fun onBackPressed() {
        if (fabMenuOverlay != null) {
            hideFabMenu()
        } else {
            super.onBackPressed()
        }
    }    private fun clearFields() {
        editTextName.text.clear()
        editTextPrice.text.clear()
        editTextDescription.text.clear()
    }
    
    private fun openExpenseDetail(position: Int) {
        if (position >= 0 && position < expenseList.size) {
            val expense = expenseList[position]
            val intent = Intent(this, ExpenseDetailActivity::class.java)
            intent.putExtra("expense_id", expense.id)
            intent.putExtra("expense_name", expense.name)
            intent.putExtra("expense_price", expense.price)
            intent.putExtra("expense_description", expense.description)
            intent.putExtra("expense_date", expense.date)
            intent.putExtra("expense_time", expense.time)
            intent.putExtra("expense_position", position)
            expenseDetailLauncher.launch(intent)
        }
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
}
