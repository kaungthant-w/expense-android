package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.*
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.poi.ss.usermodel.*
import org.apache.poi.xssf.usermodel.XSSFWorkbook
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class ExportActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
      // Navigation Drawer components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
      // UI Components
    private lateinit var editTextCustomTitle: EditText
    private lateinit var spinnerExportPeriod: Spinner
    private lateinit var spinnerPaperSize: Spinner
    private lateinit var buttonSaveAsExcel: Button
    private lateinit var buttonPrintWirelessly: Button
    private lateinit var cardViewDeviceDiscovery: CardView
    private lateinit var textViewDeviceStatus: TextView
    private lateinit var progressBarDeviceSearch: ProgressBar
    private lateinit var recyclerViewDevices: RecyclerView
    
    // Data and Managers
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currencyManager: CurrencyManager
    private val gson = Gson()
    
    // Export launchers
    private lateinit var excelExportLauncher: ActivityResultLauncher<Intent>      // Data arrays for spinners
    private val periodOptions = arrayOf("export_period_today", "export_period_this_week", "export_period_this_month", "export_period_this_year")
    private val paperSizeOptions = arrayOf("A4", "Legal", "Letter")
    private val paperSizeLabels = arrayOf("📄 A4 (210 × 297 mm)", "📄 Legal (216 × 356 mm)", "📄 Letter (216 × 279 mm)")
    
    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_export)
        
        initializeComponents()
        setupNavigationDrawer()
        setupClickListeners()
        setupSpinners()
        setupExportLaunchers()
        updateUITexts()
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
    
    private fun initializeComponents() {        // Initialize navigation drawer
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
          // Initialize UI components
        editTextCustomTitle = findViewById(R.id.editTextCustomTitle)
        spinnerExportPeriod = findViewById(R.id.spinnerExportPeriod)
        spinnerPaperSize = findViewById(R.id.spinnerPaperSize)
        buttonSaveAsExcel = findViewById(R.id.buttonSaveAsExcel)
        buttonPrintWirelessly = findViewById(R.id.buttonPrintWirelessly)
        cardViewDeviceDiscovery = findViewById(R.id.cardViewDeviceDiscovery)
        textViewDeviceStatus = findViewById(R.id.textViewDeviceStatus)
        progressBarDeviceSearch = findViewById(R.id.progressBarDeviceSearch)
        recyclerViewDevices = findViewById(R.id.recyclerViewDevices)
        
        // Initialize data components
        sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
        currencyManager = CurrencyManager.getInstance(this)
        
        // Set default title
        editTextCustomTitle.setText(languageManager.getString("app_name") + " " + languageManager.getString("report_export"))
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
    
    private fun setupClickListeners() {
        // Back button
        findViewById<ImageButton>(R.id.buttonBack).setOnClickListener {
            finish()
        }
        
        // Excel export button
        buttonSaveAsExcel.setOnClickListener {
            showExcelExportDialog()
        }
        
        // Print wirelessly button
        buttonPrintWirelessly.setOnClickListener {
            showPrintDialog()
        }
    }
    
    private fun setupSpinners() {
        // Export Period Spinner
        val periodAdapter = object : ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, periodOptions) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).text = languageManager.getString(periodOptions[position])
                return view
            }
              override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).text = languageManager.getString(periodOptions[position])
                return view
            }
        }
        periodAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerExportPeriod.adapter = periodAdapter
        
        // Paper Size Spinner
        val paperSizeAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, paperSizeLabels)
        paperSizeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerPaperSize.adapter = paperSizeAdapter
        
        // Set A4 as default (index 0)
        spinnerPaperSize.setSelection(0)
    }
    
    private fun setupExportLaunchers() {
        excelExportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    exportToExcel(uri)
                }
            }
        }
    }
      private fun updateUITexts() {
        // Update static text elements        findViewById<TextView>(R.id.textViewTitle).text = languageManager.getString("export_comprehensive_title")
        findViewById<TextView>(R.id.textViewDescription).text = languageManager.getString("export_comprehensive_description")
        findViewById<TextView>(R.id.textViewCustomTitleLabel).text = "📋 " + languageManager.getString("custom_title")
        findViewById<TextView>(R.id.textViewPeriodLabel).text = "📅 " + languageManager.getString("export_period")
        findViewById<TextView>(R.id.textViewPaperSizeLabel).text = "📄 " + languageManager.getString("paper_size")
        findViewById<TextView>(R.id.textViewOptionsLabel).text = "📤 " + languageManager.getString("export_options")
        findViewById<TextView>(R.id.textViewDeviceDiscoveryLabel).text = "🔍 " + languageManager.getString("device_discovery")
        
        // Update buttons
        buttonSaveAsExcel.text = "📊 " + languageManager.getString("save_as_excel")
        buttonPrintWirelessly.text = "🖨️ " + languageManager.getString("print_wirelessly")
        
        // Update hint
        editTextCustomTitle.hint = languageManager.getString("enter_title_hint")
        
        // Update device status
        textViewDeviceStatus.text = languageManager.getString("device_search_ready")
    }
      private fun showExcelExportDialog() {
        val selectedPeriod = spinnerExportPeriod.selectedItemPosition
        val periodName = when (selectedPeriod) {
            0 -> languageManager.getString("export_period_today")
            1 -> languageManager.getString("export_period_this_week") 
            2 -> languageManager.getString("export_period_this_month")
            3 -> languageManager.getString("export_period_this_year")
            else -> "all periods"
        }
        
        val message = languageManager.getString("excel_export_message") + 
                "\n\n📅 Export period: $periodName" +
                "\n💡 Tip: If no data is found, try selecting 'This Week' or 'This Month' period."
        
        AlertDialog.Builder(this)
            .setTitle("📊 " + languageManager.getString("excel_export"))
            .setMessage(message)
            .setPositiveButton(languageManager.getString("export_comprehensive")) { _, _ ->
                startExcelExport()
            }
            .setNegativeButton(languageManager.getString("cancel"), null)
            .show()
    }
    
    private fun showPrintDialog() {
        AlertDialog.Builder(this)
            .setTitle("🖨️ " + languageManager.getString("wireless_print"))
            .setMessage(languageManager.getString("wireless_print_message"))
            .setPositiveButton(languageManager.getString("discover_devices")) { _, _ ->
                startDeviceDiscovery()
            }
            .setNegativeButton(languageManager.getString("cancel"), null)
            .show()
    }
      private fun startExcelExport() {
        // Pre-validate expenses before starting export
        val selectedPeriod = spinnerExportPeriod.selectedItemPosition
        val previewExpenses = loadFilteredExpenses(selectedPeriod)
        
        if (previewExpenses.isEmpty()) {
            val periodName = when (selectedPeriod) {
                0 -> languageManager.getString("export_period_today")
                1 -> languageManager.getString("export_period_this_week") 
                2 -> languageManager.getString("export_period_this_month")
                3 -> languageManager.getString("export_period_this_year")
                else -> "selected period"
            }
            
            AlertDialog.Builder(this)
                .setTitle("⚠️ No Data Found")
                .setMessage("No expenses found for $periodName.\n\n" +
                        "💡 Try selecting:\n" +
                        "• 'This Week' to export recent expenses\n" +
                        "• 'This Month' for monthly data\n" +
                        "• Add new expenses for today if needed")
                .setPositiveButton("Change Period", null)
                .show()
            return
        }
        
        // Show confirmation with preview
        val totalAmount = previewExpenses.sumOf { it.price }
        val confirmMessage = "Ready to export ${previewExpenses.size} expenses\n" +
                "Total amount: $%.2f\n\nProceed with Excel export?".format(totalAmount)
        
        AlertDialog.Builder(this)
            .setTitle("📊 Export Confirmation")
            .setMessage(confirmMessage)
            .setPositiveButton("Export") { _, _ ->
                proceedWithExcelExport()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun proceedWithExcelExport() {
        val customTitle = editTextCustomTitle.text.toString().ifEmpty { 
            languageManager.getString("app_name") + " " + languageManager.getString("report_export")
        }
        
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        val fileName = "${customTitle.replace(" ", "_")}_$timestamp.xlsx"
        
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        
        excelExportLauncher.launch(intent)
    }
    
    private fun startDeviceDiscovery() {
        cardViewDeviceDiscovery.visibility = View.VISIBLE
        textViewDeviceStatus.text = languageManager.getString("searching_devices")
        progressBarDeviceSearch.visibility = View.VISIBLE
        
        // Simulate device discovery (in real implementation, this would use WiFi Direct or Bluetooth)
        android.os.Handler(mainLooper).postDelayed({
            textViewDeviceStatus.text = languageManager.getString("no_devices_found")
            progressBarDeviceSearch.visibility = View.GONE
        }, 3000)
    }
      private fun exportToExcel(uri: Uri) {
        try {
            val customTitle = editTextCustomTitle.text.toString().ifEmpty { 
                languageManager.getString("app_name") + " " + languageManager.getString("report_export")
            }
              val selectedPeriod = spinnerExportPeriod.selectedItemPosition
            val selectedCurrency = currencyManager.getCurrentCurrency()
            val selectedLanguageCode = languageManager.getCurrentLanguage()
            val selectedPaperSize = paperSizeOptions[spinnerPaperSize.selectedItemPosition]
            
            // Load expense data
            val expenses = loadFilteredExpenses(selectedPeriod)
              // Debug logging
            android.util.Log.d("ExportActivity", "Loaded ${expenses.size} expenses for export")
            android.util.Log.d("ExportActivity", "Selected paper size: $selectedPaperSize")
            if (expenses.isEmpty()) {
                val periodName = when (selectedPeriod) {
                    0 -> languageManager.getString("export_period_today")
                    1 -> languageManager.getString("export_period_this_week") 
                    2 -> languageManager.getString("export_period_this_month")
                    3 -> languageManager.getString("export_period_this_year")
                    else -> "selected period"
                }
                
                AlertDialog.Builder(this)
                    .setTitle("📊 " + languageManager.getString("excel_export"))
                    .setMessage("No expenses found for $periodName. Please:\n\n" +
                            "• Add some expenses for the selected period, or\n" +
                            "• Choose a different time period that contains expenses\n\n" +
                            "Current available expenses are from previous days.")
                    .setPositiveButton(languageManager.getString("ok"), null)
                    .show()
                return
            }
            
            // Create Excel workbook
            val workbook = XSSFWorkbook()
            val sheet = workbook.createSheet(customTitle)
            
            // Set paper size based on selection
            when (selectedPaperSize) {
                "A4" -> sheet.printSetup.paperSize = org.apache.poi.ss.usermodel.PrintSetup.A4_PAPERSIZE
                "Legal" -> sheet.printSetup.paperSize = org.apache.poi.ss.usermodel.PrintSetup.LEGAL_PAPERSIZE
                "Letter" -> sheet.printSetup.paperSize = org.apache.poi.ss.usermodel.PrintSetup.LETTER_PAPERSIZE
            }
            
            // Set print margins for better layout
            sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.TopMargin, 0.75)
            sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.BottomMargin, 0.75)
            sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.LeftMargin, 0.7)
            sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.RightMargin, 0.7)
            
            // Create header style
            val headerStyle = workbook.createCellStyle()
            val headerFont = workbook.createFont()
            headerFont.bold = true
            headerFont.color = IndexedColors.WHITE.index
            headerStyle.setFont(headerFont)
            headerStyle.fillForegroundColor = IndexedColors.DARK_BLUE.index
            headerStyle.fillPattern = FillPatternType.SOLID_FOREGROUND
            headerStyle.alignment = HorizontalAlignment.CENTER
            headerStyle.verticalAlignment = VerticalAlignment.CENTER
              // Create title style
            val titleStyle = workbook.createCellStyle()
            val titleFont = workbook.createFont()
            titleFont.bold = true
            titleFont.fontHeightInPoints = 16
            titleStyle.setFont(titleFont)
            titleStyle.alignment = HorizontalAlignment.CENTER
            
            // Create info style
            val infoStyle = workbook.createCellStyle()
            val infoFont = workbook.createFont()
            infoFont.italic = true
            infoStyle.setFont(infoFont)
            
            // Create title row
            val titleRow = sheet.createRow(0)
            val titleCell = titleRow.createCell(0)
            titleCell.setCellValue(customTitle)
            titleCell.cellStyle = titleStyle
            
            // Merge title cells
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, 6))
            
            // Create info rows
            val dateFormat = SimpleDateFormat("dd/MM/yyyy HH:mm:ss", Locale.getDefault())
            val infoRow1 = sheet.createRow(1)
            val infoCell1 = infoRow1.createCell(0)
            infoCell1.setCellValue("${languageManager.getString("export_date")}: ${dateFormat.format(Date())}")
            infoCell1.cellStyle = infoStyle
            
            val infoRow2 = sheet.createRow(2)
            val infoCell2 = infoRow2.createCell(0)
            infoCell2.setCellValue("${languageManager.getString("export_period")}: ${languageManager.getString(periodOptions[selectedPeriod])}")
            infoCell2.cellStyle = infoStyle
              val infoRow3 = sheet.createRow(3)
            val infoCell3 = infoRow3.createCell(0)
            infoCell3.setCellValue("${languageManager.getString("total_expenses")}: ${expenses.size}")
            infoCell3.cellStyle = infoStyle
              val infoRow4 = sheet.createRow(4)
            val infoCell4 = infoRow4.createCell(0)
            infoCell4.setCellValue("Paper Size: $selectedPaperSize")
            infoCell4.cellStyle = infoStyle              // Create header row for data
            val headerRow = sheet.createRow(6)
            val headers = arrayOf(
                "No",
                languageManager.getString("date"),
                languageManager.getString("expense_name"),
                languageManager.getString("amount"),
                languageManager.getString("description"),
                "Remark"
            )
            
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }            // Add expense data
            android.util.Log.d("ExportActivity", "Starting to add ${expenses.size} expenses to Excel")
            expenses.forEachIndexed { index, expense ->
                val row = sheet.createRow(7 + index)
                
                // Convert amount to target currency
                val convertedAmount = if (expense.currency == selectedCurrency) {
                    currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
                } else {
                    // For now, just show the original amount with a note
                    currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
                }
                
                android.util.Log.d("ExportActivity", "Adding expense ${index + 1}: ${expense.name} - ${expense.price}")
                
                // New column order: No, Date, Name, Amount, Description, Remark
                row.createCell(0).setCellValue((index + 1).toString())  // No - Sequential number
                row.createCell(1).setCellValue(expense.date)  // Date
                row.createCell(2).setCellValue(expense.name)  // Name
                row.createCell(3).setCellValue(currencyManager.formatCurrency(currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)))  // Amount
                row.createCell(4).setCellValue(expense.description.ifEmpty { languageManager.getString("no_description") })  // Description
                row.createCell(5).setCellValue("")  // Remark - empty for now since ExpenseItem doesn't have this field
            }
            android.util.Log.d("ExportActivity", "Finished adding expenses to Excel")            // Set manual column widths for new order: No, Date, Name, Amount, Description, Remark
            sheet.setColumnWidth(0, 8 * 256)   // No column - 8 characters
            sheet.setColumnWidth(1, 12 * 256)  // Date column - 12 characters
            sheet.setColumnWidth(2, 20 * 256)  // Name column - 20 characters
            sheet.setColumnWidth(3, 15 * 256)  // Amount column - 15 characters
            sheet.setColumnWidth(4, 25 * 256)  // Description column - 25 characters  
            sheet.setColumnWidth(5, 15 * 256)  // Remark column - 15 characters
              // Save to file
            android.util.Log.d("ExportActivity", "Starting to write Excel file to URI: $uri")
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                android.util.Log.d("ExportActivity", "Writing workbook to output stream")
                workbook.write(outputStream)
                android.util.Log.d("ExportActivity", "Successfully wrote workbook to file")
            }
            workbook.close()
            android.util.Log.d("ExportActivity", "Excel export completed successfully")
            
            Toast.makeText(this, languageManager.getString("export_success"), Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            android.util.Log.e("ExportActivity", "Export failed", e)
            Toast.makeText(this, "${languageManager.getString("export_failed")}: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
      private fun loadFilteredExpenses(periodIndex: Int): List<ExpenseItem> {
        val expensesJson = sharedPreferences.getString("expenses", "[]") ?: "[]"
        val type = object : TypeToken<List<ExpenseItem>>() {}.type
        val allExpenses: List<ExpenseItem> = gson.fromJson(expensesJson, type) ?: emptyList()
        
        // Debug logging
        android.util.Log.d("ExportActivity", "Total expenses in storage: ${allExpenses.size}")
        
        // Filter out deleted expenses
        val activeExpenses = allExpenses.filter { !it.isDeleted }
        android.util.Log.d("ExportActivity", "Active expenses (not deleted): ${activeExpenses.size}")
        
        // Filter by period
        val calendar = Calendar.getInstance()
        val today = calendar.time
        val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
        
        val filteredExpenses = when (periodIndex) {
            0 -> { // Today
                val todayStr = dateFormat.format(today)
                android.util.Log.d("ExportActivity", "Filtering for today: $todayStr")
                activeExpenses.filter { expense ->
                    android.util.Log.d("ExportActivity", "Checking expense date: ${expense.date}")
                    expense.date == todayStr
                }
            }
            1 -> { // This week
                calendar.set(Calendar.DAY_OF_WEEK, calendar.firstDayOfWeek)
                val weekStart = calendar.time
                android.util.Log.d("ExportActivity", "Filtering for this week from: ${dateFormat.format(weekStart)} to: ${dateFormat.format(today)}")
                activeExpenses.filter { expense ->
                    try {
                        val expenseDate = dateFormat.parse(expense.date)
                        val isInRange = expenseDate != null && 
                                       (expenseDate.after(weekStart) || expenseDate == weekStart) && 
                                       (expenseDate.before(today) || expenseDate == today)
                        android.util.Log.d("ExportActivity", "Expense ${expense.name} (${expense.date}) in week range: $isInRange")
                        isInRange
                    } catch (e: Exception) {
                        android.util.Log.e("ExportActivity", "Error parsing date ${expense.date}: ${e.message}")
                        false
                    }
                }
            }
            2 -> { // This month
                calendar.set(Calendar.DAY_OF_MONTH, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val monthStart = calendar.time
                android.util.Log.d("ExportActivity", "Filtering for this month from: ${dateFormat.format(monthStart)} to: ${dateFormat.format(today)}")
                activeExpenses.filter { expense ->
                    try {
                        val expenseDate = dateFormat.parse(expense.date)
                        val isInRange = expenseDate != null && 
                                       (expenseDate.after(monthStart) || expenseDate == monthStart) && 
                                       (expenseDate.before(today) || expenseDate == today)
                        android.util.Log.d("ExportActivity", "Expense ${expense.name} (${expense.date}) in month range: $isInRange")
                        isInRange
                    } catch (e: Exception) {
                        android.util.Log.e("ExportActivity", "Error parsing date ${expense.date}: ${e.message}")
                        false
                    }
                }
            }
            3 -> { // This year
                calendar.set(Calendar.DAY_OF_YEAR, 1)
                calendar.set(Calendar.HOUR_OF_DAY, 0)
                calendar.set(Calendar.MINUTE, 0)
                calendar.set(Calendar.SECOND, 0)
                calendar.set(Calendar.MILLISECOND, 0)
                val yearStart = calendar.time
                android.util.Log.d("ExportActivity", "Filtering for this year from: ${dateFormat.format(yearStart)} to: ${dateFormat.format(today)}")
                activeExpenses.filter { expense ->
                    try {
                        val expenseDate = dateFormat.parse(expense.date)
                        val isInRange = expenseDate != null && 
                                       (expenseDate.after(yearStart) || expenseDate == yearStart) && 
                                       (expenseDate.before(today) || expenseDate == today)
                        android.util.Log.d("ExportActivity", "Expense ${expense.name} (${expense.date}) in year range: $isInRange")
                        isInRange
                    } catch (e: Exception) {
                        android.util.Log.e("ExportActivity", "Error parsing date ${expense.date}: ${e.message}")
                        false
                    }
                }
            }
            else -> {
                android.util.Log.d("ExportActivity", "No period filter, returning all active expenses")
                activeExpenses
            }
        }
        
        android.util.Log.d("ExportActivity", "Filtered expenses count: ${filteredExpenses.size}")
        return filteredExpenses
    }
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this, MainActivity::class.java))
            }
            R.id.nav_summary -> {
                startActivity(Intent(this, SummaryActivity::class.java))
            }
            R.id.nav_all_list -> {
                startActivity(Intent(this, AllListActivity::class.java))
            }
            R.id.nav_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
            }            R.id.nav_currency_exchange -> {
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
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    @Suppress("DEPRECATION")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
    
    override fun onResume() {
        super.onResume()
        updateUITexts()
    }
}
