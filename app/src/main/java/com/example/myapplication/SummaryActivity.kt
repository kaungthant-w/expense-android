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
import java.util.concurrent.TimeUnit

class SummaryActivity : AppCompatActivity() {
    
    private lateinit var sharedPreferences: SharedPreferences
    private val gson = Gson()
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)
        
        setupActionBar()
        setupSharedPreferences()
        loadSummaryData()
    }
    
    private fun setupActionBar() {
        supportActionBar?.title = "📊 Expense Summary"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
    }
    
    private fun loadSummaryData() {
        val expensesJson = sharedPreferences.getString("expenses", "[]")
        val type = object : TypeToken<List<ExpenseItem>>() {}.type
        val expensesList: List<ExpenseItem> = gson.fromJson(expensesJson, type) ?: emptyList()
        
        displaySummary(expensesList)
    }
    
    private fun displaySummary(expenses: List<ExpenseItem>) {
        val totalExpenses = expenses.size
        val totalAmount = expenses.sumOf { it.price }
        val averageAmount = if (totalExpenses > 0) totalAmount / totalExpenses else 0.0
        
        // Calculate today's expenses
        val today = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).format(Date())
        val todayExpenses = expenses.filter { it.date == today }
        val todayAmount = todayExpenses.sumOf { it.price }
        
        // Calculate this month's expenses
        val currentMonth = SimpleDateFormat("MM/yyyy", Locale.getDefault()).format(Date())
        val monthExpenses = expenses.filter { 
            val expenseMonth = it.date.substring(3) // Get MM/yyyy part
            expenseMonth == currentMonth
        }
        val monthAmount = monthExpenses.sumOf { it.price }
        
        // Find highest and lowest expense
        val highestExpense = expenses.maxByOrNull { it.price }
        val lowestExpense = expenses.minByOrNull { it.price }
        
        // Update UI
        findViewById<TextView>(R.id.textTotalExpenses).text = totalExpenses.toString()
        findViewById<TextView>(R.id.textTotalAmount).text = String.format("$%.2f", totalAmount)
        findViewById<TextView>(R.id.textAverageAmount).text = String.format("$%.2f", averageAmount)
        
        findViewById<TextView>(R.id.textTodayExpenses).text = todayExpenses.size.toString()
        findViewById<TextView>(R.id.textTodayAmount).text = String.format("$%.2f", todayAmount)
        
        findViewById<TextView>(R.id.textMonthExpenses).text = monthExpenses.size.toString()
        findViewById<TextView>(R.id.textMonthAmount).text = String.format("$%.2f", monthAmount)
        
        findViewById<TextView>(R.id.textHighestExpense).text = 
            if (highestExpense != null) "${highestExpense.name}: $${String.format("%.2f", highestExpense.price)}" 
            else "No expenses yet"
            
        findViewById<TextView>(R.id.textLowestExpense).text =            if (lowestExpense != null) "${lowestExpense.name}: $${String.format("%.2f", lowestExpense.price)}" 
            else "No expenses yet"
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
          // Update UI
        // findViewById<TextView>(R.id.textWeeklyAmount).text = String.format("$%.2f", weeklyAmount)
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
}
