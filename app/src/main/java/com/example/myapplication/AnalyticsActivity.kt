package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsActivity : AppCompatActivity() {
    
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currencyManager: CurrencyManager
    private val gson = Gson()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)
        
        setupActionBar()
        setupSharedPreferences()
        loadAnalyticsData()
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
        supportActionBar?.title = "📈 Expense Analytics"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
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
    
    private fun displayAnalytics(expenses: List<ExpenseItem>) {
        // Calculate spending by day of week
        val dayOfWeekExpenses = mutableMapOf<String, Double>()
        val dayOfWeekCounts = mutableMapOf<String, Int>()
        
        expenses.forEach { expense ->
            try {
                val date = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(expense.date)
                val dayOfWeek = SimpleDateFormat("EEEE", Locale.getDefault()).format(date!!)
                
                dayOfWeekExpenses[dayOfWeek] = (dayOfWeekExpenses[dayOfWeek] ?: 0.0) + expense.price
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
        val currentCurrency = currencyManager.getCurrentCurrency()
        
        findViewById<TextView>(R.id.textMostExpensiveDay).text = 
            if (mostExpensiveDay != null) {
                val displayAmount = if (currentCurrency == CurrencyManager.CURRENCY_MMK) {
                    currencyManager.convertFromUsd(mostExpensiveDay.value)
                } else mostExpensiveDay.value
                "${mostExpensiveDay.key}: ${currencyManager.formatCurrency(displayAmount)}"
            } else "No data available"
            
        findViewById<TextView>(R.id.textLeastExpensiveDay).text = 
            if (leastExpensiveDay != null) {
                val displayAmount = if (currentCurrency == CurrencyManager.CURRENCY_MMK) {
                    currencyManager.convertFromUsd(leastExpensiveDay.value)
                } else leastExpensiveDay.value
                "${leastExpensiveDay.key}: ${currencyManager.formatCurrency(displayAmount)}"
            } else "No data available"
        
        val morningTotal = morningExpenses.sumOf { it.price }
        val displayMorningTotal = if (currentCurrency == CurrencyManager.CURRENCY_MMK) {
            currencyManager.convertFromUsd(morningTotal)
        } else morningTotal
        findViewById<TextView>(R.id.textMorningExpenses).text = 
            "${morningExpenses.size} expenses - ${currencyManager.formatCurrency(displayMorningTotal)}"
            
        val afternoonTotal = afternoonExpenses.sumOf { it.price }
        val displayAfternoonTotal = if (currentCurrency == CurrencyManager.CURRENCY_MMK) {
            currencyManager.convertFromUsd(afternoonTotal)
        } else afternoonTotal
        findViewById<TextView>(R.id.textAfternoonExpenses).text = 
            "${afternoonExpenses.size} expenses - ${currencyManager.formatCurrency(displayAfternoonTotal)}"
            
        val eveningTotal = eveningExpenses.sumOf { it.price }
        val displayEveningTotal = if (currentCurrency == CurrencyManager.CURRENCY_MMK) {
            currencyManager.convertFromUsd(eveningTotal)
        } else eveningTotal
        findViewById<TextView>(R.id.textEveningExpenses).text = 
            "${eveningExpenses.size} expenses - ${currencyManager.formatCurrency(displayEveningTotal)}"
            
        val nightTotal = nightExpenses.sumOf { it.price }
        val displayNightTotal = if (currentCurrency == CurrencyManager.CURRENCY_MMK) {
            currencyManager.convertFromUsd(nightTotal)
        } else nightTotal
        findViewById<TextView>(R.id.textNightExpenses).text = 
            "${nightExpenses.size} expenses - ${currencyManager.formatCurrency(displayNightTotal)}"
        
        findViewById<TextView>(R.id.textThisWeekExpenses).text = thisWeekExpenses.size.toString()
        val weekTotal = thisWeekExpenses.sumOf { it.price }
        val displayWeekTotal = if (currentCurrency == CurrencyManager.CURRENCY_MMK) {
            currencyManager.convertFromUsd(weekTotal)
        } else weekTotal
        findViewById<TextView>(R.id.textThisWeekAmount).text = currencyManager.formatCurrency(displayWeekTotal)
          // Average expense per day
        val averagePerDay = if (expenses.isNotEmpty()) {
            val uniqueDates = expenses.map { it.date }.toSet()
            expenses.sumOf { it.price } / uniqueDates.size
        } else 0.0
        
        val displayAveragePerDay = if (currentCurrency == CurrencyManager.CURRENCY_MMK) {
            currencyManager.convertFromUsd(averagePerDay)
        } else averagePerDay
        findViewById<TextView>(R.id.textAveragePerDay).text = currencyManager.formatCurrency(displayAveragePerDay)
        
        // Show top 3 most expensive expenses
        val topExpenses = expenses.sortedByDescending { it.price }.take(3)
        val topExpensesText = if (topExpenses.isNotEmpty()) {
            topExpenses.mapIndexed { index, expense -> 
                val displayAmount = if (currentCurrency == CurrencyManager.CURRENCY_MMK) {
                    currencyManager.convertFromUsd(expense.price)
                } else expense.price
                "${index + 1}. ${expense.name}: ${currencyManager.formatCurrency(displayAmount)}"
            }.joinToString("\n")
        } else "No expenses yet"
        
        findViewById<TextView>(R.id.textTopExpenses).text = topExpensesText
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
