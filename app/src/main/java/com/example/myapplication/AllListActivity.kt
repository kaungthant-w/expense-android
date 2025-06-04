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
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
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

    // Activity result launcher for expense editing
    private val expenseDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val data = result.data
            val shouldDelete = data?.getBooleanExtra("delete_expense", false) ?: false
            
            if (shouldDelete) {
                // Handle delete from detail activity
                val expenseId = data?.getLongExtra("expense_id", -1L) ?: -1L
                handleExpenseDelete(expenseId)
            } else {
                // Handle save/edit from detail activity
                handleExpenseSave(data)
            }
        }
    }    // Selection components
    private lateinit var layoutSelectionControls: CardView
    private lateinit var checkboxSelectAll: CheckBox
    private lateinit var textViewSelectionCount: TextView
    private lateinit var buttonDeleteSelected: Button
    private lateinit var buttonToggleSelection: Button
    private lateinit var buttonCancelSelection: Button
    private var isSelectionMode = false
    
    // Filter components
    private lateinit var cardViewFilterStatus: CardView
    private lateinit var textViewFilterStatus: TextView
    private lateinit var buttonClearActiveFilters: Button
    
    // Modal filter components
    private lateinit var modalSpinnerYear: Spinner
    private lateinit var modalSpinnerMonth: Spinner
    private lateinit var modalEditTextStartDate: EditText
    private lateinit var modalEditTextEndDate: EditText
    
    // Filter state
    private var isFiltersActive = false
    private var currentYearFilter = ""
    private var currentMonthFilter = 0
    private var currentStartDate = ""
    private var currentEndDate = ""
    
    override fun onCreate(savedInstanceState: Bundle?) {        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_list)
          initViews()
        setupNavigationDrawer()
        setupSharedPreferences()
        setupRecyclerView()
        setupStaticTexts()
        loadAllExpenses()
        updateNavigationMenuTitles()
        
        // Setup back press handling
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })
    }
    
    override fun onResume() {
        super.onResume()
        setupStaticTexts()
        loadAllExpenses()
        updateNavigationMenuTitles()
    }
    
    private fun initViews() {
        recyclerView = findViewById(R.id.recyclerViewAllList)
        
        // Navigation Drawer
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        
        // Back button click listener
        findViewById<android.widget.ImageButton>(R.id.buttonBack).setOnClickListener {
            finish()
        }
        
        // History button click listener
        findViewById<Button>(R.id.buttonViewHistory).setOnClickListener {
            navigateToHistory()
        }
        
        // Filter status components
        cardViewFilterStatus = findViewById(R.id.cardViewFilterStatus)
        textViewFilterStatus = findViewById(R.id.textViewFilterStatus)
        buttonClearActiveFilters = findViewById(R.id.buttonClearActiveFilters)        // Selection controls
        layoutSelectionControls = findViewById(R.id.layoutSelectionControls)
        checkboxSelectAll = findViewById<CheckBox>(R.id.checkboxSelectAll)
        textViewSelectionCount = findViewById<TextView>(R.id.textViewSelectionCount)
        buttonDeleteSelected = findViewById<Button>(R.id.buttonDeleteSelected)
        buttonToggleSelection = findViewById<Button>(R.id.buttonToggleSelection)
        buttonCancelSelection = findViewById<Button>(R.id.buttonCancelSelection)
        
        // Action buttons
        findViewById<Button>(R.id.buttonToggleFilter).setOnClickListener {
            showFilterModal()
        }
        
        buttonClearActiveFilters.setOnClickListener {
            clearAllFilters()
        }
        
        setupSelectionControls()
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
    
    private fun showFilterModal() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.modal_filter, null)
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        // Get modal components
        modalSpinnerYear = dialogView.findViewById(R.id.spinnerModalYear)
        modalSpinnerMonth = dialogView.findViewById(R.id.spinnerModalMonth)
        modalEditTextStartDate = dialogView.findViewById(R.id.editTextModalFromDate)
        modalEditTextEndDate = dialogView.findViewById(R.id.editTextModalToDate)
        
        val buttonCloseModal = dialogView.findViewById<ImageButton>(R.id.buttonCloseModal)
        val buttonModalApplyFilter = dialogView.findViewById<Button>(R.id.buttonModalApplyFilter)
        val buttonModalClearFilters = dialogView.findViewById<Button>(R.id.buttonModalClearFilters)
        
        // Setup modal components
        setupModalSpinners()
        setupModalDatePickers()
        
        // Set current filter values
        setCurrentFilterValues()
        
        // Set click listeners
        buttonCloseModal.setOnClickListener {
            dialog.dismiss()
        }
        
        buttonModalApplyFilter.setOnClickListener {
            applyModalFilters()
            dialog.dismiss()
        }
        
        buttonModalClearFilters.setOnClickListener {
            clearModalFilters()
        }
        
        dialog.show()
    }
    
    private fun setupModalSpinners() {
        // Setup year spinner
        val years = listOf("All") + (2020..2030).map { it.toString() }
        val yearAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, years)
        yearAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modalSpinnerYear.adapter = yearAdapter
        
        // Setup month spinner
        val months = listOf("All", "January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December")
        val monthAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, months)
        monthAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        modalSpinnerMonth.adapter = monthAdapter
    }
    
    private fun setupModalDatePickers() {
        modalEditTextStartDate.setOnClickListener {
            showDatePickerDialog { date ->
                modalEditTextStartDate.setText(date)
            }
        }
        
        modalEditTextEndDate.setOnClickListener {
            showDatePickerDialog { date ->
                modalEditTextEndDate.setText(date)
            }
        }
    }
    
    private fun setCurrentFilterValues() {
        // Set current year filter
        if (currentYearFilter.isNotEmpty()) {
            val yearAdapter = modalSpinnerYear.adapter
            for (i in 0 until yearAdapter.count) {
                if (yearAdapter.getItem(i).toString() == currentYearFilter) {
                    modalSpinnerYear.setSelection(i)
                    break
                }
            }
        }
        
        // Set current month filter
        modalSpinnerMonth.setSelection(currentMonthFilter)
        
        // Set current date filters
        modalEditTextStartDate.setText(currentStartDate)
        modalEditTextEndDate.setText(currentEndDate)
    }
    
    private fun applyModalFilters() {
        currentYearFilter = modalSpinnerYear.selectedItem?.toString() ?: "All"
        currentMonthFilter = modalSpinnerMonth.selectedItemPosition
        currentStartDate = modalEditTextStartDate.text.toString().trim()
        currentEndDate = modalEditTextEndDate.text.toString().trim()
        
        // Check if any filters are active
        isFiltersActive = currentYearFilter != "All" || 
                         currentMonthFilter > 0 || 
                         currentStartDate.isNotEmpty() || 
                         currentEndDate.isNotEmpty()
        
        if (isFiltersActive) {
            applyFilters()
            showFilterStatus()
        } else {
            clearAllFilters()
        }
    }
    
    private fun clearModalFilters() {
        modalSpinnerYear.setSelection(0)
        modalSpinnerMonth.setSelection(0)
        modalEditTextStartDate.text.clear()
        modalEditTextEndDate.text.clear()
    }
    
    private fun showFilterStatus() {
        cardViewFilterStatus.visibility = View.VISIBLE
        
        val filterText = buildString {
            append("🔍 Filters: ")
            val filters = mutableListOf<String>()
            
            if (currentYearFilter != "All") {
                filters.add("Year: $currentYearFilter")
            }
            if (currentMonthFilter > 0) {
                val monthNames = listOf("", "Jan", "Feb", "Mar", "Apr", "May", "Jun",
                    "Jul", "Aug", "Sep", "Oct", "Nov", "Dec")
                filters.add("Month: ${monthNames[currentMonthFilter]}")
            }
            if (currentStartDate.isNotEmpty()) {
                filters.add("From: $currentStartDate")
            }
            if (currentEndDate.isNotEmpty()) {
                filters.add("To: $currentEndDate")
            }
            
            append(filters.joinToString(", "))
        }
        
        textViewFilterStatus.text = filterText
    }
    
    private fun clearAllFilters() {
        currentYearFilter = "All"
        currentMonthFilter = 0
        currentStartDate = ""
        currentEndDate = ""
        isFiltersActive = false
        
        cardViewFilterStatus.visibility = View.GONE
        
        // Reset to show all expenses
        allExpenses.clear()
        allExpenses.addAll(originalExpenses)
        allListAdapter.notifyDataSetChanged()
    }private fun showDatePickerDialog(onDateSelected: (String) -> Unit) {
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
    
    private fun applyFilters() {
        val filteredExpenses = filterExpenses()
        allExpenses.clear()
        allExpenses.addAll(filteredExpenses)
        allListAdapter.notifyDataSetChanged()
    }

    private fun filterExpenses(): List<ExpenseItem> {
        var filtered = originalExpenses.toList()
        
        // Year filter
        if (currentYearFilter.isNotEmpty() && currentYearFilter != "All") {
            filtered = filtered.filter { expense ->
                val expenseYear = expense.date.split("/").lastOrNull()
                expenseYear == currentYearFilter
            }
        }
        
        // Month filter
        if (currentMonthFilter > 0) {
            filtered = filtered.filter { expense ->
                val expenseMonth = expense.date.split("/").getOrNull(1)?.toIntOrNull()
                expenseMonth == currentMonthFilter
            }
        }
        
        // Date range filter
        if (currentStartDate.isNotEmpty() || currentEndDate.isNotEmpty()) {
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            
            filtered = filtered.filter { expense ->
                try {
                    val expenseDate = dateFormat.parse(expense.date)
                    var isInRange = true
                    
                    if (currentStartDate.isNotEmpty()) {
                        val start = dateFormat.parse(currentStartDate)
                        isInRange = isInRange && expenseDate != null && start != null && !expenseDate.before(start)
                    }
                    
                    if (currentEndDate.isNotEmpty()) {
                        val end = dateFormat.parse(currentEndDate)
                        isInRange = isInRange && expenseDate != null && end != null && !expenseDate.after(end)
                    }
                    
                    isInRange
                } catch (e: Exception) {
                    false
                }
            }
        }
        
        return filtered
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
        buttonToggleSelection.text = languageManager.getString("cancel_selection")
        updateSelectionUI()
    }

    private fun exitSelectionMode() {
        isSelectionMode = false
        allListAdapter.setSelectionMode(false)
        layoutSelectionControls.visibility = View.GONE
        buttonToggleSelection.text = languageManager.getString("toggle_selection")
    }
    
    private fun updateSelectionUI() {
        val selectedCount = allListAdapter.getSelectedCount()
        textViewSelectionCount.text = languageManager.getString("selection_count").replace("{count}", selectedCount.toString())
        
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
                .setTitle(languageManager.getString("delete_confirmation_title"))
                .setMessage(languageManager.getString("delete_confirmation_message")
                    .replace("{count}", selectedItems.size.toString())
                    .replace("{items}", selectedItems.joinToString(", ") { it.name }))
                .setPositiveButton(languageManager.getString("delete_button")) { _, _ ->
                    performMultipleSoftDelete(selectedItems)
                }
                .setNegativeButton(languageManager.getString("cancel_button"), null)
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
    }private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
        currencyManager = CurrencyManager.getInstance(this)
    }
    
    private fun setupRecyclerView() {
        allListAdapter = AllListAdapter(allExpenses) { item ->
            if (isSelectionMode) {
                allListAdapter.toggleSelection(item)
                updateSelectionUI()
            } else {
                // Open expense for editing when not in selection mode
                openExpenseForEdit(item)
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
            val expensesJson = sharedPreferences.getString("expenses", "[]") ?: "[]"
            val type = object : TypeToken<List<ExpenseItem>>() {}.type
            gson.fromJson(expensesJson, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    private fun saveExpenses(expenses: List<ExpenseItem>) {
        val expensesJson = gson.toJson(expenses)
        sharedPreferences.edit().putString("expenses", expensesJson).apply()
    }

    private fun openExpenseForEdit(expense: ExpenseItem) {
        val intent = Intent(this, ExpenseDetailActivity::class.java)
        intent.putExtra("expense_id", expense.id)
        intent.putExtra("expense_name", expense.name)
        intent.putExtra("expense_price", expense.price)
        intent.putExtra("expense_description", expense.description)
        intent.putExtra("expense_date", expense.date)
        intent.putExtra("expense_time", expense.time)
        intent.putExtra("expense_currency", expense.currency)
        
        expenseDetailLauncher.launch(intent)
    }

    private fun handleExpenseDelete(expenseId: Long) {
        if (expenseId != -1L) {
            val allStoredExpenses = loadAllStoredExpenses().toMutableList()
            val expenseIndex = allStoredExpenses.indexOfFirst { it.id == expenseId }
            
            if (expenseIndex != -1) {
                val expense = allStoredExpenses[expenseIndex]
                // Mark as deleted with timestamp
                val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
                val updatedExpense = expense.copy(
                    isDeleted = true,
                    deletedAt = dateFormat.format(Date())
                )
                allStoredExpenses[expenseIndex] = updatedExpense
                
                // Save all expenses and reload active ones
                saveExpenses(allStoredExpenses)
                loadAllExpenses()
                
                Toast.makeText(this, languageManager.getString("expense_moved_to_history"), Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun handleExpenseSave(data: Intent?) {
        if (data != null) {
            val expenseId = data.getLongExtra("expense_id", -1L)
            val isNewExpense = data.getBooleanExtra("is_new_expense", false)
            
            if (expenseId != -1L) {
                val name = data.getStringExtra("expense_name") ?: ""
                val price = data.getDoubleExtra("expense_price", 0.0)
                val description = data.getStringExtra("expense_description") ?: ""
                val date = data.getStringExtra("expense_date") ?: ""
                val time = data.getStringExtra("expense_time") ?: ""
                val currency = data.getStringExtra("expense_currency") ?: "USD"
                
                val allStoredExpenses = loadAllStoredExpenses().toMutableList()
                
                if (isNewExpense) {
                    // Add new expense (shouldn't happen in AllListActivity, but handle it)
                    val newExpense = ExpenseItem(
                        id = expenseId,
                        name = name,
                        price = price,
                        description = description,
                        date = date,
                        time = time,
                        currency = currency
                    )
                    allStoredExpenses.add(newExpense)
                    Toast.makeText(this, languageManager.getString("expense_added_successfully"), Toast.LENGTH_SHORT).show()
                } else {
                    // Update existing expense
                    val expenseIndex = allStoredExpenses.indexOfFirst { it.id == expenseId }
                    if (expenseIndex != -1) {
                        val existingExpense = allStoredExpenses[expenseIndex]
                        val updatedExpense = existingExpense.copy(
                            name = name,
                            price = price,
                            description = description,
                            date = date,
                            time = time,
                            currency = currency
                        )
                        allStoredExpenses[expenseIndex] = updatedExpense
                        Toast.makeText(this, languageManager.getString("expense_updated_successfully"), Toast.LENGTH_SHORT).show()
                    }
                }
                
                // Save all expenses and reload active ones
                saveExpenses(allStoredExpenses)
                loadAllExpenses()
            }
        }
    }

    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        navigationView.setNavigationItemSelectedListener(this)
    }
    
    private fun setupStaticTexts() {
        // Update action bar title
        supportActionBar?.title = languageManager.getString("all_list_title")
        
        // Update title TextView if it exists
        findViewById<TextView>(R.id.textViewTitle)?.text = languageManager.getString("all_list_title")
        
        // Update selection mode texts
        updateSelectionModeTexts()
    }
    
    private fun updateSelectionModeTexts() {
        // Update selection buttons if they exist
        try {
            buttonToggleSelection.text = languageManager.getString("toggle_selection")
            buttonCancelSelection.text = languageManager.getString("cancel_selection")
            buttonDeleteSelected.text = languageManager.getString("delete_selected")
            checkboxSelectAll.text = languageManager.getString("select_all")
        } catch (e: Exception) {
            // Views not initialized yet, ignore
        }
    }

    private fun updateNavigationMenuTitles() {        val menu = navigationView.menu
        menu.findItem(R.id.nav_home)?.title = languageManager.getString("nav_home")
        menu.findItem(R.id.nav_all_list)?.title = languageManager.getString("nav_all_list")
        menu.findItem(R.id.nav_history)?.title = languageManager.getString("nav_history")
        menu.findItem(R.id.nav_summary)?.title = languageManager.getString("nav_summary")
        menu.findItem(R.id.nav_currency_exchange)?.title = languageManager.getString("nav_currency_exchange")
        menu.findItem(R.id.nav_settings)?.title = languageManager.getString("nav_settings")
        menu.findItem(R.id.nav_feedback)?.title = languageManager.getString("nav_feedback")
        menu.findItem(R.id.nav_about)?.title = languageManager.getString("nav_about")
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.nav_all_list -> {
                // Already in AllListActivity
            }            R.id.nav_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.nav_feedback -> {
                startActivity(Intent(this, FeedbackActivity::class.java))
            }        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
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

        inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {            private val textViewName: TextView = itemView.findViewById(R.id.textViewName)
            private val textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
            private val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
            private val textViewDateTime: TextView = itemView.findViewById(R.id.textViewDateTime)
            private val checkboxSelect: CheckBox = itemView.findViewById(R.id.checkboxSelect)

            fun bind(expense: ExpenseItem) {
                textViewName.text = expense.name
                
                // Use proper currency conversion for display
                val displayAmount = currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
                textViewPrice.text = currencyManager.formatCurrency(displayAmount)
                
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
