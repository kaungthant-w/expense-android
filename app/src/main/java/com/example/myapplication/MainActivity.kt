package com.example.myapplication

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.appcompat.widget.Toolbar
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.navigation.NavigationView
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar
import java.util.Date
import java.util.Locale

class MainActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var expenseAdapter: ExpenseAdapter
    private val expenseList = mutableListOf<ExpenseItem>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currencyManager: CurrencyManager
    private val gson = Gson()
    private lateinit var toolbar: Toolbar
      // Navigation Drawer components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var drawerToggle: ActionBarDrawerToggle
    
    // New UI components
    private lateinit var todaySummaryCard: CardView
    private lateinit var todayExpensesCount: TextView
    private lateinit var todayTotalAmount: TextView
    private lateinit var todaySummaryTitle: TextView
    private lateinit var todayExpensesLabel: TextView
    private lateinit var todayAmountLabel: TextView
    private lateinit var tabLayout: TabLayout
    private lateinit var viewPager: ViewPager2
    private lateinit var viewPagerAdapter: ExpenseViewPagerAdapter
    private lateinit var fab: DraggableFloatingActionButton
    
    // Activity result launcher for expense detail activity
    private val expenseDetailLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == RESULT_OK) {
            val deleteData = result.data
            val shouldDelete = deleteData?.getBooleanExtra("delete_expense", false) ?: false
            if (shouldDelete) {
                // Handle delete from detail activity - no confirmation needed as it was already confirmed
                val expenseId = deleteData?.getLongExtra("expense_id", -1L) ?: -1L
                if (expenseId != -1L) {                    // Find the expense in the list and soft delete it directly
                    val position = expenseList.indexOfFirst { it.id == expenseId }
                    if (position != -1) {
                        val expenseToDelete = expenseList[position]
                        
                        // Mark as deleted with timestamp
                        expenseToDelete.isDeleted = true
                        expenseToDelete.deletedAt = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault()).format(Date())
                          // Remove from main list
                        expenseList.removeAt(position)
                        // Check if adapter is initialized before calling notify methods
                        if (::expenseAdapter.isInitialized) {
                            expenseAdapter.notifyItemRemoved(position)
                            expenseAdapter.notifyItemRangeChanged(position, expenseList.size)
                        }
                        
                        // Save the deleted expense specifically
                        saveDeletedExpense(expenseToDelete)
                        Toast.makeText(this, languageManager.getString("expense_moved_to_history"), Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                // Handle save/edit from detail activity
                val activityResult = result.data
                if (activityResult != null) {
                    val expenseId = activityResult.getLongExtra("expense_id", -1L)
                    val isNewExpense = activityResult.getBooleanExtra("is_new_expense", false)
                    if (expenseId != -1L) {
                        val name = activityResult.getStringExtra("expense_name") ?: ""
                        val price = activityResult.getDoubleExtra("expense_price", 0.0)
                        val description = activityResult.getStringExtra("expense_description") ?: ""
                        val date = activityResult.getStringExtra("expense_date") ?: ""
                        val time = activityResult.getStringExtra("expense_time") ?: ""
                        val currency = activityResult.getStringExtra("expense_currency") ?: "USD"
                          if (isNewExpense) {
                            // Add new expense
                            val newExpense = ExpenseItem(
                                id = expenseId,
                                name = name,
                                price = price,
                                description = description,
                                date = date,
                                time = time,
                                currency = currency                            )
                            expenseList.add(newExpense)
                            if (::expenseAdapter.isInitialized) {
                                expenseAdapter.notifyItemInserted(expenseList.size - 1)
                            }
                            saveAllExpenses()
                            Toast.makeText(this, languageManager.getString("expense_added_successfully"), Toast.LENGTH_SHORT).show()
                        } else {
                            // Update existing expense
                            val position = expenseList.indexOfFirst { it.id == expenseId }
                            if (position != -1) {                                val expense = expenseList[position]
                                expense.name = name
                                expense.price = price
                                expense.description = description
                                expense.date = date
                                expense.time = time
                                expense.currency = currency
                                  if (::expenseAdapter.isInitialized) {
                                    expenseAdapter.notifyItemChanged(position)
                                }
                                saveAllExpenses()
                                Toast.makeText(this, languageManager.getString("expense_updated_successfully"), Toast.LENGTH_SHORT).show()
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
    ) { _ ->
        // Refresh the expense list when returning from history activity
        // This will update the main list in case any expenses were restored
        loadExpenses()
    }
      // Activity result launcher for all list activity
    private val allListActivityLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { _ ->
        // Refresh the expense list when returning from all list activity
        // This will update the main list in case any expenses were restored
        loadExpenses()
    }
      override fun onCreate(savedInstanceState: Bundle?) {
        try {
            // Apply theme before calling super.onCreate()
            applyTheme()
            super.onCreate(savedInstanceState)
            setContentView(R.layout.activity_main)

            initViews()
            setupSharedPreferences()
            setupRecyclerView()
            setupClickListeners()
            setupToolbar()
            setupNavigationDrawer()
            setupViewPager()
            // setCurrentDateTime() removed: obsolete with modal dialog
            loadExpenses()
            updateNavigationMenuTitles()
            updateTodaySummaryCard()
            updateTodaySummary()
        } catch (e: Exception) {
            Log.e("MainActivity", "Initialization error", e)
            Toast.makeText(this, "App error: " + (e.message ?: "Unknown error"), Toast.LENGTH_LONG).show()
            // Optionally finish() to avoid a blank screen
            // finish()
        }
    }

    override fun onPostCreate(savedInstanceState: Bundle?) {
        super.onPostCreate(savedInstanceState)
        // Sync the toggle state after onRestoreInstanceState has occurred        drawerToggle.syncState()
    }
      override fun onResume() {
        super.onResume()
        
        // Check if language needs to be updated (for when returning from LanguageActivity)
        // This ensures immediate language updates even if broadcast was missed while paused
        refreshLanguageIfNeeded()
        
        // Refresh expenses when returning to MainActivity
        // This ensures restored items appear in the list
        loadExpenses()
        // Refresh fragments and summary if they exist
        if (::viewPagerAdapter.isInitialized) {
            refreshAllFragments()
        }
    }
    
    private fun refreshLanguageIfNeeded() {
        // Force refresh of all UI elements to catch any language changes
        // that might have occurred while activity was paused
        Log.d("MainActivity", "Checking for language updates in onResume")
        
        updateToolbarTitle()
        updateNavigationMenuTitles()
        updateNavigationHeaderText()
        updateTodaySummaryCard()
        updateTabTitles()
        
        // Refresh fragment translations if they exist
        if (::viewPagerAdapter.isInitialized) {
            refreshAllFragments()
        }
        
        // Force refresh the views
        runOnUiThread {
            navigationView.invalidate()
            todaySummaryCard.invalidate()
            tabLayout.invalidate()
        }
        
        Log.d("MainActivity", "Language refresh completed in onResume")
    }    override fun onLanguageChanged() {
        // Don't call super.onLanguageChanged() as it calls recreate()
        // We want to update UI immediately without recreating the activity
        Log.d("MainActivity", "onLanguageChanged called - updating all UI elements")
        
        // Only proceed if activity is active and not finishing
        if (isFinishing || isDestroyed) {
            Log.d("MainActivity", "Activity is finishing/destroyed, skipping language update")
            return
        }
        
        try {
            // Update all UI elements in the correct order
            updateToolbarTitle()
            updateNavigationMenuTitles()
            updateNavigationHeaderText()
            updateTodaySummaryCard()
            updateTabTitles()
            
            // Refresh fragment translations only if adapter is initialized and we're not transitioning
            if (::viewPagerAdapter.isInitialized && !isChangingConfigurations) {
                refreshAllFragments()
            }
            
            // Force refresh the views
            runOnUiThread {
                navigationView.invalidate()
                todaySummaryCard.invalidate()
                tabLayout.invalidate()
            }
        } catch (e: Exception) {
            Log.e("MainActivity", "Error during language change: ${e.message}")
        }
        
        Log.d("MainActivity", "onLanguageChanged completed - all UI elements updated")
    }
    
    private fun initViews() {
        // Navigation drawer components
        drawerLayout = findViewById<DrawerLayout>(R.id.drawer_layout)
        navigationView = findViewById<NavigationView>(R.id.nav_view)
        toolbar = findViewById<Toolbar>(R.id.toolbar)
        // New UI components
        todaySummaryCard = findViewById<CardView>(R.id.todaySummaryCard)
        todayExpensesCount = findViewById<TextView>(R.id.todayExpensesCount)
        todayTotalAmount = findViewById<TextView>(R.id.todayTotalAmount)
        todaySummaryTitle = findViewById<TextView>(R.id.todaySummaryTitle)
        todayExpensesLabel = findViewById<TextView>(R.id.todayExpensesLabel)
        todayAmountLabel = findViewById<TextView>(R.id.todayAmountLabel)
        tabLayout = findViewById<TabLayout>(R.id.tabLayout)
        viewPager = findViewById<ViewPager2>(R.id.viewPager)
        fab = findViewById<DraggableFloatingActionButton>(R.id.fab)
    }
    
    private fun setupRecyclerView() {
        // RecyclerView setup is now handled by ExpenseListFragment        // Initialize the adapter for fragment communication
        expenseAdapter = ExpenseAdapter(expenseList,
            onDeleteClick = { position -> deleteExpenseItem(position) },
            onEditClick = { position -> editExpenseItem(position) },
            onItemClick = { position -> openExpenseDetail(position) },
            languageManager = languageManager
        )
    }
    
    private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
        currencyManager = CurrencyManager.getInstance(this)
    }
    
    private fun setupClickListeners() {
        // Set up FAB click listener to show add expense modal
        fab.setOnClickListener {
            showAddExpenseModal()        }
    }
    
    private fun setupToolbar() {
        // Set up the toolbar as the action bar
        setSupportActionBar(toolbar)
        
        // Enable the home button
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.setDisplayShowTitleEnabled(true)
        supportActionBar?.title = "HSU Expense"
    }
    
    private fun setupNavigationDrawer() {
        // Set up the drawer toggle
        drawerToggle = ActionBarDrawerToggle(
            this, drawerLayout, toolbar,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(drawerToggle)
        drawerToggle.syncState()
        
        // Set navigation item selected listener
        navigationView.setNavigationItemSelectedListener(this)
    }
    
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.toolbar_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.action_export -> {
                // Launch ExportActivity
                startActivity(Intent(this, ExportActivity::class.java))
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    private fun setupViewPager() {
        viewPagerAdapter = ExpenseViewPagerAdapter(this)
        viewPager.adapter = viewPagerAdapter
        
        TabLayoutMediator(tabLayout, viewPager) { tab, position ->
            tab.text = viewPagerAdapter.getTabTitle(position)
        }.attach()
    }

    private fun showAddExpenseModal() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_expense_modal, null)
        
        // Initialize dialog form elements
        val modalEditTextName = dialogView.findViewById<EditText>(R.id.editTextName)
        val modalEditTextPrice = dialogView.findViewById<EditText>(R.id.editTextPrice)
        val modalEditTextDescription = dialogView.findViewById<EditText>(R.id.editTextDescription)
        val modalEditTextDate = dialogView.findViewById<EditText>(R.id.editTextDate)
        val modalEditTextTime = dialogView.findViewById<EditText>(R.id.editTextTime)
        val modalButtonSeeMore = dialogView.findViewById<TextView>(R.id.buttonSeeMoreInputOptions)
        val modalLayoutAdditional = dialogView.findViewById<LinearLayout>(R.id.layoutAdditionalOptions)
        val modalButtonAdd = dialogView.findViewById<Button>(R.id.buttonAdd)
        val modalButtonCancel = dialogView.findViewById<Button>(R.id.buttonCancel)
        val modalDialogTitle = dialogView.findViewById<TextView>(R.id.dialogTitle)
        
        // Set localized text for modal dialog elements
        updateModalDialogTexts(dialogView, modalEditTextName, modalEditTextPrice, modalEditTextDescription, 
                               modalEditTextDate, modalEditTextTime, modalButtonSeeMore, modalButtonAdd, 
                               modalButtonCancel, modalDialogTitle)
        
        // Set current date and time
        val calendar = Calendar.getInstance()
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
        modalEditTextDate.setText(dateFormat.format(calendar.time))
        modalEditTextTime.setText(timeFormat.format(calendar.time))
        
        // Setup click listeners for modal fields
        var isModalAdditionalOptionsVisible = false
        modalButtonSeeMore.setOnClickListener {
            isModalAdditionalOptionsVisible = !isModalAdditionalOptionsVisible
            modalLayoutAdditional.visibility = if (isModalAdditionalOptionsVisible) View.VISIBLE else View.GONE
            modalButtonSeeMore.text = if (isModalAdditionalOptionsVisible) languageManager.getString("see_less") else languageManager.getString("see_more")
        }
        
        modalEditTextDate.setOnClickListener {
            showDatePickerForModal(modalEditTextDate)
        }
        
        modalEditTextTime.setOnClickListener {
            showTimePickerForModal(modalEditTextTime)
        }
        
        // Create and show dialog
        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(true)
            .create()
        
        // Setup dialog button actions
        modalButtonCancel.setOnClickListener {
            dialog.dismiss()
        }
        
        modalButtonAdd.setOnClickListener {
            addExpenseFromModal(dialog, modalEditTextName, modalEditTextPrice, modalEditTextDescription, modalEditTextDate, modalEditTextTime)        }
        
        dialog.show()
    }
    
    private fun showDatePickerForModal(editText: EditText) {
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
    
    private fun showTimePickerForModal(editText: EditText) {
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
    }
    
    private fun addExpenseFromModal(
        dialog: AlertDialog,
        modalEditTextName: EditText,
        modalEditTextPrice: EditText,
        modalEditTextDescription: EditText,
        modalEditTextDate: EditText,
        modalEditTextTime: EditText
    ) {
        val name = modalEditTextName.text.toString().trim()
        val priceText = modalEditTextPrice.text.toString().trim()
        val description = modalEditTextDescription.text.toString().trim()
        val date = modalEditTextDate.text.toString().trim()
        val time = modalEditTextTime.text.toString().trim()
        
        // Validation
        if (name.isEmpty()) {
            modalEditTextName.error = languageManager.getString("name_required")
            modalEditTextName.requestFocus()
            return
        }
        
        if (priceText.isEmpty()) {
            modalEditTextPrice.error = languageManager.getString("price_required")
            modalEditTextPrice.requestFocus()
            return
        }
        
        val price = try {
            priceText.toDouble()
        } catch (e: NumberFormatException) {
            modalEditTextPrice.error = languageManager.getString("invalid_price_format")
            modalEditTextPrice.requestFocus()
            return
        }
        
        if (price <= 0) {
            modalEditTextPrice.error = languageManager.getString("price_must_be_positive")
            modalEditTextPrice.requestFocus()
            return
        }
          // Create expense item
        val expenseItem = ExpenseItem(
            id = System.currentTimeMillis(),
            name = name,
            price = price,
            description = description,
            date = date,
            time = time,
            currency = currencyManager.getStorageCurrency()
        )
          // Add new expense at the beginning of the list (latest first)
        expenseList.add(0, expenseItem)
        if (::expenseAdapter.isInitialized) {
            expenseAdapter.notifyItemInserted(0)
        }
        saveAllExpenses()
        
        // Refresh fragments and summary
        refreshAllFragments()
        updateTodaySummary()
        
        // Close dialog and show success message
        dialog.dismiss()
        Toast.makeText(this, languageManager.getString("expense_added_successfully"), Toast.LENGTH_SHORT).show()
    }
      private fun updateTodaySummary() {
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val todayExpenses = expenseList.filter { it.date == today }

        todayExpensesCount.text = todayExpenses.size.toString()

        val total = todayExpenses.sumOf { expense ->
            currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        }

        // Use CurrencyManager to format the total with the correct symbol (MMK or $)
        todayTotalAmount.text = currencyManager.formatCurrency(total)
    }
    
    // Methods for fragment communication
    fun getAllExpenses(): List<ExpenseItem> {
        return expenseList.toList()
    }
    
    fun deleteExpenseFromFragment(position: Int, filteredExpenses: List<ExpenseItem>) {
        if (position >= 0 && position < filteredExpenses.size) {
            val expenseToDelete = filteredExpenses[position]
            // Find the expense in the main list
            val mainPosition = expenseList.indexOfFirst { it.id == expenseToDelete.id }
            if (mainPosition != -1) {
                deleteExpenseItem(mainPosition)
            }
        }
    }
    
    fun editExpenseFromFragment(position: Int, filteredExpenses: List<ExpenseItem>) {
        if (position >= 0 && position < filteredExpenses.size) {
            val expenseToEdit = filteredExpenses[position]
            // Find the expense in the main list
            val mainPosition = expenseList.indexOfFirst { it.id == expenseToEdit.id }
            if (mainPosition != -1) {
                editExpenseItem(mainPosition)
            }
        }
    }
    
    fun openExpenseDetailFromFragment(position: Int, filteredExpenses: List<ExpenseItem>) {
        if (position >= 0 && position < filteredExpenses.size) {
            val expenseToOpen = filteredExpenses[position]
            // Find the expense in the main list
            val mainPosition = expenseList.indexOfFirst { it.id == expenseToOpen.id }
            if (mainPosition != -1) {
                openExpenseDetail(mainPosition)
            }
        }
    }    private fun refreshAllFragments() {
        // Refresh all fragments in the ViewPager with safety checks
        try {
            for (i in 0 until viewPagerAdapter.itemCount) {
                val fragment = supportFragmentManager.findFragmentByTag("f$i") as? ExpenseListFragment
                if (fragment != null && fragment.isAdded && !fragment.isDetached) {
                    fragment.refreshExpenses()
                    fragment.refreshTranslations()
                }
            }
            updateTodaySummary()
        } catch (e: Exception) {
            android.util.Log.e("MainActivity", "Error refreshing fragments: ${e.message}")
        }
    }
    
    private fun updateNavigationMenuTitles() {
        Log.d("MainActivity", "Updating navigation menu titles")
          val menu = navigationView.menu
        menu.findItem(R.id.nav_home)?.title = languageManager.getString("nav_home")
        menu.findItem(R.id.nav_all_list)?.title = languageManager.getString("nav_all_list")
        menu.findItem(R.id.nav_history)?.title = languageManager.getString("nav_history")
        menu.findItem(R.id.nav_summary)?.title = languageManager.getString("nav_summary")
        menu.findItem(R.id.nav_currency_exchange)?.title = languageManager.getString("nav_currency_exchange")
        menu.findItem(R.id.nav_export_excel)?.title = languageManager.getString("nav_export_excel")
        menu.findItem(R.id.nav_settings)?.title = languageManager.getString("nav_settings")
        menu.findItem(R.id.nav_feedback)?.title = languageManager.getString("nav_feedback")
        menu.findItem(R.id.nav_about)?.title = languageManager.getString("nav_about")
        
        // Force refresh the navigation view
        navigationView.invalidate()
        navigationView.requestLayout()
        
        Log.d("MainActivity", "Navigation menu titles updated")
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
    }
    
    private fun loadExpenses() {
        val json = sharedPreferences.getString("expenses", null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<ExpenseItem>>() {}.type
            val savedExpenses: List<ExpenseItem> = gson.fromJson(json, type)
            expenseList.clear()
            // Only load non-deleted expenses and sort by timestamp (latest first)
            val activeExpenses = savedExpenses.filter { !it.isDeleted }
                .sortedByDescending { it.id } // Sort by ID (timestamp) in descending order            Log.d("MainActivity", "Loading expenses: total=${savedExpenses.size}, active=${activeExpenses.size}")
            expenseList.addAll(activeExpenses)
            if (::expenseAdapter.isInitialized) {
                expenseAdapter.notifyDataSetChanged()
            }
            
            // Refresh fragments if they exist
            if (::viewPagerAdapter.isInitialized) {
                refreshAllFragments()
            }
        } else {
            Log.d("MainActivity", "No expenses found in storage")
        }
    }
    
    private fun saveExpenses() {
        val editor = sharedPreferences.edit()
        val json = gson.toJson(expenseList)
        editor.putString("expenses", json)
        editor.apply()
    }
    
    private fun saveAllExpenses() {
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
    
    private fun addExpenseItem() {
        // No-op: old form elements removed. Use modal dialog for adding expenses.
    }
    
    private fun editExpenseItem(position: Int) {
        val expense = expenseList[position]
        val dialogView = layoutInflater.inflate(R.layout.dialog_edit_expense, null)
        
        val editName = dialogView.findViewById<EditText>(R.id.editDialogName)
        val editPrice = dialogView.findViewById<EditText>(R.id.editDialogPrice)
        val editDescription = dialogView.findViewById<EditText>(R.id.editDialogDescription)
        val editDate = dialogView.findViewById<EditText>(R.id.editDialogDate)
        val editTime = dialogView.findViewById<EditText>(R.id.editDialogTime)
          editName.setText(expense.name)
        // Display the price in the current currency format for editing
        val displayAmount = currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        editPrice.setText(displayAmount.toString())
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
                }                // Use CurrencyManager for native currency storage
                val storageAmount = currencyManager.getStorageAmount(newPrice)
                val storageCurrency = currencyManager.getStorageCurrency()
                
                expense.name = newName
                expense.price = storageAmount
                expense.description = newDescription
                expense.date = newDate
                expense.time = newTime
                expense.currency = storageCurrency
                
                if (::expenseAdapter.isInitialized) {
                    expenseAdapter.notifyItemChanged(position)
                }
                saveAllExpenses() // Use saveAllExpenses to preserve deleted items
                
                // Refresh fragments and summary
                refreshAllFragments()
                
                Toast.makeText(this, languageManager.getString("expense_updated_successfully"), Toast.LENGTH_SHORT).show()
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
    }
      private fun deleteExpenseItem(position: Int) {
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
                    if (::expenseAdapter.isInitialized) {
                        expenseAdapter.notifyItemRemoved(position)
                        expenseAdapter.notifyItemRangeChanged(position, expenseList.size)
                    }
                    
                    // Save the deleted expense specifically
                    saveDeletedExpense(expenseToDelete)
                    
                    // Refresh fragments and summary
                    refreshAllFragments()
                    
                    Toast.makeText(this, "💾 Expense moved to history. Check History to restore.", Toast.LENGTH_LONG).show()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
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
            intent.putExtra("expense_currency", expense.currency)
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
    
    private fun updateTodaySummaryCard() {
        Log.d("MainActivity", "Updating Today's Summary Card")
        
        // Update Today's Summary card texts
        todaySummaryTitle.text = languageManager.getString("todays_summary")
        todayExpensesLabel.text = languageManager.getString("total_expenses")
        todayAmountLabel.text = languageManager.getString("total_amount")
        
        // Force refresh the card view
        todaySummaryCard.invalidate()
        todaySummaryCard.requestLayout()
        
        // Refresh the today's summary data to ensure proper display
        updateTodaySummary()
        
        Log.d("MainActivity", "Today's Summary Card updated: title=${todaySummaryTitle.text}, expenses=${todayExpensesLabel.text}, amount=${todayAmountLabel.text}")
    }
      private fun updateTabTitles() {
        Log.d("MainActivity", "Updating tab titles")
        
        // Refresh tab titles by updating each tab
        for (i in 0 until tabLayout.tabCount) {
            val tab = tabLayout.getTabAt(i)
            val newTitle = viewPagerAdapter.getTabTitle(i)
            tab?.text = newTitle
            Log.d("MainActivity", "Tab $i updated to: $newTitle")
        }
        
        // Force refresh the tab layout
        tabLayout.invalidate()
        tabLayout.requestLayout()
        
        Log.d("MainActivity", "Tab titles update completed")
    }
    
    private fun updateToolbarTitle() {
        Log.d("MainActivity", "Updating toolbar title")
        
        val newTitle = languageManager.getString("app_name")
        toolbar.title = newTitle
        supportActionBar?.title = newTitle
        
        // Force refresh the toolbar
        toolbar.invalidate()
        
        Log.d("MainActivity", "Toolbar title updated to: $newTitle")
    }
    
    private fun updateModalDialogTexts(dialogView: View, modalEditTextName: EditText, modalEditTextPrice: EditText, 
                                      modalEditTextDescription: EditText, modalEditTextDate: EditText, 
                                      modalEditTextTime: EditText, modalButtonSeeMore: TextView, 
                                      modalButtonAdd: Button, modalButtonCancel: Button, modalDialogTitle: TextView?) {
        // Update dialog title
        modalDialogTitle?.text = languageManager.getString("add_new_expense")
        
        // Update input field hints
        modalEditTextName.hint = languageManager.getString("expense_name_hint")
        modalEditTextPrice.hint = languageManager.getString("price_hint")
        modalEditTextDescription.hint = languageManager.getString("description_hint")
        modalEditTextDate.hint = languageManager.getString("date_hint")
        modalEditTextTime.hint = languageManager.getString("time_hint")
        
        // Update button texts
        modalButtonSeeMore.text = languageManager.getString("see_more")
        modalButtonAdd.text = languageManager.getString("add")
        modalButtonCancel.text = languageManager.getString("cancel")
    }
    
    private fun updateNavigationHeaderText() {
        Log.d("MainActivity", "Updating navigation header text")
        
        // Get the navigation header view
        val headerView = navigationView.getHeaderView(0)
        val headerTextView = headerView?.findViewById<TextView>(R.id.textView)
        
        headerTextView?.text = "📊 " + languageManager.getString("app_description")
        
        Log.d("MainActivity", "Navigation header text updated")
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                // Already in MainActivity, just close drawer
            }
            R.id.nav_summary -> {
                startActivity(Intent(this, SummaryActivity::class.java))
            }
            R.id.nav_all_list -> {
                val intent = Intent(this, AllListActivity::class.java)
                allListActivityLauncher.launch(intent)
            }
            R.id.nav_history -> {
                val intent = Intent(this, HistoryActivity::class.java)
                historyActivityLauncher.launch(intent)
            }            R.id.nav_currency_exchange -> {
                startActivity(Intent(this, CurrencyExchangeActivity::class.java))
            }
            R.id.nav_export_excel -> {
                startActivity(Intent(this, ExportActivity::class.java))
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.nav_feedback -> {
                startActivity(Intent(this, FeedbackActivity::class.java))
            }
            R.id.nav_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    @Suppress("DEPRECATION")
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
