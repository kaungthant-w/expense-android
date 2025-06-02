package com.example.myapplication

import android.app.DatePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.android.material.snackbar.Snackbar
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class AllListActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var recyclerView: RecyclerView
        private lateinit var allListAdapter: AllListAdapter
    private val allExpenses = mutableListOf<ExpenseItem>()
    private val originalExpenses = mutableListOf<ExpenseItem>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currencyManager: CurrencyManager
    private val gson = Gson()

    // Navigation Drawer components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    // Selection components
    private lateinit var layoutSelectionControls: CardView
    private lateinit var checkboxSelectAll: CheckBox
    private lateinit var textViewSelectionCount: TextView
    private lateinit var buttonDeleteSelected: Button
    private lateinit var buttonToggleSelection: Button
    private lateinit var buttonCancelSelection: Button
    private var isSelectionMode = false

    // Filter components
    private lateinit var cardViewFilterControls: CardView
    private lateinit var spinnerYear: Spinner
    private lateinit var spinnerMonth: Spinner
    private lateinit var editTextStartDate: EditText
    private lateinit var editTextEndDate: EditText
    private lateinit var buttonFilter: Button
    private lateinit var buttonToggleFilter: Button
    private lateinit var buttonClearFilter: Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_list)
        
        initViews()
        setupNavigationDrawer()
        setupSharedPreferences()
        setupRecyclerView()
        loadAllExpenses()
        updateNavigationMenuTitles()
    }    override fun onResume() {
        super.onResume()
        loadAllExpenses()
        updateNavigationMenuTitles()
    }

    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewAllList)
        
        // Navigation Drawer
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        
        // Selection controls
        layoutSelectionControls = findViewById(R.id.layoutSelectionControls)
        checkboxSelectAll = findViewById(R.id.checkboxSelectAll)
        textViewSelectionCount = findViewById(R.id.textViewSelectionCount)
        buttonDeleteSelected = findViewById(R.id.buttonDeleteSelected)
        buttonToggleSelection = findViewById(R.id.buttonToggleSelection)
        buttonCancelSelection = findViewById(R.id.buttonCancelSelection)
        
        // Filter controls
        cardViewFilterControls = findViewById(R.id.cardViewFilterControls)
        spinnerYear = findViewById(R.id.spinnerYear)
        spinnerMonth = findViewById(R.id.spinnerMonth)
        editTextStartDate = findViewById(R.id.editTextFromDate)
        editTextEndDate = findViewById(R.id.editTextToDate)
        buttonFilter = findViewById(R.id.buttonApplyFilter)
        buttonToggleFilter = findViewById(R.id.buttonToggleFilter)
        buttonClearFilter = findViewById(R.id.buttonClearFilters)
        
        setupSelectionControls()
        setupFilterControls()
        setupDatePickerFields()
    }

    private fun setupSelectionControls() {
        buttonToggleSelection.setOnClickListener {
            toggleSelectionMode()
        }
        
        buttonCancelSelection.setOnClickListener {
            exitSelectionMode()
        }
        
        checkboxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (isSelectionMode) {
                if (isChecked) {
                    allListAdapter.selectAll()
                } else {
                    allListAdapter.clearSelection()
                }
                updateSelectionUI()
            }
        }
        
        buttonDeleteSelected.setOnClickListener {
            deleteSelectedItems()
        }
    }

    private fun setupFilterControls() {
        buttonToggleFilter.setOnClickListener {
            toggleFilterVisibility()
        }
        
        buttonFilter.setOnClickListener {
            applyFilters()
        }
        
        buttonClearFilter.setOnClickListener {
            clearFilters()
        }
        
        setupYearSpinner()
        setupMonthSpinner()
    }

    private fun setupDatePickerFields() {
        editTextStartDate.setOnClickListener {
            showDatePickerDialog(editTextStartDate) { date ->
                editTextStartDate.setText(date)
            }
        }
        
        editTextEndDate.setOnClickListener {
            showDatePickerDialog(editTextEndDate) { date ->
                editTextEndDate.setText(date)
            }
        }
    }

    private fun showDatePickerDialog(editText: EditText, onDateSelected: (String) -> Unit) {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%02d/%02d/%04d", dayOfMonth, month + 1, year)
                onDateSelected(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }

    private fun setupYearSpinner() {
        val years = (2020..2030).map { it.toString() }
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerYear.adapter = yearAdapter
    }

    private fun setupMonthSpinner() {
        val months = listOf("All", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December")
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerMonth.adapter = monthAdapter
    }

    private fun toggleSelectionMode() {
        if (isSelectionMode) {
            exitSelectionMode()
        } else {
            enterSelectionMode()
        }
    }

    private fun enterSelectionMode() {
        isSelectionMode = true
        allListAdapter.setSelectionMode(true)
        layoutSelectionControls.visibility = View.VISIBLE
        buttonToggleSelection.text = "Cancel Selection"
        updateSelectionUI()
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        allListAdapter.setSelectionMode(false)
        layoutSelectionControls.visibility = View.GONE
        buttonToggleSelection.text = "Select Items"
    }

    private fun updateSelectionUI() {
        val selectedCount = allListAdapter.getSelectedCount()
        textViewSelectionCount.text = "$selectedCount selected"
        
        val totalItems = allExpenses.size
        checkboxSelectAll.isChecked = selectedCount == totalItems && totalItems > 0
        
        buttonDeleteSelected.isEnabled = selectedCount > 0
        
        checkboxSelectAll.setOnCheckedChangeListener(null)
        if (isSelectionMode) {
            if (selectedCount == totalItems && totalItems > 0) {
                checkboxSelectAll.isChecked = true
            } else {
                checkboxSelectAll.isChecked = false
            }
        }
        checkboxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (isSelectionMode) {
                if (isChecked) {
                    allListAdapter.selectAll()
                } else {
                    allListAdapter.clearSelection()
                }
            }
        }
    }

    private fun deleteSelectedItems() {
        val selectedItems = allListAdapter.getSelectedItems()
        if (selectedItems.isNotEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("Delete Selected Items")
                .setMessage("Are you sure you want to delete ${selectedItems.size} item(s)?")
                .setPositiveButton("Delete") { _, _ ->
                    performMultipleSoftDelete(selectedItems)
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }

    private fun performMultipleSoftDelete(itemsToDelete: List<ExpenseItem>) {
        val allStoredExpenses = loadAllStoredExpenses().toMutableList()
        
        for (item in itemsToDelete) {
            val index = allStoredExpenses.indexOfFirst { expense ->
                expense.name == item.name &&
                expense.price == item.price &&
                expense.date == item.date &&
                expense.time == item.time &&
                expense.description == item.description
            }
            
            if (index != -1) {
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val updatedExpense = allStoredExpenses[index].copy(
                    isDeleted = true,
                    deletedAt = dateFormat.format(Date())
                )
                allStoredExpenses[index] = updatedExpense
            }
        }
        
        saveExpenses(allStoredExpenses)
        loadAllExpenses()
        exitSelectionMode()
        
        val deletedCount = itemsToDelete.size
        val message = if (deletedCount == 1) "1 item moved to history" else "$deletedCount items moved to history"
        showHistorySnackbar(message)
    }

    private fun showHistorySnackbar(message: String) {
        Snackbar.make(recyclerView, message, Snackbar.LENGTH_LONG)
            .setAction("View History") {
                navigateToHistory()
            }
            .show()
    }

    private fun navigateToHistory() {
        startActivity(Intent(this, HistoryActivity::class.java))
    }

    private fun toggleFilterVisibility() {
        cardViewFilterControls.visibility = if (cardViewFilterControls.visibility == View.VISIBLE) {
            View.GONE
        } else {
            View.VISIBLE
        }
    }

    private fun applyFilters() {
        val filteredExpenses = filterExpenses()
        allExpenses.clear()
        allExpenses.addAll(filteredExpenses)
        allListAdapter.notifyDataSetChanged()
    }

    private fun filterExpenses(): List<ExpenseItem> {
        var filtered = originalExpenses.toList()
        
        val selectedYear = spinnerYear.selectedItem?.toString()
        if (!selectedYear.isNullOrEmpty() && selectedYear != "All") {
            filtered = filtered.filter { expense ->
                val expenseYear = expense.date.split("/").lastOrNull()
                expenseYear == selectedYear
            }
        }
        
        val selectedMonthIndex = spinnerMonth.selectedItemPosition
        if (selectedMonthIndex > 0) {
            filtered = filtered.filter { expense ->
                val expenseMonth = expense.date.split("/").getOrNull(1)?.toIntOrNull()
                expenseMonth == selectedMonthIndex
            }
        }
        
        val startDate = editTextStartDate.text.toString().trim()
        val endDate = editTextEndDate.text.toString().trim()
        
        if (startDate.isNotEmpty() || endDate.isNotEmpty()) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            filtered = filtered.filter { expense ->
                try {
                    val expenseDate = dateFormat.parse(expense.date)
                    var isInRange = true
                    
                    if (startDate.isNotEmpty()) {
                        val start = dateFormat.parse(startDate)
                        isInRange = isInRange && expenseDate != null && start != null && !expenseDate.before(start)
                    }
                    
                    if (endDate.isNotEmpty()) {
                        val end = dateFormat.parse(endDate)
                        isInRange = isInRange && expenseDate != null && end != null && !expenseDate.after(end)
                    }
                    
                    isInRange
                } catch (e: Exception) {
                    false
                }
            }
        }
        
        return filtered
    }    private fun clearFilters() {
        spinnerYear.setSelection(0)
        spinnerMonth.setSelection(0)
        editTextStartDate.text.clear()
        editTextEndDate.text.clear()
        allExpenses.clear()
        allExpenses.addAll(originalExpenses)
        allListAdapter.notifyDataSetChanged()
    }

    private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("MyAppPrefs", Context.MODE_PRIVATE)
        currencyManager = CurrencyManager.getInstance(this)
    }

    private fun setupRecyclerView() {
        allListAdapter = AllListAdapter(allExpenses) { item ->
            if (isSelectionMode) {
                allListAdapter.toggleSelection(item)
                updateSelectionUI()
            }
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = allListAdapter
    }

    private fun loadAllExpenses() {
        val allStoredExpenses = loadAllStoredExpenses()
        val activeExpenses = allStoredExpenses.filter { !it.isDeleted }
        
        originalExpenses.clear()
        originalExpenses.addAll(activeExpenses)
        allExpenses.clear()
        allExpenses.addAll(activeExpenses)
        allListAdapter.notifyDataSetChanged()
    }

    private fun loadAllStoredExpenses(): List<ExpenseItem> {
        return try {
            val expensesJson = sharedPreferences.getString("all_expenses", "[]") ?: "[]"
            val type = object : TypeToken<List<ExpenseItem>>() {}.type
            gson.fromJson(expensesJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }

    private fun saveExpenses(expenses: List<ExpenseItem>) {
        val expensesJson = gson.toJson(expenses)
        sharedPreferences.edit().putString("all_expenses", expensesJson).apply()
    }

    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)
    }

    private fun updateNavigationMenuTitles() {
        // Update navigation menu titles if needed
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.nav_all_list -> {
                // Already in AllListActivity
            }
            R.id.nav_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
            }
            R.id.nav_analytics -> {
                startActivity(Intent(this, AnalyticsActivity::class.java))
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.nav_feedback -> {
                startActivity(Intent(this, FeedbackActivity::class.java))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }

    inner class AllListAdapter(
        private val expenses: MutableList<ExpenseItem>,
        private val onItemClick: (ExpenseItem) -> Unit
    ) : RecyclerView.Adapter<AllListAdapter.ViewHolder>() {

        private val selectedItems = mutableSetOf<ExpenseItem>()
        private var isSelectionModeEnabled = false

        fun setSelectionMode(enabled: Boolean) {
            isSelectionModeEnabled = enabled
            if (!enabled) {
                selectedItems.clear()
            }
            notifyDataSetChanged()
        }

        fun toggleSelection(item: ExpenseItem) {
            if (selectedItems.contains(item)) {
                selectedItems.remove(item)
            } else {
                selectedItems.add(item)
            }
            notifyDataSetChanged()
        }

        fun selectAll() {
            selectedItems.clear()
            selectedItems.addAll(expenses)
            notifyDataSetChanged()
        }

        fun clearSelection() {
            selectedItems.clear()
            notifyDataSetChanged()
        }

        fun getSelectedCount(): Int = selectedItems.size

        fun getSelectedItems(): List<ExpenseItem> = selectedItems.toList()

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_all_list, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val expense = expenses[position]
            holder.bind(expense)
        }

        override fun getItemCount(): Int = expenses.size

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
            private val textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
            private val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
            private val textViewDateTime: TextView = itemView.findViewById(R.id.textViewDateTime)
            private val checkboxSelect: CheckBox = itemView.findViewById(R.id.checkboxSelect)

            fun bind(expense: ExpenseItem) {
                textViewName.text = expense.name
                textViewPrice.text = currencyManager.formatCurrency(expense.price)
                textViewDescription.text = expense.description
                textViewDateTime.text = "📅 ${expense.date} • 🕐 ${expense.time}"

                // Selection mode handling
                if (isSelectionModeEnabled) {
                    checkboxSelect.visibility = View.VISIBLE
                    checkboxSelect.isChecked = selectedItems.contains(expense)
                    checkboxSelect.setOnCheckedChangeListener { _, isChecked ->
                        if (isChecked) {
                            selectedItems.add(expense)
                        } else {
                            selectedItems.remove(expense)
                        }
                        this@AllListActivity.updateSelectionUI()
                    }
                } else {
                    checkboxSelect.visibility = View.GONE
                }

                // Item click handling
                itemView.setOnClickListener {
                    if (isSelectionModeEnabled) {
                        checkboxSelect.isChecked = !checkboxSelect.isChecked
                    } else {
                        onItemClick(expense)
                    }
                }

                // Long click for selection mode
                itemView.setOnLongClickListener {
                    if (!isSelectionModeEnabled) {
                        this@AllListActivity.enterSelectionMode()
                        selectedItems.add(expense)
                        this@AllListActivity.updateSelectionUI()
                        notifyDataSetChanged()
                    }
                    true
                }
            }
        }
    }
}
