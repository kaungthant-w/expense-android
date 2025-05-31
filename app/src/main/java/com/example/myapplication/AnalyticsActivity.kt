package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*

class AnalyticsActivity : AppCompatActivity() {
    
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_analytics)
        
        setupActionBar()
        setupSharedPreferences()
        loadAnalyticsData()
    }
    
    private fun setupActionBar() {
        supportActionBar?.title = "📈 Expense Analytics"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
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
        findViewById<TextView>(R.id.textMostExpensiveDay).text = 
            if (mostExpensiveDay != null) "${mostExpensiveDay.key}: $${String.format("%.2f", mostExpensiveDay.value)}" 
            else "No data available"
            
        findViewById<TextView>(R.id.textLeastExpensiveDay).text = 
            if (leastExpensiveDay != null) "${leastExpensiveDay.key}: $${String.format("%.2f", leastExpensiveDay.value)}" 
            else "No data available"
        
        findViewById<TextView>(R.id.textMorningExpenses).text = 
            "${morningExpenses.size} expenses - $${String.format("%.2f", morningExpenses.sumOf { it.price })}"
            
        findViewById<TextView>(R.id.textAfternoonExpenses).text = 
            "${afternoonExpenses.size} expenses - $${String.format("%.2f", afternoonExpenses.sumOf { it.price })}"
            
        findViewById<TextView>(R.id.textEveningExpenses).text = 
            "${eveningExpenses.size} expenses - $${String.format("%.2f", eveningExpenses.sumOf { it.price })}"
            
        findViewById<TextView>(R.id.textNightExpenses).text = 
            "${nightExpenses.size} expenses - $${String.format("%.2f", nightExpenses.sumOf { it.price })}"
        
        findViewById<TextView>(R.id.textThisWeekExpenses).text = thisWeekExpenses.size.toString()
        findViewById<TextView>(R.id.textThisWeekAmount).text = String.format("$%.2f", thisWeekExpenses.sumOf { it.price })
        
        // Average expense per day
        val averagePerDay = if (expenses.isNotEmpty()) {
            val uniqueDates = expenses.map { it.date }.toSet()
            expenses.sumOf { it.price } / uniqueDates.size
        } else 0.0
        
        findViewById<TextView>(R.id.textAveragePerDay).text = String.format("$%.2f", averagePerDay)
        
        // Show top 3 most expensive expenses
        val topExpenses = expenses.sortedByDescending { it.price }.take(3)
        val topExpensesText = if (topExpenses.isNotEmpty()) {
            topExpenses.mapIndexed { index, expense -> 
                "${index + 1}. ${expense.name}: $${String.format("%.2f", expense.price)}"
            }.joinToString("\n")
        } else "No expenses yet"
        
        findViewById<TextView>(R.id.textTopExpenses).text = topExpensesText
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
