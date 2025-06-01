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
import java.util.concurrent.TimeUnit

class SummaryActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currencyManager: CurrencyManager
    private val gson = Gson()
      // Navigation Drawer components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
      override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)
        
        setupActionBar()
        initViews()
        setupNavigationDrawer()
        setupSharedPreferences()
        loadSummaryData()
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
        supportActionBar?.title = "📊 Expense Summary"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
      private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        
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
        currencyManager = CurrencyManager.getInstance(this)
    }
    
    private fun loadSummaryData() {
        val expensesJson = sharedPreferences.getString("expenses", "[]")
        val type = object : TypeToken<List<ExpenseItem>>() {}.type
        val expensesList: List<ExpenseItem> = gson.fromJson(expensesJson, type) ?: emptyList()
        
        displaySummary(expensesList)
    }    private fun displaySummary(expenses: List<ExpenseItem>) {
        val totalExpenses = expenses.size
        
        // Calculate totals using display amounts to handle mixed currencies properly
        val totalAmount = expenses.sumOf { expense ->
            currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        }
        val averageAmount = if (totalExpenses > 0) totalAmount / totalExpenses else 0.0
        
        // Calculate today's expenses
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val todayExpenses = expenses.filter { it.date == today }
        val todayAmount = todayExpenses.sumOf { expense ->
            currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        }
        
        // Calculate this month's expenses
        val currentMonth = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
        val monthExpenses = expenses.filter { 
            val expenseMonth = it.date.substring(3) // Get MM/yyyy part
            expenseMonth == currentMonth
        }
        val monthAmount = monthExpenses.sumOf { expense ->
            currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        }
        
        // Find highest and lowest expense based on display amounts
        val highestExpense = expenses.maxByOrNull { expense ->
            currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        }
        val lowestExpense = expenses.minByOrNull { expense ->
            currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        }
          // Amounts are already in display currency, no need for conversion
        val displayTotalAmount = totalAmount
        val displayAverageAmount = averageAmount
        val displayTodayAmount = todayAmount
        val displayMonthAmount = monthAmount
        
        // Update UI
        findViewById<TextView>(R.id.textTotalExpenses).text = totalExpenses.toString()
        findViewById<TextView>(R.id.textTotalAmount).text = currencyManager.formatCurrency(displayTotalAmount)
        findViewById<TextView>(R.id.textAverageAmount).text = currencyManager.formatCurrency(displayAverageAmount)
        
        findViewById<TextView>(R.id.textTodayExpenses).text = todayExpenses.size.toString()
        findViewById<TextView>(R.id.textTodayAmount).text = currencyManager.formatCurrency(displayTodayAmount)
        
        findViewById<TextView>(R.id.textMonthExpenses).text = monthExpenses.size.toString()
        findViewById<TextView>(R.id.textMonthAmount).text = currencyManager.formatCurrency(displayMonthAmount)
          findViewById<TextView>(R.id.textHighestExpense).text = 
            if (highestExpense != null) {
                val displayHighestAmount = currencyManager.getDisplayAmountFromStored(highestExpense.price, highestExpense.currency)
                "${highestExpense.name}: ${currencyManager.formatCurrency(displayHighestAmount)}"
            } else "No expenses yet"
              findViewById<TextView>(R.id.textLowestExpense).text =
            if (lowestExpense != null) {
                val displayLowestAmount = currencyManager.getDisplayAmountFromStored(lowestExpense.price, lowestExpense.currency)
                "${lowestExpense.name}: ${currencyManager.formatCurrency(displayLowestAmount)}"
            } else "No expenses yet"
    }
    
    private fun loadDetailedStats() {
        val expensesJson = sharedPreferences.getString("expenses", "[]")
        val type = object : TypeToken<List<ExpenseItem>>() {}.type
        val expensesList: List<ExpenseItem> = gson.fromJson(expensesJson, type) ?: emptyList()
        
        displayDetailedStats(expensesList)
    }
    
    private fun displayDetailedStats(expenses: List<ExpenseItem>) {
        // Calculate weekly statistics
        val calendar = Calendar.getInstance()
        val startOfWeek = calendar.clone() as Calendar
        startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
        startOfWeek.set(Calendar.MINUTE, 0)
        startOfWeek.set(Calendar.SECOND, 0)
        
        val weeklyExpenses = expenses.filter { expense ->
            try {
                val expenseDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(expense.date)
                expenseDate != null && expenseDate.after(startOfWeek.time)
            } catch (e: Exception) {
                false
            }
        }
        
        val weeklyAmount = weeklyExpenses.sumOf { it.price }
        val weeklyAverage = if (weeklyExpenses.isNotEmpty()) weeklyAmount / 7 else 0.0
        
        // Find most expensive day of the week
        val dayExpenseMap = mutableMapOf<String, Double>()
        expenses.forEach { expense ->
            try {
                val expenseDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(expense.date)
                if (expenseDate != null) {
                    val dayFormat = SimpleDateFormat("EEEE", Locale.getDefault())
                    val dayName = dayFormat.format(expenseDate)
                    dayExpenseMap[dayName] = (dayExpenseMap[dayName] ?: 0.0) + expense.price
                }
            } catch (e: Exception) {
                // Ignore parsing errors
            }
        }
        
        val mostExpensiveDay = dayExpenseMap.maxByOrNull { it.value }?.key ?: "N/A"
        
        // Calculate expense patterns
        val amountFrequency = mutableMapOf<String, Int>()
        expenses.forEach { expense ->
            val roundedAmount = String.format("%.0f", expense.price)
            amountFrequency[roundedAmount] = (amountFrequency[roundedAmount] ?: 0) + 1
        }
        
        val mostFrequentAmount = amountFrequency.maxByOrNull { it.value }?.key?.toDoubleOrNull() ?: 0.0
        
        // Calculate spending trend (comparing recent vs older expenses)
        val recentExpenses = expenses.take(expenses.size / 2)
        val olderExpenses = expenses.drop(expenses.size / 2)
        
        val recentAverage = if (recentExpenses.isNotEmpty()) recentExpenses.sumOf { it.price } / recentExpenses.size else 0.0
        val olderAverage = if (olderExpenses.isNotEmpty()) olderExpenses.sumOf { it.price } / olderExpenses.size else 0.0
        
        val spendingTrend = when {
            recentAverage > olderAverage * 1.1 -> "📈 Increasing"
            recentAverage < olderAverage * 0.9 -> "📉 Decreasing"
            else -> "📊 Stable"
        }
        
        // Calculate days since last expense
        val daysSinceLastExpense = if (expenses.isNotEmpty()) {
            try {
                val lastExpenseDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(expenses.first().date)
                val today = Date()
                if (lastExpenseDate != null) {
                    TimeUnit.DAYS.convert(today.time - lastExpenseDate.time, TimeUnit.MILLISECONDS).toInt()
                } else 0
            } catch (e: Exception) {
                0
            }
        } else 0
          // Update UI        // findViewById<TextView>(R.id.textWeeklyAmount).text = String.format("$%.2f", weeklyAmount)
        // findViewById<TextView>(R.id.textWeeklyAverage).text = String.format("$%.2f", weeklyAverage)
        findViewById<TextView>(R.id.textHighestExpense).text = mostExpensiveDay
        // findViewById<TextView>(R.id.textMostFrequentAmount).text = String.format("$%.0f", mostFrequentAmount)
        // findViewById<TextView>(R.id.textSpendingTrend).text = spendingTrend
        // findViewById<TextView>(R.id.textDaysSinceLastExpense).text = daysSinceLastExpense.toString()
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
                // Already in this activity, just close drawer
            }
            R.id.nav_analytics -> {
                startActivity(Intent(this, AnalyticsActivity::class.java))
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
