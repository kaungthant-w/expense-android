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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.*

class AllListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var allListAdapter: AllListAdapter
    private val allExpenses = mutableListOf<ExpenseItem>()
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var languageManager: LanguageManager
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
    
    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_list)
          languageManager = LanguageManager.getInstance(this)
        setupActionBar()
        initViews()
        setupStaticTexts()
        setupNavigationDrawer()
        setupSharedPreferences()
        setupRecyclerView()
        loadAllExpenses()
        updateNavigationMenuTitles()
    }
    
    override fun onResume() {
        super.onResume()
        // Refresh translations when activity resumes
        setupStaticTexts()
        updateNavigationMenuTitles()
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
      private fun setupActionBar() {
        supportActionBar?.title = languageManager.getString("all_list_title")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupStaticTexts() {
        // Update action bar title
        supportActionBar?.title = languageManager.getString("all_list_title")
        
        // Update title TextView
        findViewById<TextView>(R.id.textViewTitle)?.text = languageManager.getString("all_expenses_active_title")
        
        // Update history button text
        findViewById<Button>(R.id.buttonViewHistory)?.text = languageManager.getString("view_history")
        
        // Update button texts
        updateSelectionModeTexts()
        
        // Update checkbox text
        checkboxSelectAll.text = languageManager.getString("select_all")
        
        // Update delete button text
        buttonDeleteSelected.text = languageManager.getString("delete_selected")
    }
    
    private fun updateSelectionModeTexts() {
        if (isSelectionMode) {
            buttonToggleSelection.text = languageManager.getString("selection_mode_on")
        } else {
            buttonToggleSelection.text = languageManager.getString("toggle_selection")
        }
        buttonCancelSelection.text = languageManager.getString("cancel_selection")
    }
    
    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        recyclerView = findViewById(R.id.recyclerViewAllList)
        
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
        
        // Setup history button click listener
        findViewById<Button>(R.id.buttonViewHistory).setOnClickListener {
            navigateToHistory()
        }
    }
    
    private fun navigateToHistory() {
        val intent = Intent(this, HistoryActivity::class.java)
        startActivity(intent)
    }
    
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
            if (allListAdapter.isSelectionMode()) {
                if (isChecked) {
                    allListAdapter.selectAll()
                } else {
                    allListAdapter.clearSelection()
                }
                updateSelectionUI(allListAdapter.getSelectedCount(), allListAdapter.isAllSelected())
            }
        }
        
        // Delete Selected button
        buttonDeleteSelected.setOnClickListener {
            deleteSelectedItems()
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
        allListAdapter.setSelectionMode(true)
        layoutSelectionControls.visibility = View.VISIBLE
        buttonToggleSelection.text = languageManager.getString("selection_mode_on")
        supportActionBar?.title = languageManager.getString("all_list_title")
        updateSelectionUI(0, false)
    }
    
    private fun exitSelectionMode() {
        isSelectionMode = false
        allListAdapter.setSelectionMode(false)
        layoutSelectionControls.visibility = View.GONE
        buttonToggleSelection.text = languageManager.getString("toggle_selection")
        supportActionBar?.title = languageManager.getString("all_list_title")
    }
      private fun updateSelectionUI(selectedCount: Int, isAllSelected: Boolean) {
        textViewSelectionCount.text = languageManager.getString("selection_count").replace("{count}", selectedCount.toString())
        
        // Update "Select All" checkbox without triggering listener
        checkboxSelectAll.setOnCheckedChangeListener(null)
        checkboxSelectAll.isChecked = isAllSelected
        checkboxSelectAll.setOnCheckedChangeListener { _, isChecked ->
            if (allListAdapter.isSelectionMode()) {
                if (isChecked) {
                    allListAdapter.selectAll()
                } else {
                    allListAdapter.clearSelection()
                }
                updateSelectionUI(allListAdapter.getSelectedCount(), allListAdapter.isAllSelected())
            }
        }
        
        // Enable/disable delete button based on selection
        buttonDeleteSelected.isEnabled = selectedCount > 0
    }
    
    private fun deleteSelectedItems() {
        val selectedIndices = allListAdapter.getSelectedItems()
        if (selectedIndices.isEmpty()) return
        
        val selectedItems = selectedIndices.map { allExpenses[it] }
        val itemNames = selectedItems.joinToString(", ") { it.name }
        
        AlertDialog.Builder(this)
            .setTitle(languageManager.getString("all_list_delete_dialog_title"))
            .setMessage(languageManager.getString("all_list_delete_dialog_message")
                .replace("{count}", selectedIndices.size.toString())
                .replace("{items}", itemNames))
            .setPositiveButton(languageManager.getString("all_list_delete_confirm")) { _, _ ->
                performMultipleSoftDelete(selectedIndices.toList())
            }
            .setNegativeButton(languageManager.getString("all_list_delete_cancel"), null)
            .show()
    }
    
    private fun performMultipleSoftDelete(indices: List<Int>) {
        val currentDateTime = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
        
        // Get the actual expenses from the original data source
        val expensesJson = sharedPreferences.getString("expenses", "[]")
        val type = object : TypeToken<MutableList<ExpenseItem>>() {}.type
        val allStoredExpenses: MutableList<ExpenseItem> = gson.fromJson(expensesJson, type) ?: mutableListOf()
        
        // Mark items as deleted in the stored data
        val itemsToDelete = indices.map { allExpenses[it] }
        itemsToDelete.forEach { itemToDelete ->
            val storedItem = allStoredExpenses.find { it.name == itemToDelete.name && it.date == itemToDelete.date && it.time == itemToDelete.time }
            storedItem?.let {
                it.isDeleted = true
                it.deletedAt = currentDateTime
            }
        }
        
        // Save updated expenses to storage
        val updatedExpensesJson = gson.toJson(allStoredExpenses)
        sharedPreferences.edit().putString("expenses", updatedExpensesJson).apply()
        
        // Reload the filtered list (only active items)
        loadAllExpenses()
        exitSelectionMode()
        
        // Show success message with option to view history
        val deletedCount = indices.size
        val message = languageManager.getString("all_list_delete_success").replace("{count}", deletedCount.toString())
        
        val toast = android.widget.Toast.makeText(this, message, android.widget.Toast.LENGTH_LONG)
        toast.show()
        
        // Optional: Show a snackbar with action to view history
        showHistorySnackbar(deletedCount)
    }
    
    private fun showHistorySnackbar(deletedCount: Int) {
        // Create a simple dialog asking if user wants to view history
        AlertDialog.Builder(this)
            .setTitle(languageManager.getString("all_list_items_deleted_title"))
            .setMessage(languageManager.getString("all_list_items_deleted_message").replace("{count}", deletedCount.toString()))
            .setPositiveButton(languageManager.getString("all_list_view_history")) { _, _ ->
                navigateToHistory()
            }
            .setNegativeButton(languageManager.getString("all_list_continue"), null)
            .show()
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (isSelectionMode) {
            exitSelectionMode()
        } else if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
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
    
    private fun updateNavigationMenuTitles() {
        val menu = navigationView.menu
        menu.findItem(R.id.nav_home)?.title = languageManager.getString("nav_home")
        menu.findItem(R.id.nav_all_list)?.title = languageManager.getString("nav_all_list")
        menu.findItem(R.id.nav_history)?.title = languageManager.getString("nav_history")
        menu.findItem(R.id.nav_summary)?.title = languageManager.getString("nav_summary")
        menu.findItem(R.id.nav_analytics)?.title = languageManager.getString("nav_analytics")
        menu.findItem(R.id.nav_currency_exchange)?.title = languageManager.getString("nav_currency_exchange")
        menu.findItem(R.id.nav_settings)?.title = languageManager.getString("nav_settings")
        menu.findItem(R.id.nav_feedback)?.title = languageManager.getString("nav_feedback")
        menu.findItem(R.id.nav_about)?.title = languageManager.getString("nav_about")
    }
    
    private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
    }
    
    private fun setupRecyclerView() {
        allListAdapter = AllListAdapter(allExpenses) { selectedCount, isAllSelected ->
            updateSelectionUI(selectedCount, isAllSelected)
        }
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = allListAdapter
    }
    
    private fun loadAllExpenses() {
        val expensesJson = sharedPreferences.getString("expenses", "[]")
        val type = object : TypeToken<List<ExpenseItem>>() {}.type
        val expenses: List<ExpenseItem> = gson.fromJson(expensesJson, type) ?: emptyList()
        
        allExpenses.clear()
        // Only show active (non-deleted) expenses on All List screen
        allExpenses.addAll(expenses.filter { !it.isDeleted })
        allListAdapter.notifyDataSetChanged()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.nav_summary -> {
                startActivity(Intent(this, SummaryActivity::class.java))
            }
            R.id.nav_analytics -> {
                startActivity(Intent(this, AnalyticsActivity::class.java))
            }
            R.id.nav_all_list -> {
                // Already in this activity, just close drawer
            }
            R.id.nav_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
            }
            R.id.nav_currency_exchange -> {
                startActivity(Intent(this, CurrencyExchangeActivity::class.java))
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
            else -> {
                // Handle any other menu items
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}

class AllListAdapter(
    private val expenses: List<ExpenseItem>,
    private val onSelectionChanged: (Int, Boolean) -> Unit
) : RecyclerView.Adapter<AllListAdapter.AllListViewHolder>() {
    
    private lateinit var currencyManager: CurrencyManager
    private lateinit var languageManager: LanguageManager
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
    
    class AllListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val checkboxSelect: CheckBox = itemView.findViewById(R.id.checkboxSelect)
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        val textViewDateTime: TextView = itemView.findViewById(R.id.textViewDateTime)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
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
        selectedItems.addAll(0 until expenses.size)
        notifyDataSetChanged()
        onSelectionChanged(selectedItems.size, isAllSelected())
    }
    
    fun clearSelection() {
        selectedItems.clear()
        notifyDataSetChanged()
        onSelectionChanged(selectedItems.size, isAllSelected())
    }
    
    fun isAllSelected(): Boolean = selectedItems.size == expenses.size && expenses.isNotEmpty()
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_all_list, parent, false)
        
        // Initialize CurrencyManager if not already done
        if (!::currencyManager.isInitialized) {
            currencyManager = CurrencyManager.getInstance(parent.context)
        }
        
        // Initialize LanguageManager if not already done
        if (!::languageManager.isInitialized) {
            languageManager = LanguageManager.getInstance(parent.context)
        }
        
        return AllListViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: AllListViewHolder, position: Int) {
        val expense = expenses[position]
        
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
        
        holder.textViewDescription.text = if (expense.description.isNotEmpty()) {
            expense.description
        } else {
            languageManager.getString("all_list_no_description")
        }
        
        holder.textViewDateTime.text = "📅 ${expense.date} • 🕐 ${convertTo12HourFormat(expense.time)}"
        
        // Since we only show active items, set consistent styling
        holder.textViewStatus.text = languageManager.getString("all_list_status_active")
        holder.textViewStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
        holder.itemView.alpha = 1.0f
    }
    
    override fun getItemCount(): Int = expenses.size
}
