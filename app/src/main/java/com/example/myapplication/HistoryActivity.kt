package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class HistoryActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {    private lateinit var recyclerView: RecyclerView
    private lateinit var historyAdapter: HistoryAdapter
    private val deletedExpenses = mutableListOf<ExpenseItem>()
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    
    // Navigation Drawer components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    
    // Selection components
    private lateinit var layoutSelectionControls: View
    private lateinit var checkboxSelectAll: CheckBox
    private lateinit var textViewSelectionCount: TextView
    private lateinit var buttonDeleteSelected: Button
    private lateinit var buttonToggleSelection: Button
    private lateinit var buttonCancelSelection: Button
    private var isSelectionMode = false
      override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_history)
        
        setupActionBar()
        initViews()
        setupStaticTexts()
        setupNavigationDrawer()
        setupSharedPreferences()
        setupRecyclerView()
        loadDeletedExpenses()
        updateNavigationMenuTitles()
        
        // Setup back press handling
        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (isSelectionMode) {
                    exitSelectionMode()
                } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                    drawerLayout.closeDrawer(GravityCompat.START)
                } else {
                    finish()
                }
            }
        })
    }
      private fun updateNavigationMenuTitles() {
        val menu = navigationView.menu
        menu.findItem(R.id.nav_home)?.title = languageManager.getString("nav_home")
        menu.findItem(R.id.nav_all_list)?.title = languageManager.getString("nav_all_list")
        menu.findItem(R.id.nav_history)?.title = languageManager.getString("nav_history")
        menu.findItem(R.id.nav_summary)?.title = languageManager.getString("nav_summary")
        menu.findItem(R.id.nav_currency_exchange)?.title = languageManager.getString("nav_currency_exchange")
        menu.findItem(R.id.nav_settings)?.title = languageManager.getString("nav_settings")
        menu.findItem(R.id.nav_feedback)?.title = languageManager.getString("nav_feedback")
        menu.findItem(R.id.nav_about)?.title = languageManager.getString("nav_about")
    }override fun onResume() {
        super.onResume()
        // Refresh deleted expenses list when activity resumes        // This ensures we always show the latest deleted data
        loadDeletedExpenses()
        // Refresh translations when activity resumes
        setupStaticTexts()
        updateNavigationMenuTitles()
        // Refresh button translations in adapter
        historyAdapter.refreshTranslations()
    }override fun onLanguageChanged() {
        super.onLanguageChanged()
        // Refresh all text elements when language changes
        setupStaticTexts()
        updateNavigationMenuTitles()
        // Refresh button translations in adapter
        historyAdapter.refreshTranslations()
    }
      private fun setupActionBar() {
        supportActionBar?.title = "🗃️ Deleted Items History"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupStaticTexts() {
        // Update action bar title
        supportActionBar?.title = languageManager.getString("deleted_items_history_title")
        
        // Update title TextView
        findViewById<TextView>(R.id.textViewTitle)?.text = languageManager.getString("deleted_items_history_title")
        
        // Update button texts
        updateSelectionModeTexts()
        
        // Update checkbox text
        checkboxSelectAll.text = languageManager.getString("select_all")
          // Update delete button text
        buttonDeleteSelected.text = languageManager.getString("delete_forever")
    }
    
    private fun updateSelectionModeTexts() {
        if (isSelectionMode) {
            buttonToggleSelection.text = languageManager.getString("selection_mode_on")
            supportActionBar?.title = languageManager.getString("deleted_items_history_title")
        } else {
            buttonToggleSelection.text = languageManager.getString("enable_selection")
            supportActionBar?.title = languageManager.getString("deleted_items_history_title")
        }
        buttonCancelSelection.text = languageManager.getString("cancel_selection")
    }
    
    private fun initViews() {        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        recyclerView = findViewById(R.id.recyclerViewHistory)
        
        // Selection components
        layoutSelectionControls = findViewById(R.id.layoutSelectionControls)
        checkboxSelectAll = findViewById(R.id.checkboxSelectAll)
        textViewSelectionCount = findViewById(R.id.textViewSelectionCount)
        buttonDeleteSelected = findViewById(R.id.buttonDeleteSelected)
        buttonToggleSelection = findViewById(R.id.buttonToggleSelection)
        buttonCancelSelection = findViewById(R.id.buttonCancelSelection)
        
        setupSelectionControls()
          // Setup back button click listener
        findViewById<android.widget.ImageButton>(R.id.buttonBack).setOnClickListener {
            finish()
        }
    }
    
    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, null,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        navigationView.setNavigationItemSelectedListener(this)
    }
      private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
    }
      private fun setupRecyclerView() {
        historyAdapter = HistoryAdapter(
            deletedExpenses,
            onRestoreClick = { position -> restoreExpenseItem(position) },
            onDeletePermanentlyClick = { position -> showDeletePermanentlyDialog(position) },
            onSelectionChanged = { selectedCount, isAllSelected ->
                updateSelectionUI(selectedCount, isAllSelected)
            },
            languageManager = languageManager
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
                .setTitle(languageManager.getString("restore_expense_title"))
                .setMessage(languageManager.getString("restore_expense_message").replace("{name}", expense.name))
                .setPositiveButton(languageManager.getString("restore_button")) { _, _ ->
                    // Mark as not deleted
                    expense.isDeleted = false
                    expense.deletedAt = null
                    
                    // Remove from deleted list
                    deletedExpenses.removeAt(position)
                    historyAdapter.notifyItemRemoved(position)
                    historyAdapter.notifyItemRangeChanged(position, deletedExpenses.size)
                    
                    // Save the restored expense specifically
                    saveRestoredExpense(expense)
                    
                    Toast.makeText(this, languageManager.getString("restore_success"), Toast.LENGTH_SHORT).show()
                    
                    // Set result to notify MainActivity to refresh
                    setResult(RESULT_OK)
                }
                .setNegativeButton(languageManager.getString("cancel"), null)
                .show()
        }
    }
      private fun showDeletePermanentlyDialog(position: Int) {
        if (position >= 0 && position < deletedExpenses.size) {
            val expense = deletedExpenses[position]
            
            AlertDialog.Builder(this)
                .setTitle(languageManager.getString("delete_permanently_title"))
                .setMessage(languageManager.getString("delete_permanently_message").replace("{name}", expense.name))
                .setPositiveButton(languageManager.getString("delete_permanently_button")) { _, _ ->
                    deletePermanently(position)
                }
                .setNegativeButton(languageManager.getString("cancel"), null)
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
            
            // Remove from all expenses permanently
            removeExpensePermanently(expense.id)
            
            Toast.makeText(this, languageManager.getString("delete_permanently_success").replace("{count}", "1"), Toast.LENGTH_SHORT).show()
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
        sharedPreferences.edit().putString("expenses", updatedExpensesJson).apply()    }
    
    private fun setupSelectionControls() {
        // Toggle Selection button - Enter/Exit selection mode
        buttonToggleSelection.setOnClickListener {
            toggleSelectionMode()
        }
        
        // Cancel Selection button - Exit selection mode without action
        buttonCancelSelection.setOnClickListener {
            exitSelectionMode()
        }
        
        // Select All checkbox
        checkboxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (historyAdapter.isSelectionMode()) {
                if (isChecked) {
                    historyAdapter.selectAll()
                } else {
                    historyAdapter.clearSelection()
                }
                updateSelectionUI(historyAdapter.getSelectedCount(), historyAdapter.isAllSelected())
            }
        }
          // Delete Forever button
        buttonDeleteSelected.setOnClickListener {
            deleteSelectedItemsForever()
        }
        
        // Initially hide selection controls
        layoutSelectionControls.visibility = View.GONE
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
        historyAdapter.setSelectionMode(true)
        layoutSelectionControls.visibility = View.VISIBLE
        buttonToggleSelection.text = languageManager.getString("selection_mode_on")
        supportActionBar?.title = languageManager.getString("deleted_items_history_title")
        updateSelectionUI(0, false)
    }
    
    private fun exitSelectionMode() {
        isSelectionMode = false
        historyAdapter.setSelectionMode(false)
        layoutSelectionControls.visibility = View.GONE
        buttonToggleSelection.text = languageManager.getString("enable_selection")
        supportActionBar?.title = languageManager.getString("deleted_items_history_title")
    }
      private fun updateSelectionUI(selectedCount: Int, isAllSelected: Boolean) {
        textViewSelectionCount.text = languageManager.getString("selection_count").replace("{count}", selectedCount.toString())
        
        // Update "Select All" checkbox without triggering listener
        checkboxSelectAll.setOnCheckedChangeListener(null)
        checkboxSelectAll.isChecked = isAllSelected
        checkboxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (historyAdapter.isSelectionMode()) {
                if (isChecked) {
                    historyAdapter.selectAll()
                } else {
                    historyAdapter.clearSelection()
                }
                updateSelectionUI(historyAdapter.getSelectedCount(), historyAdapter.isAllSelected())
            }
        }
          // Enable/disable delete button based on selection
        buttonDeleteSelected.isEnabled = selectedCount > 0
    }
    
    private fun deleteSelectedItemsForever() {
        val selectedIndices = historyAdapter.getSelectedItems()
        if (selectedIndices.isEmpty()) return
        
        val selectedItems = selectedIndices.map { deletedExpenses[it] }
        val itemNames = selectedItems.joinToString(", ") { it.name }
        
        AlertDialog.Builder(this)
            .setTitle("⚠️ Delete Forever")
            .setMessage("Are you sure you want to permanently delete these ${selectedIndices.size} item(s)?\n\n$itemNames\n\nThis action cannot be undone.")
            .setPositiveButton("🗑️ Delete Forever") { _, _ ->
                performMultiplePermanentDelete(selectedIndices.toList().sortedDescending())
            }
            .setNegativeButton("❌ Cancel", null)
            .show()
    }
    
    private fun performMultiplePermanentDelete(indices: List<Int>) {
        val expenseIds = mutableListOf<Long>()
        
        // Collect expense IDs and remove from local list in reverse order
        indices.forEach { index ->
            expenseIds.add(deletedExpenses[index].id)
            deletedExpenses.removeAt(index)
        }
        
        // Remove from shared preferences
        expenseIds.forEach { id ->
            removeExpensePermanently(id)
        }
        
        // Update UI
        historyAdapter.notifyDataSetChanged()
        exitSelectionMode()
          // Show success message
        val deletedCount = indices.size
        Toast.makeText(
            this,
            languageManager.getString("delete_permanently_success").replace("{count}", deletedCount.toString()),
            Toast.LENGTH_SHORT
        ).show()
    }
    
    private fun applyTheme() {
        val themePrefs = getSharedPreferences(ThemeActivity.THEME_PREFS, Context.MODE_PRIVATE)
        val savedTheme = themePrefs.getString(ThemeActivity.THEME_KEY, ThemeActivity.THEME_SYSTEM)
        
        when (savedTheme) {
            ThemeActivity.THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeActivity.THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            ThemeActivity.THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this, MainActivity::class.java))
            }            R.id.nav_summary -> {
                startActivity(Intent(this, SummaryActivity::class.java))
            }
            R.id.nav_all_list -> {
                startActivity(Intent(this, AllListActivity::class.java))
            }
            R.id.nav_history -> {
                // Already in this activity, just close drawer
            }
            R.id.nav_currency_exchange -> {
                startActivity(Intent(this, CurrencyExchangeActivity::class.java))
            }            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.nav_feedback -> {
                startActivity(Intent(this, FeedbackActivity::class.java))
            }            R.id.nav_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }
            else -> {
                // Handle unknown menu items
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}

class HistoryAdapter(
    private val deletedExpenses: MutableList<ExpenseItem>,
    private val onRestoreClick: (Int) -> Unit,
    private val onDeletePermanentlyClick: (Int) -> Unit,
    private val onSelectionChanged: (Int, Boolean) -> Unit,
    private val languageManager: LanguageManager
) : RecyclerView.Adapter<HistoryAdapter.HistoryViewHolder>() {

    private lateinit var currencyManager: CurrencyManager
    private var selectionMode = false
    private val selectedItems = mutableSetOf<Int>()

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

    class HistoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val checkboxSelect: CheckBox = view.findViewById(R.id.checkboxSelect)
        val textViewName: TextView = view.findViewById(R.id.textViewName)
        val textViewPrice: TextView = view.findViewById(R.id.textViewPrice)
        val textViewDescription: TextView = view.findViewById(R.id.textViewDescription)
        val textViewDateTime: TextView = view.findViewById(R.id.textViewDateTime)
        val textViewDeletedDate: TextView = view.findViewById(R.id.textViewDeletedDate)
        val buttonRestore: Button = view.findViewById(R.id.buttonRestore)
        val buttonDeletePermanently: Button = view.findViewById(R.id.buttonDeletePermanently)
    }
    
    fun setSelectionMode(enabled: Boolean) {
        selectionMode = enabled
        if (!enabled) {
            selectedItems.clear()
        }
        notifyDataSetChanged()
    }
    
    fun isSelectionMode(): Boolean = selectionMode
    
    fun getSelectedItems(): Set<Int> = selectedItems.toSet()
    
    fun getSelectedCount(): Int = selectedItems.size
    
    fun selectAll() {
        selectedItems.clear()
        selectedItems.addAll(0 until deletedExpenses.size)
        notifyDataSetChanged()
    }
    
    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
    }
    
    fun isAllSelected(): Boolean = selectedItems.size == deletedExpenses.size && deletedExpenses.isNotEmpty()
    
    // Method to refresh button translations when language changes
    fun refreshTranslations() {
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HistoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_history, parent, false)
        
        // Initialize CurrencyManager if not already done
        if (!::currencyManager.isInitialized) {
            currencyManager = CurrencyManager.getInstance(parent.context)
        }
        
        return HistoryViewHolder(view)
    }    override fun onBindViewHolder(holder: HistoryViewHolder, position: Int) {
        val expense = deletedExpenses[position]
        
        // Handle selection checkbox
        holder.checkboxSelect.visibility = if (selectionMode) View.VISIBLE else View.GONE
        holder.checkboxSelect.isChecked = selectedItems.contains(position)
        
        holder.checkboxSelect.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                selectedItems.add(position)
            } else {
                selectedItems.remove(position)
            }
            onSelectionChanged(selectedItems.size, isAllSelected())
        }
        
        // Handle item click for selection
        holder.itemView.setOnClickListener {
            if (selectionMode) {
                holder.checkboxSelect.isChecked = !holder.checkboxSelect.isChecked
            }
        }
          holder.textViewName.text = expense.name
        
        // Use CurrencyManager's new method for display
        val displayAmount = currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        holder.textViewPrice.text = currencyManager.formatCurrency(displayAmount)
          holder.textViewDescription.text = if (expense.description.isNotEmpty()) expense.description else "No description"
        
        // Convert time to 12-hour format with AM/PM
        val formattedTime = convertTo12HourFormat(expense.time)
        holder.textViewDateTime.text = "${expense.date} at $formattedTime"
        
        holder.textViewDeletedDate.text = "Deleted on ${expense.deletedAt ?: "Unknown date"}"
          // Hide/show action buttons based on selection mode
        val buttonVisibility = if (selectionMode) View.GONE else View.VISIBLE
        holder.buttonRestore.visibility = buttonVisibility
        holder.buttonDeletePermanently.visibility = buttonVisibility
        
        // Set translated button text
        holder.buttonRestore.text = languageManager.getString("restore_button")
        holder.buttonDeletePermanently.text = languageManager.getString("delete_permanently_button")
        
        holder.buttonRestore.setOnClickListener {
            onRestoreClick(position)
        }
        
        holder.buttonDeletePermanently.setOnClickListener {
            onDeletePermanentlyClick(position)
        }
    }

    override fun getItemCount(): Int = deletedExpenses.size
}
