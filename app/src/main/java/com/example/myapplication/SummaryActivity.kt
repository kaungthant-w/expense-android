package com.example.myapplication

import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.ImageButton
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.example.myapplication.models.Expense
import com.example.myapplication.pdf.*
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.text.SimpleDateFormat
import java.util.*
import java.util.concurrent.TimeUnit

class SummaryActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
      private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currencyManager: CurrencyManager
    private val gson = Gson()
      // Navigation Drawer components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_summary)
        setupActionBar()
        initViews()
        setupNavigationDrawer()
        setupSharedPreferences()
        loadSummaryData()
        updateNavigationMenuTitles()
        updateTextElements()
    }
    
    private fun applyTheme() {
        val themePrefs = getSharedPreferences(ThemeActivity.THEME_PREFS, Context.MODE_PRIVATE)
        val savedTheme = themePrefs.getString(ThemeActivity.THEME_KEY, ThemeActivity.THEME_SYSTEM)
        
        when (savedTheme) {
            ThemeActivity.THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeActivity.THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            ThemeActivity.THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }    private fun setupActionBar() {
        supportActionBar?.title = languageManager.getString("summary_title")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
          // Setup back button click listener
        findViewById<ImageButton>(R.id.buttonBack).setOnClickListener {
            finish()
        }
          // Setup PDF export button click listener
        findViewById<ImageButton>(R.id.buttonPdfExport).setOnClickListener {
            showPdfExportDialog()
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
    }    private fun updateNavigationMenuTitles() {
        val menu = navigationView.menu
        menu.findItem(R.id.nav_home)?.title = languageManager.getString("nav_home")
        menu.findItem(R.id.nav_all_list)?.title = languageManager.getString("nav_all_list")
        menu.findItem(R.id.nav_history)?.title = languageManager.getString("nav_history")
        menu.findItem(R.id.nav_summary)?.title = languageManager.getString("nav_summary")
        menu.findItem(R.id.nav_currency_exchange)?.title = languageManager.getString("nav_currency_exchange")
        menu.findItem(R.id.nav_settings)?.title = languageManager.getString("nav_settings")
        menu.findItem(R.id.nav_feedback)?.title = languageManager.getString("nav_feedback")
        menu.findItem(R.id.nav_about)?.title = languageManager.getString("nav_about")
    }
      private fun updateTextElements() {
        // Update header and section titles
        findViewById<TextView>(R.id.textViewSummaryTitle)?.text = languageManager.getString("summary_title")
        findViewById<TextView>(R.id.textViewOverallStats)?.text = languageManager.getString("overall_statistics")
        findViewById<TextView>(R.id.textViewTodaysSummary)?.text = languageManager.getString("today_summary")
        findViewById<TextView>(R.id.textViewWeeklySummary)?.text = languageManager.getString("analytics_weekly_analysis")
        findViewById<TextView>(R.id.textViewMontHSUmmary)?.text = languageManager.getString("month_summary")
        findViewById<TextView>(R.id.textViewExpenseExtremes)?.text = languageManager.getString("expense_extremes")
        
        // Update labels
        findViewById<TextView>(R.id.textViewTotalExpensesLabel)?.text = languageManager.getString("total_expenses")
        findViewById<TextView>(R.id.textViewTotalAmountLabel)?.text = languageManager.getString("total_amount")
        findViewById<TextView>(R.id.textViewAverageAmountLabel)?.text = languageManager.getString("average_amount")
        findViewById<TextView>(R.id.textViewTodayExpensesLabel)?.text = languageManager.getString("summary_today_expenses")
        findViewById<TextView>(R.id.textViewTodayTotalLabel)?.text = languageManager.getString("today_total")
        findViewById<TextView>(R.id.textViewWeekExpensesLabel)?.text = languageManager.getString("analytics_this_week_expenses")
        findViewById<TextView>(R.id.textViewWeekTotalLabel)?.text = languageManager.getString("analytics_this_week_total")
        findViewById<TextView>(R.id.textViewWeekAverageLabel)?.text = languageManager.getString("analytics_average_per_day")
        findViewById<TextView>(R.id.textViewMonthExpensesLabel)?.text = languageManager.getString("month_expenses")
        findViewById<TextView>(R.id.textViewMonthTotalLabel)?.text = languageManager.getString("month_total")
        findViewById<TextView>(R.id.textViewHighestExpenseLabel)?.text = languageManager.getString("highest_expense")
        findViewById<TextView>(R.id.textViewLowestExpenseLabel)?.text = languageManager.getString("lowest_expense")
    }
    
    private fun setupSharedPreferences() {
        sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
        currencyManager = CurrencyManager.getInstance(this)
    }
      private fun loadSummaryData() {
        val expensesJson = sharedPreferences.getString("expenses", "[]")
        val type = object : TypeToken<List<ExpenseItem>>() {}.type
        val allExpensesList: List<ExpenseItem> = gson.fromJson(expensesJson, type) ?: emptyList()
        
        // Filter out soft deleted expenses - only show active expenses
        val activeExpensesList = allExpensesList.filter { !it.isDeleted }
        
        displaySummary(activeExpensesList)
    }private fun displaySummary(expenses: List<ExpenseItem>) {
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
        
        // Calculate this week's expenses
        val calendar = Calendar.getInstance()
        val startOfWeek = calendar.clone() as Calendar
        // Set to Monday of this week
        startOfWeek.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY)
        startOfWeek.set(Calendar.HOUR_OF_DAY, 0)
        startOfWeek.set(Calendar.MINUTE, 0)
        startOfWeek.set(Calendar.SECOND, 0)
        startOfWeek.set(Calendar.MILLISECOND, 0)
        
        val weekExpenses = expenses.filter { expense ->
            try {
                val expenseDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(expense.date)
                expenseDate != null && !expenseDate.before(startOfWeek.time)
            } catch (e: Exception) {
                false
            }
        }
        val weekAmount = weekExpenses.sumOf { expense ->
            currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
        }
        val weekAveragePerDay = if (weekExpenses.isNotEmpty()) weekAmount / 7 else 0.0
        
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
        val displayWeekAmount = weekAmount
        val displayWeekAveragePerDay = weekAveragePerDay
        val displayMonthAmount = monthAmount
        
        // Update UI
        findViewById<TextView>(R.id.textTotalExpenses).text = totalExpenses.toString()
        findViewById<TextView>(R.id.textTotalAmount).text = currencyManager.formatCurrency(displayTotalAmount)
        findViewById<TextView>(R.id.textAverageAmount).text = currencyManager.formatCurrency(displayAverageAmount)
        
        findViewById<TextView>(R.id.textTodayExpenses).text = todayExpenses.size.toString()
        findViewById<TextView>(R.id.textTodayAmount).text = currencyManager.formatCurrency(displayTodayAmount)
        
        findViewById<TextView>(R.id.textWeekExpenses).text = weekExpenses.size.toString()
        findViewById<TextView>(R.id.textWeekAmount).text = currencyManager.formatCurrency(displayWeekAmount)
        findViewById<TextView>(R.id.textWeekAverage).text = currencyManager.formatCurrency(displayWeekAveragePerDay)
          findViewById<TextView>(R.id.textMonthExpenses).text = monthExpenses.size.toString()
        findViewById<TextView>(R.id.textMonthAmount).text = currencyManager.formatCurrency(displayMonthAmount)
        
        findViewById<TextView>(R.id.textHighestExpense).text =
            if (highestExpense != null) {
                val displayHighestAmount = currencyManager.getDisplayAmountFromStored(highestExpense.price, highestExpense.currency)
                "${highestExpense.name}: ${currencyManager.formatCurrency(displayHighestAmount)}"
            } else languageManager.getString("no_expenses_yet")
          
        findViewById<TextView>(R.id.textLowestExpense).text =
            if (lowestExpense != null) {
                val displayLowestAmount = currencyManager.getDisplayAmountFromStored(lowestExpense.price, lowestExpense.currency)
                "${lowestExpense.name}: ${currencyManager.formatCurrency(displayLowestAmount)}"
            } else languageManager.getString("no_expenses_yet")
    }
      private fun loadDetailedStats() {
        val expensesJson = sharedPreferences.getString("expenses", "[]")
        val type = object : TypeToken<List<ExpenseItem>>() {}.type
        val allExpensesList: List<ExpenseItem> = gson.fromJson(expensesJson, type) ?: emptyList()
        
        // Filter out soft deleted expenses - only show active expenses
        val activeExpensesList = allExpensesList.filter { !it.isDeleted }
        
        displayDetailedStats(activeExpensesList)
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
            }        }
        
        // Removed unused weeklyAmount calculation that was causing compilation warning
        
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
        
        // val mostFrequentAmount = amountFrequency.maxByOrNull { it.value }?.key?.toDoubleOrNull() ?: 0.0        // Calculate spending trend (comparing recent vs older expenses)
        // Removed unused calculations that were causing compilation warnings
        
        // val spendingTrend = when {
        //     recentAverage > olderAverage * 1.1 -> "📈 Increasing"
        //     recentAverage < olderAverage * 0.9 -> "📉 Decreasing"
        //     else -> "📊 Stable"
        // }
        
        // Calculate days since last expense
        // val daysSinceLastExpense = if (expenses.isNotEmpty()) {
        //     try {
        //         val lastExpenseDate = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault()).parse(expenses.first().date)
        //         val today = Date()
        //         if (lastExpenseDate != null) {
        //             TimeUnit.DAYS.convert(today.time - lastExpenseDate.time, TimeUnit.MILLISECONDS).toInt()
        //         } else 0
        //     } catch (e: Exception) {
        //         0
        //     }
        // } else 0
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
            }R.id.nav_feedback -> {
                startActivity(Intent(this, FeedbackActivity::class.java))
            }
            R.id.nav_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            @Suppress("DEPRECATION")
            super.onBackPressed()
        }
    }
      private fun showPdfExportDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_pdf_export, null)
        
        // Find spinners in the dialog
        val spinnerPeriod: Spinner = dialogView.findViewById(R.id.spinnerPeriod)
        val spinnerCurrency: Spinner = dialogView.findViewById(R.id.spinnerCurrency)
        val spinnerLanguage: Spinner = dialogView.findViewById(R.id.spinnerLanguage)
        
        // Setup period options
        val periodOptions = arrayOf(
            languageManager.getString("pdf_export_today"),
            languageManager.getString("pdf_export_weekly"), 
            languageManager.getString("pdf_export_monthly"),
            languageManager.getString("pdf_export_yearly")
        )
        spinnerPeriod.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, periodOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        // Setup currency options
        val currencyOptions = arrayOf("USD", "MMK", "JPY", "CNY", "THB")
        spinnerCurrency.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        // Setup language options
        val languageOptions = arrayOf(
            languageManager.getString("pdf_export_english"),
            languageManager.getString("pdf_export_myanmar"),
            languageManager.getString("pdf_export_japanese"),
            languageManager.getString("pdf_export_chinese"),
            languageManager.getString("pdf_export_thai")
        )
        spinnerLanguage.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageOptions).apply {
            setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        }
        
        // Set default selections
        spinnerCurrency.setSelection(currencyOptions.indexOf(currencyManager.getCurrentDisplayCurrency()))
        
        val dialog = AlertDialog.Builder(this)
            .setTitle(languageManager.getString("pdf_export_title"))
            .setView(dialogView)            .setPositiveButton(languageManager.getString("pdf_export_generate")) { _, _ ->
                val periodIndex = spinnerPeriod.selectedItemPosition
                val selectedCurrency = currencyOptions[spinnerCurrency.selectedItemPosition]
                val languageIndex = spinnerLanguage.selectedItemPosition
                
                generatePdfExport(periodIndex, selectedCurrency, languageIndex)
            }.setNegativeButton(languageManager.getString("cancel_button")) { dialog, _ ->
                dialog.dismiss()
            }
            .create()
            
        dialog.show()
    }
      private fun generatePdfExport(periodIndex: Int, targetCurrency: String, languageIndex: Int) {
        try {
            // Load all expenses
            val expensesJson = sharedPreferences.getString("expenses", "[]")
            val type = object : TypeToken<List<ExpenseItem>>() {}.type
            val allExpensesList: List<ExpenseItem> = gson.fromJson(expensesJson, type) ?: emptyList()
            
            // Filter out soft deleted expenses
            val activeExpensesList = allExpensesList.filter { !it.isDeleted }
            
            // Convert ExpenseItem to Expense for PDF system
            val expensesForPdf = convertToExpenseList(activeExpensesList)
            
            // Create PDF export facade with context
            val pdfExportFacade = PdfExportFacade(this)
            
            // Determine period string
            val periodString = when (periodIndex) {
                0 -> "today"
                1 -> "weekly"
                2 -> "monthly"
                3 -> "yearly"
                else -> "today"
            }
            
            // Determine language string
            val languageString = when (languageIndex) {
                0 -> "en"
                1 -> "mm"
                2 -> "ja"
                3 -> "zh"
                4 -> "th"
                else -> "en"
            }
            
            // Generate PDF using the correct method name
            val success = pdfExportFacade.exportPdf(
                period = periodString,
                expenses = expensesForPdf,
                targetCurrency = targetCurrency,
                targetLanguage = languageString
            )
            
            if (success) {
                // Show success message
                Toast.makeText(this, languageManager.getString("pdf_export_success"), Toast.LENGTH_LONG).show()
            } else {
                Toast.makeText(this, languageManager.getString("pdf_export_failed"), Toast.LENGTH_LONG).show()
            }
            
        } catch (e: Exception) {
            e.printStackTrace()
            Toast.makeText(this, "${languageManager.getString("pdf_export_error")}: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
      private fun convertToExpenseList(expenseItems: List<ExpenseItem>): List<Expense> {
        return expenseItems.map { item ->
            // Convert date string (dd/MM/yyyy) to timestamp
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timestamp = try {
                dateFormat.parse(item.date)?.time ?: System.currentTimeMillis()
            } catch (e: Exception) {
                System.currentTimeMillis()
            }
            
            // Convert currency amount to target currency for PDF
            val convertedAmount = currencyManager.getDisplayAmountFromStored(item.price, item.currency)
            
            Expense(
                id = item.id,
                name = item.name,
                amount = convertedAmount,
                description = item.description,
                date = timestamp
            )
        }
    }
}
