package com.example.myapplication.pdf

import com.example.myapplication.models.Expense
import java.util.*

interface PeriodStrategy {
    fun filterExpenses(expenses: List<Expense>): List<Expense>
    fun getPeriodName(): String
}

class TodayStrategy : PeriodStrategy {
    override fun filterExpenses(expenses: List<Expense>): List<Expense> {
        val today = Calendar.getInstance()
        val todayYear = today.get(Calendar.YEAR)
        val todayMonth = today.get(Calendar.MONTH)
        val todayDay = today.get(Calendar.DAY_OF_MONTH)
        
        return expenses.filter { expense ->
            val expenseDate = Calendar.getInstance().apply {
                timeInMillis = expense.date
            }
            expenseDate.get(Calendar.YEAR) == todayYear &&
            expenseDate.get(Calendar.MONTH) == todayMonth &&
            expenseDate.get(Calendar.DAY_OF_MONTH) == todayDay
        }
    }
    
    override fun getPeriodName(): String = "today"
}

class WeeklyStrategy : PeriodStrategy {
    override fun filterExpenses(expenses: List<Expense>): List<Expense> {
        val calendar = Calendar.getInstance()
        val currentWeek = calendar.get(Calendar.WEEK_OF_YEAR)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return expenses.filter { expense ->
            val expenseCalendar = Calendar.getInstance().apply {
                timeInMillis = expense.date
            }
            expenseCalendar.get(Calendar.WEEK_OF_YEAR) == currentWeek &&
            expenseCalendar.get(Calendar.YEAR) == currentYear
        }
    }
    
    override fun getPeriodName(): String = "weekly"
}

class MonthlyStrategy : PeriodStrategy {
    override fun filterExpenses(expenses: List<Expense>): List<Expense> {
        val calendar = Calendar.getInstance()
        val currentMonth = calendar.get(Calendar.MONTH)
        val currentYear = calendar.get(Calendar.YEAR)
        
        return expenses.filter { expense ->
            val expenseCalendar = Calendar.getInstance().apply {
                timeInMillis = expense.date
            }
            expenseCalendar.get(Calendar.MONTH) == currentMonth &&
            expenseCalendar.get(Calendar.YEAR) == currentYear
        }
    }
    
    override fun getPeriodName(): String = "monthly"
}

class YearlyStrategy : PeriodStrategy {
    override fun filterExpenses(expenses: List<Expense>): List<Expense> {
        val calendar = Calendar.getInstance()
        val currentYear = calendar.get(Calendar.YEAR)
        
        return expenses.filter { expense ->
            val expenseCalendar = Calendar.getInstance().apply {
                timeInMillis = expense.date
            }
            expenseCalendar.get(Calendar.YEAR) == currentYear
        }
    }
    
    override fun getPeriodName(): String = "yearly"
}

object PeriodStrategyFactory {
    fun createStrategy(period: String): PeriodStrategy {
        return when (period.lowercase()) {
            "today" -> TodayStrategy()
            "weekly" -> WeeklyStrategy()
            "monthly" -> MonthlyStrategy()
            "yearly" -> YearlyStrategy()
            else -> TodayStrategy() // Default to today
        }
    }
}