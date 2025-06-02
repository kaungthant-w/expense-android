package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
      private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currencyManager: CurrencyManager
    private lateinit var languageManager: LanguageManager
    private val gson = Gson()
    
    // Navigation Drawer components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
      override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)
        
        languageManager = LanguageManager.getInstance(this)
        setupActionBar()
        initViews()
        setupNavigationDrawer()
        setupSharedPreferences()
        loadAnalyticsData()
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
        supportActionBar?.title = languageManager.getString("analytics_title")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
      private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        
        // Setup back button click listener
        findViewById<android.widget.ImageButton>(R.id.buttonBack).setOnClickListener {
            finish()
        }
        
        // Set all static text elements using LanguageManager
        setupStaticTexts()
    }
    
    private fun setupStaticTexts() {
        // Find and set all static text elements
        try {            // Main title
            findViewById<TextView>(R.id.textAnalyticsTitle)?.text = 
                languageManager.getString("analytics_title")
            
            // Weekly Analysis section
            findViewById<TextView>(R.id.textWeeklyAnalysisTitle)?.text = 
                languageManager.getString("analytics_weekly_analysis")
            findViewById<TextView>(R.id.textThisWeekExpensesLabel)?.text = 
                languageManager.getString("analytics_this_week_expenses")
            findViewById<TextView>(R.id.textThisWeekTotalLabel)?.text = 
                languageManager.getString("analytics_this_week_total")
            findViewById<TextView>(R.id.textAveragePerDayLabel)?.text = 
                languageManager.getString("analytics_average_per_day")
            
            // Day of Week Analysis section
            findViewById<TextView>(R.id.textDayOfWeekAnalysisTitle)?.text = 
                languageManager.getString("analytics_day_of_week_analysis")
            findViewById<TextView>(R.id.textMostExpensiveDayLabel)?.text = 
                languageManager.getString("analytics_most_expensive_day_label")
            findViewById<TextView>(R.id.textLeastExpensiveDayLabel)?.text = 
                languageManager.getString("analytics_least_expensive_day_label")
            
            // Time of Day Analysis section
            findViewById<TextView>(R.id.textTimeOfDayAnalysisTitle)?.text = 
                languageManager.getString("analytics_time_of_day_analysis")
            findViewById<TextView>(R.id.textMorningLabel)?.text = 
                languageManager.getString("analytics_morning_label")
            findViewById<TextView>(R.id.textAfternoonLabel)?.text = 
                languageManager.getString("analytics_afternoon_label")
            findViewById<TextView>(R.id.textEveningLabel)?.text = 
                languageManager.getString("analytics_evening_label")
            findViewById<TextView>(R.id.textNightLabel)?.text = 
                languageManager.getString("analytics_night_label")
            
            // Top Expenses section
            findViewById<TextView>(R.id.textTopExpensesTitle)?.text = 
                languageManager.getString("analytics_top_expenses")
                
        } catch (e: Exception) {
            // Handle any missing views gracefully
            e.printStackTrace()
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
        currencyManager = CurrencyManager.getInstance(this)
    }
    
    private fun loadAnalyticsData() {
        val expensesJson = sharedPreferences.getString("expenses", "[]")
        val type = object : TypeToken<List<ExpenseItem>>() {}.type
        val expensesList: List<ExpenseItem> = gson.fromJson(expensesJson, type) ?: emptyList()
        
        displayAnalytics(expensesList)
    }
    
    override fun onResume() {
        super.onResume()
        // Update static texts when resuming (in case language was changed)
        setupStaticTexts()
        // Refresh analytics data to update with new language
        loadAnalyticsData()
    }
    
    private fun displayAnalytics(expenses: List<ExpenseItem>) {
        // Calculate spending by day of week
        val dayOfWeekExpenses = mutableMapOf<String, Double>()
        val dayOfWeekCounts = mutableMapOf<String, Int>()
        
        expenses.forEach { expense ->
            try {
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(expense.date)
                val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(date!!)
                
                // Use display amount instead of stored amount
                val displayAmount = currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
                dayOfWeekExpenses[dayOfWeek] = (dayOfWeekExpenses[dayOfWeek] ?: 0.0) + displayAmount
                dayOfWeekCounts[dayOfWeek] = (dayOfWeekCounts[dayOfWeek] ?: 0) + 1
            } catch (e: Exception) {
                // Skip invalid dates
            }
        }
        
        // Find most expensive day
        val mostExpensiveDay = dayOfWeekExpenses.maxByOrNull { it.value }
        val leastExpensiveDay = dayOfWeekExpenses.minByOrNull { it.value }
        
        // Calculate spending patterns
        val morningExpenses = expenses.filter { 
            val hour = it.time.substring(0, 2).toIntOrNull() ?: 0
            hour in 6..11
        }
        val afternoonExpenses = expenses.filter { 
            val hour = it.time.substring(0, 2).toIntOrNull() ?: 0
            hour in 12..17
        }
        val eveningExpenses = expenses.filter { 
            val hour = it.time.substring(0, 2).toIntOrNull() ?: 0
            hour in 18..23
        }
        val nightExpenses = expenses.filter { 
            val hour = it.time.substring(0, 2).toIntOrNull() ?: 0
            hour in 0..5
        }
        
        // Calculate weekly spending
        val calendar = Calendar.getInstance()
        val thisWeekExpenses = expenses.filter { expense ->
            try {
                val expenseDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(expense.date)
                val expenseCalendar = Calendar.getInstance()
                expenseCalendar.time = expenseDate!!
                
                val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
                val expenseWeek = expenseCalendar.get(Calendar.WEEK_OF_YEAR)
                val currentYear = calendar.get(Calendar.YEAR)
                val expenseYear = expenseCalendar.get(Calendar.YEAR)
                
                currentWeek == expenseWeek && currentYear == expenseYear
            } catch (e: Exception) {
                false
            }
        }
          // Update UI
        findViewById<TextView>(R.id.textMostExpensiveDay).text = 
            if (mostExpensiveDay != null) {
                // Amount is already in display currency, no conversion needed
                languageManager.getString("analytics_most_expensive_day")
                    .replace("{day}", mostExpensiveDay.key)
                    .replace("{amount}", currencyManager.formatCurrency(mostExpensiveDay.value))
            } else languageManager.getString("analytics_no_data")
            
        findViewById<TextView>(R.id.textLeastExpensiveDay).text = 
            if (leastExpensiveDay != null) {
                // Amount is already in display currency, no conversion needed
                languageManager.getString("analytics_least_expensive_day")
                    .replace("{day}", leastExpensiveDay.key)
                    .replace("{amount}", currencyManager.formatCurrency(leastExpensiveDay.value))
            } else languageManager.getString("analytics_no_data")
          val morningTotal = morningExpenses.sumOf { expense ->
            currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        }
        findViewById<TextView>(R.id.textMorningExpenses).text = 
            languageManager.getString("analytics_morning_expenses")
                .replace("{count}", morningExpenses.size.toString())
                .replace("{amount}", currencyManager.formatCurrency(morningTotal))
            
        val afternoonTotal = afternoonExpenses.sumOf { expense ->
            currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        }
        findViewById<TextView>(R.id.textAfternoonExpenses).text = 
            languageManager.getString("analytics_afternoon_expenses")
                .replace("{count}", afternoonExpenses.size.toString())
                .replace("{amount}", currencyManager.formatCurrency(afternoonTotal))
            
        val eveningTotal = eveningExpenses.sumOf { expense ->
            currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        }
        findViewById<TextView>(R.id.textEveningExpenses).text = 
            languageManager.getString("analytics_evening_expenses")
                .replace("{count}", eveningExpenses.size.toString())
                .replace("{amount}", currencyManager.formatCurrency(eveningTotal))
            
        val nightTotal = nightExpenses.sumOf { expense ->
            currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        }
        findViewById<TextView>(R.id.textNightExpenses).text = 
            languageManager.getString("analytics_night_expenses")
                .replace("{count}", nightExpenses.size.toString())
                .replace("{amount}", currencyManager.formatCurrency(nightTotal))
        
        findViewById<TextView>(R.id.textThisWeekExpenses).text = thisWeekExpenses.size.toString()
        val weekTotal = thisWeekExpenses.sumOf { expense ->
            currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        }
        findViewById<TextView>(R.id.textThisWeekAmount).text = currencyManager.formatCurrency(weekTotal)
        
        // Average expense per day
        val averagePerDay = if (expenses.isNotEmpty()) {
            val uniqueDates = expenses.map { it.date }.toSet()
            expenses.sumOf { expense ->
                currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
            } / uniqueDates.size
        } else 0.0
        
        findViewById<TextView>(R.id.textAveragePerDay).text = currencyManager.formatCurrency(averagePerDay)
          // Show top 3 most expensive expenses
        val topExpenses = expenses.sortedByDescending { expense ->
            currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        }.take(3)
        val topExpensesText = if (topExpenses.isNotEmpty()) {
            topExpenses.mapIndexed { index, expense -> 
                val displayAmount = currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
                "${index + 1}. ${expense.name}: ${currencyManager.formatCurrency(displayAmount)}"
            }.joinToString("\n")
        } else languageManager.getString("analytics_no_expenses")
        
        findViewById<TextView>(R.id.textTopExpenses).text = topExpensesText
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
                // Already in this activity, just close drawer
            }
            R.id.nav_all_list -> {
                startActivity(Intent(this, AllListActivity::class.java))
            }
            R.id.nav_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
            }
            R.id.nav_currency_exchange -> {
                startActivity(Intent(this, CurrencyExchangeActivity::class.java))
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }            R.id.nav_feedback -> {
                startActivity(Intent(this, FeedbackActivity::class.java))
            }
            R.id.nav_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
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
