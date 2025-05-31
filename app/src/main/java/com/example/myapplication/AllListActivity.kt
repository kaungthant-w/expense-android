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
import android.widget.TextView
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
import java.util.*

class AllListActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var recyclerView: RecyclerView
    private lateinit var allListAdapter: AllListAdapter
    private val allExpenses = mutableListOf<ExpenseItem>()
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
      // Navigation Drawer components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_all_list)
        
        setupActionBar()
        initViews()
        setupNavigationDrawer()
        setupSharedPreferences()
        setupRecyclerView()
        loadAllExpenses()
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
        supportActionBar?.title = "📋 All Expenses"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        recyclerView = findViewById(R.id.recyclerViewAllList)
        
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
        allListAdapter = AllListAdapter(allExpenses)
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = allListAdapter
    }
    
    private fun loadAllExpenses() {
        val expensesJson = sharedPreferences.getString("expenses", "[]")
        val type = object : TypeToken<List<ExpenseItem>>() {}.type
        val expenses: List<ExpenseItem> = gson.fromJson(expensesJson, type) ?: emptyList()
        
        allExpenses.clear()
        allExpenses.addAll(expenses) // Load ALL expenses (both active and deleted)
        allListAdapter.notifyDataSetChanged()    }
    
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
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}

class AllListAdapter(private val expenses: List<ExpenseItem>) : RecyclerView.Adapter<AllListAdapter.AllListViewHolder>() {
    
    private lateinit var currencyManager: CurrencyManager
    
    class AllListViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val textViewName: TextView = itemView.findViewById(R.id.textViewName)
        val textViewPrice: TextView = itemView.findViewById(R.id.textViewPrice)
        val textViewDescription: TextView = itemView.findViewById(R.id.textViewDescription)
        val textViewDateTime: TextView = itemView.findViewById(R.id.textViewDateTime)
        val textViewStatus: TextView = itemView.findViewById(R.id.textViewStatus)
    }
      override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AllListViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_all_list, parent, false)
        
        // Initialize CurrencyManager if not already done
        if (!::currencyManager.isInitialized) {
            currencyManager = CurrencyManager.getInstance(parent.context)
        }
        
        return AllListViewHolder(view)
    }
      override fun onBindViewHolder(holder: AllListViewHolder, position: Int) {
        val expense = expenses[position]
        
        holder.textViewName.text = expense.name
        
        // Format price with currency using CurrencyManager
        val currentCurrency = currencyManager.getCurrentCurrency()
        val displayAmount = if (currentCurrency == CurrencyManager.CURRENCY_MMK) {
            currencyManager.convertFromUsd(expense.price)
        } else {
            expense.price
        }
        holder.textViewPrice.text = currencyManager.formatCurrency(displayAmount)
        
        holder.textViewDescription.text = if (expense.description.isNotEmpty()) {
            expense.description
        } else {
            "No description"
        }
        
        holder.textViewDateTime.text = "📅 ${expense.date} • 🕐 ${expense.time}"
        
        // Show status (Active or Deleted)
        if (expense.isDeleted) {
            holder.textViewStatus.text = "🗑️ DELETED (${expense.deletedAt})"
            holder.textViewStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_red_dark))
            holder.itemView.alpha = 0.6f // Make deleted items slightly transparent
        } else {
            holder.textViewStatus.text = "✅ ACTIVE"
            holder.textViewStatus.setTextColor(holder.itemView.context.getColor(android.R.color.holo_green_dark))
            holder.itemView.alpha = 1.0f
        }
    }
    
    override fun getItemCount(): Int = expenses.size
}
