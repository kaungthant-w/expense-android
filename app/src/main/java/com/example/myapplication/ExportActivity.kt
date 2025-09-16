package com.example.myapplication

import android.Manifest
import android.app.Activity
import android.bluetooth.BluetoothDevice
import android.content.Context
import android.content.DialogInterface
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.net.Uri
import android.net.wifi.WifiManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import org.apache.poi.ss.usermodel.*
import java.io.IOException
import java.io.OutputStream
import java.net.InetSocketAddress
import java.net.Socket
import java.nio.charset.Charset
import kotlinx.coroutines.*
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
    private lateinit var buttonPrintBluetooth: Button
    private lateinit var cardViewDeviceDiscovery: CardView
    private lateinit var textViewDeviceStatus: TextView
    private lateinit var progressBarDeviceSearch: ProgressBar
    private lateinit var recyclerViewDevices: RecyclerView    // Data and Managers
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var currencyManager: CurrencyManager
    private lateinit var bluetoothPrinterManager: BluetoothPrinterManager
    private lateinit var bluetoothDeviceAdapter: BluetoothDeviceAdapter
    private val gson = Gson()
    
    // WiFi Printing variables
    private lateinit var wifiManager: WifiManager
    private var wifiPrinterIP: String = ""
    private var wifiPrinterPort: Int = 9100 // Standard ESC/POS port
    private val wifiPrintScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    
    // Permission request codes
    private val BLUETOOTH_PERMISSION_REQUEST_CODE = 1001
    private val WIFI_PERMISSION_REQUEST_CODE = 1002
      // Export launchers
    private lateinit var excelExportLauncher: ActivityResultLauncher<Intent>
    
    // Data arrays for spinners
    private val periodOptions = arrayOf("export_period_today", "export_period_this_week", "export_period_this_month", "export_period_this_year")
    private val paperSizeOptions = arrayOf("A4", "Legal", "Letter", "Roller48", "Roller58", "Roller80", "Roller112")
    private val paperSizeLabels = arrayOf(
        "📄 A4 (210 × 297 mm)", 
        "📄 Legal (216 × 356 mm)", 
        "📄 Letter (216 × 279 mm)",
        "🧾 Roller 48mm (1.9 inch)",
        "🧾 Roller 58mm (2.3 inch)", 
        "🧾 Roller 80mm (3.1 inch)",
        "🧾 Roller 112mm (4.4 inch)"
    )
    
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
        }    }
    
    private fun initializeComponents() {
        // Initialize navigation drawer
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        
        // Initialize UI components
        editTextCustomTitle = findViewById(R.id.editTextCustomTitle)
        spinnerExportPeriod = findViewById(R.id.spinnerExportPeriod)
        spinnerPaperSize = findViewById(R.id.spinnerPaperSize)
        buttonSaveAsExcel = findViewById(R.id.buttonSaveAsExcel)
        buttonPrintWirelessly = findViewById(R.id.buttonPrintWirelessly)
        buttonPrintBluetooth = findViewById(R.id.buttonPrintBluetooth)
        cardViewDeviceDiscovery = findViewById(R.id.cardViewDeviceDiscovery)
        textViewDeviceStatus = findViewById(R.id.textViewDeviceStatus)
        progressBarDeviceSearch = findViewById(R.id.progressBarDeviceSearch)
        recyclerViewDevices = findViewById(R.id.recyclerViewDevices)        // Initialize data components
        sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
        currencyManager = CurrencyManager.getInstance(this)
        
        // Initialize WiFi components
        wifiManager = applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        loadWifiPrinterSettings()
        
        // Initialize Bluetooth components
        bluetoothPrinterManager = BluetoothPrinterManager(this)
        bluetoothPrinterManager.setCallback(bluetoothCallback)
        setupBluetoothDeviceList()
        
        // Set default title
        editTextCustomTitle.setText(languageManager.getString("app_name") + " " + languageManager.getString("report_export"))
    }    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, null,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        navigationView.setNavigationItemSelectedListener(this)
        updateNavigationMenuTitles()
    }
    
    private fun updateNavigationMenuTitles() {
        val menu = navigationView.menu
        menu.findItem(R.id.nav_home)?.title = languageManager.getString("nav_home")
        menu.findItem(R.id.nav_all_list)?.title = languageManager.getString("nav_all_list")
        menu.findItem(R.id.nav_history)?.title = languageManager.getString("nav_history")
        menu.findItem(R.id.nav_summary)?.title = languageManager.getString("nav_summary")
        menu.findItem(R.id.nav_currency_exchange)?.title = languageManager.getString("nav_currency_exchange")
        menu.findItem(R.id.nav_export_excel)?.title = languageManager.getString("nav_export_excel")
        menu.findItem(R.id.nav_settings)?.title = languageManager.getString("nav_settings")
        menu.findItem(R.id.nav_feedback)?.title = languageManager.getString("nav_feedback")
        menu.findItem(R.id.nav_about)?.title = languageManager.getString("nav_about")
    }
    
    private fun setupBluetoothDeviceList() {
        bluetoothDeviceAdapter = BluetoothDeviceAdapter { device ->
            onBluetoothDeviceSelected(device)
        }
        recyclerViewDevices.layoutManager = LinearLayoutManager(this)
        recyclerViewDevices.adapter = bluetoothDeviceAdapter
    }
      private val bluetoothCallback = object : BluetoothPrinterManager.BluetoothPrinterCallback {
        override fun onDeviceFound(device: BluetoothDevice, deviceName: String) {
            runOnUiThread {
                bluetoothDeviceAdapter.addDevice(device)
                textViewDeviceStatus.text = "Found: ${bluetoothDeviceAdapter.itemCount} devices"
            }
        }
        
        override fun onDiscoveryFinished() {
            runOnUiThread {
                progressBarDeviceSearch.visibility = View.GONE
                val deviceCount = bluetoothDeviceAdapter.itemCount
                textViewDeviceStatus.text = if (deviceCount > 0) {
                    "Found $deviceCount device(s). Tap to connect."
                } else {
                    languageManager.getString("no_devices_found")
                }
            }        }
        
        override fun onConnectionSuccess() {
            runOnUiThread {
                textViewDeviceStatus.text = "Connected successfully"
                Toast.makeText(this@ExportActivity, "Connected to printer", Toast.LENGTH_SHORT).show()
            }
        }
        
        override fun onConnectionFailed(error: String) {
            runOnUiThread {
                textViewDeviceStatus.text = "Connection failed: $error"
                Toast.makeText(this@ExportActivity, "Connection failed: $error", Toast.LENGTH_LONG).show()
            }
        }
        
        override fun onPrintSuccess() {
            runOnUiThread {
                Toast.makeText(this@ExportActivity, "Print completed successfully!", Toast.LENGTH_SHORT).show()
            }
        }
        
        override fun onPrintFailed(error: String) {
            runOnUiThread {
                Toast.makeText(this@ExportActivity, "Print failed: $error", Toast.LENGTH_LONG).show()
            }
        }
        
        override fun onDisconnected() {
            runOnUiThread {
                textViewDeviceStatus.text = "Disconnected"
                Toast.makeText(this@ExportActivity, "Disconnected from printer", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun onBluetoothDeviceSelected(device: BluetoothDevice) {
        if (checkBluetoothPermissions()) {
            bluetoothPrinterManager.connectToDevice(device)
        } else {
            requestBluetoothPermissions()
        }
    }
    
    private fun setupClickListeners() {
        // Back button
        findViewById<ImageButton>(R.id.buttonBack).setOnClickListener {
            finish()        }
        
        // Excel export button
        buttonSaveAsExcel.setOnClickListener {
            showExcelExportDialog()
        }
        
        // Print wirelessly button (WiFi printing)
        buttonPrintWirelessly.setOnClickListener {
            showWifiPrintDialog()
        }
        
        // Print Bluetooth button
        buttonPrintBluetooth.setOnClickListener {
            showBluetoothPrintDialog()
        }
    }
      private fun setupSpinners() {
        // Export Period Spinner
        val periodAdapter = object : ArrayAdapter<String>(this, R.layout.spinner_item, periodOptions) {
            override fun getView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                (view as TextView).text = languageManager.getString(periodOptions[position])
                return view            }
            
            override fun getDropDownView(position: Int, convertView: View?, parent: android.view.ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                (view as TextView).text = languageManager.getString(periodOptions[position])
                return view
            }
        }
        periodAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
        spinnerExportPeriod.adapter = periodAdapter
        
        // Paper Size Spinner
        val paperSizeAdapter = ArrayAdapter(this, R.layout.spinner_item, paperSizeLabels)
        paperSizeAdapter.setDropDownViewResource(R.layout.spinner_dropdown_item)
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
        }    }
    
    private fun updateUITexts() {
        // Update static text elements
        findViewById<TextView>(R.id.textViewTitle).text = languageManager.getString("export_comprehensive_title")
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
        
        // Update device status        textViewDeviceStatus.text = languageManager.getString("device_search_ready")
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
            }            .setNegativeButton(languageManager.getString("cancel"), null)
            .show()
    }      private fun showWifiPrintDialog() {
        val message = """
            ${languageManager.getString("wifi_print_message")}
            
            ${languageManager.getString("wifi_recommendations_title")}
            ${languageManager.getString("wifi_recommendation_connection")}
            ${languageManager.getString("wifi_recommendation_compatibility")}
            ${languageManager.getString("wifi_recommendation_quality")}
            ${languageManager.getString("wifi_recommendation_data")}
            
            ${languageManager.getString("wifi_ip_requirement")}
        """.trimIndent()
        
        AlertDialog.Builder(this)
            .setTitle("📡 " + languageManager.getString("wifi_print"))
            .setMessage(message)
            .setPositiveButton(languageManager.getString("configure_printer")) { _, _ ->
                showWifiPrinterSetupDialog()
            }
            .setNeutralButton(languageManager.getString("discover_network")) { _, _ ->
                startWifiPrinterDiscovery()
            }
            .setNegativeButton(languageManager.getString("cancel"), null)
            .show()
    }
    
    private fun showBluetoothPrintDialog() {
        AlertDialog.Builder(this)
            .setTitle("📱 " + languageManager.getString("wireless_print"))
            .setMessage(languageManager.getString("wireless_print_message"))
            .setPositiveButton(languageManager.getString("discover_devices")) { _, _ ->
                startDeviceDiscovery()
            }
            .setNegativeButton(languageManager.getString("cancel"), null)            .show()
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
        if (checkBluetoothPermissions()) {
            bluetoothDeviceAdapter.clearDevices()
            bluetoothPrinterManager.startDeviceDiscovery()
        } else {
            requestBluetoothPermissions()
        }
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
            
            // Check if it's a roller paper size
            val isRollerPaper = selectedPaperSize.startsWith("Roller")
            
            // Load expense data
            val expenses = loadFilteredExpenses(selectedPeriod)
              // Debug logging
            android.util.Log.d("ExportActivity", "Loaded ${expenses.size} expenses for export")
            android.util.Log.d("ExportActivity", "Selected paper size: $selectedPaperSize (Roller: $isRollerPaper)")
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
                // Roller papers use A4 as base but with different formatting
                else -> sheet.printSetup.paperSize = org.apache.poi.ss.usermodel.PrintSetup.A4_PAPERSIZE
            }
            
            // Set print margins - tighter for roller papers
            if (isRollerPaper) {
                sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.TopMargin, 0.25)
                sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.BottomMargin, 0.25)
                sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.LeftMargin, 0.1)
                sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.RightMargin, 0.1)
            } else {
                sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.TopMargin, 0.75)
                sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.BottomMargin, 0.75)
                sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.LeftMargin, 0.7)
                sheet.setMargin(org.apache.poi.ss.usermodel.Sheet.RightMargin, 0.7)
            }
            
            // Create styles with different font sizes for roller vs regular paper
            val baseFontSize: Short = if (isRollerPaper) 8 else 11
            val headerFontSize: Short = if (isRollerPaper) 9 else 11
            val titleFontSize: Short = if (isRollerPaper) 10 else 16
            
            // Create header style
            val headerStyle = workbook.createCellStyle()
            val headerFont = workbook.createFont()
            headerFont.bold = true
            headerFont.fontHeightInPoints = headerFontSize
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
            titleFont.fontHeightInPoints = titleFontSize
            titleStyle.setFont(titleFont)
            titleStyle.alignment = HorizontalAlignment.CENTER
            
            // Create info style
            val infoStyle = workbook.createCellStyle()
            val infoFont = workbook.createFont()
            infoFont.italic = true
            infoFont.fontHeightInPoints = baseFontSize
            infoStyle.setFont(infoFont)
            
            // Create data style for roller papers
            val dataStyle = workbook.createCellStyle()
            val dataFont = workbook.createFont()
            dataFont.fontHeightInPoints = baseFontSize
            dataStyle.setFont(dataFont)
            
            // Create title row
            val titleRow = sheet.createRow(0)
            val titleCell = titleRow.createCell(0)
            titleCell.setCellValue(customTitle)
            titleCell.cellStyle = titleStyle
            
            // Merge title cells based on paper type
            val mergeColumns = if (isRollerPaper) 3 else 5
            sheet.addMergedRegion(org.apache.poi.ss.util.CellRangeAddress(0, 0, 0, mergeColumns))
            
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
            infoCell4.cellStyle = infoStyle              
            // Create header row for data with different columns based on paper type
            // Debug logging for language localization
            android.util.Log.d("ExportActivity", "Current language: ${languageManager.getCurrentLanguage()}")
            android.util.Log.d("ExportActivity", "Amount translation: '${languageManager.getString("amount")}'")
            android.util.Log.d("ExportActivity", "Date translation: '${languageManager.getString("date")}'")
            android.util.Log.d("ExportActivity", "Expense name translation: '${languageManager.getString("expense_name")}'")
            android.util.Log.d("ExportActivity", "Description translation: '${languageManager.getString("description")}'")
            android.util.Log.d("ExportActivity", "Remark translation: '${languageManager.getString("remark")}'")
            
            val headerRow = sheet.createRow(6)
            val headers = if (isRollerPaper) {
                // Roller paper: No, Date, Name, Amount (no Description, no Remark)
                arrayOf(
                    "No",
                    languageManager.getString("date"),
                    languageManager.getString("expense_name"),
                    languageManager.getString("amount")
                )
            } else {
                // Regular paper: No, Date, Name, Amount, Description, Remark
                arrayOf(
                    "No",
                    languageManager.getString("date"),
                    languageManager.getString("expense_name"),
                    languageManager.getString("amount"),
                    languageManager.getString("description"),
                    languageManager.getString("remark")
                )
            }
            
            // Debug logging for headers
            android.util.Log.d("ExportActivity", "Headers array: ${headers.joinToString(", ") { "'$it'" }}")
            android.util.Log.d("ExportActivity", "Is roller paper: $isRollerPaper")
            
            headers.forEachIndexed { index, header ->
                val cell = headerRow.createCell(index)
                cell.setCellValue(header)
                cell.cellStyle = headerStyle
            }            // Add expense data
            android.util.Log.d("ExportActivity", "Starting to add ${expenses.size} expenses to Excel")
            expenses.forEachIndexed { index, expense ->
                val row = sheet.createRow(7 + index)
                
                android.util.Log.d("ExportActivity", "Adding expense ${index + 1}: ${expense.name} - ${expense.price}")
                
                if (isRollerPaper) {
                    // Roller paper format: No, Date, Name, Amount
                    val noCell = row.createCell(0)
                    noCell.setCellValue((index + 1).toString())
                    noCell.cellStyle = dataStyle
                    
                    val dateCell = row.createCell(1)
                    dateCell.setCellValue(expense.date)
                    dateCell.cellStyle = dataStyle
                    
                    val nameCell = row.createCell(2)
                    nameCell.setCellValue(expense.name)
                    nameCell.cellStyle = dataStyle
                    
                    val amountCell = row.createCell(3)
                    amountCell.setCellValue(currencyManager.formatCurrency(currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)))
                    amountCell.cellStyle = dataStyle
                } else {
                    // Regular paper format: No, Date, Name, Amount, Description, Remark
                    row.createCell(0).setCellValue((index + 1).toString())  // No - Sequential number
                    row.createCell(1).setCellValue(expense.date)  // Date
                    row.createCell(2).setCellValue(expense.name)  // Name
                    row.createCell(3).setCellValue(currencyManager.formatCurrency(currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)))  // Amount
                    row.createCell(4).setCellValue(expense.description.ifEmpty { languageManager.getString("no_description") })  // Description
                    row.createCell(5).setCellValue("")  // Remark - empty for now since ExpenseItem doesn't have this field
                }
            }
            android.util.Log.d("ExportActivity", "Finished adding expenses to Excel")
            
            // Add total amount row if there are expenses
            if (expenses.isNotEmpty()) {
                val totalRowIndex = 7 + expenses.size
                val totalRow = sheet.createRow(totalRowIndex)
                
                // Create total style (bold)
                val totalStyle = workbook.createCellStyle()
                val totalFont = workbook.createFont()
                totalFont.bold = true
                totalFont.fontHeightInPoints = baseFontSize
                totalStyle.setFont(totalFont)
                totalStyle.alignment = HorizontalAlignment.CENTER
                
                // Calculate total amount
                var totalAmount = 0.0
                expenses.forEach { expense ->
                    totalAmount += currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
                }
                
                if (isRollerPaper) {
                    // For roller paper: put total in Name column (column 2)
                    val totalLabelCell = totalRow.createCell(2)
                    totalLabelCell.setCellValue(languageManager.getString("total_amount"))
                    totalLabelCell.cellStyle = totalStyle
                    
                    // Add total amount in Amount column (column 3)
                    val totalAmountCell = totalRow.createCell(3)
                    totalAmountCell.setCellValue(currencyManager.formatCurrency(totalAmount))
                    totalAmountCell.cellStyle = totalStyle
                } else {
                    // For regular paper: put total in Name column (column 2)
                    val totalLabelCell = totalRow.createCell(2)
                    totalLabelCell.setCellValue(languageManager.getString("total_amount"))
                    totalLabelCell.cellStyle = totalStyle
                    
                    // Add total amount in Amount column (column 3)
                    val totalAmountCell = totalRow.createCell(3)
                    totalAmountCell.setCellValue(currencyManager.formatCurrency(totalAmount))
                    totalAmountCell.cellStyle = totalStyle
                }
                
                android.util.Log.d("ExportActivity", "Added total amount row: ${currencyManager.formatCurrency(totalAmount)}")
            }
            
            // Set manual column widths based on paper type
            if (isRollerPaper) {
                // Roller paper: narrower columns for receipt format
                sheet.setColumnWidth(0, 6 * 256)   // No column - 6 characters
                sheet.setColumnWidth(1, 10 * 256)  // Date column - 10 characters
                sheet.setColumnWidth(2, 18 * 256)  // Name column - 18 characters
                sheet.setColumnWidth(3, 12 * 256)  // Amount column - 12 characters
            } else {
                // Regular paper: standard column widths
                sheet.setColumnWidth(0, 8 * 256)   // No column - 8 characters
                sheet.setColumnWidth(1, 12 * 256)  // Date column - 12 characters
                sheet.setColumnWidth(2, 20 * 256)  // Name column - 20 characters
                sheet.setColumnWidth(3, 15 * 256)  // Amount column - 15 characters
                sheet.setColumnWidth(4, 25 * 256)  // Description column - 25 characters  
                sheet.setColumnWidth(5, 15 * 256)  // Remark column - 15 characters
            }
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
      private fun checkBluetoothPermissions(): Boolean {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
          return permissions.all { permission ->
            ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
        }
    }
      private fun requestBluetoothPermissions() {
        val permissions = if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.S) {
            arrayOf(
                Manifest.permission.BLUETOOTH_SCAN,
                Manifest.permission.BLUETOOTH_CONNECT,
                Manifest.permission.ACCESS_FINE_LOCATION
            )        } else {
            arrayOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }
        
        ActivityCompat.requestPermissions(this, permissions, BLUETOOTH_PERMISSION_REQUEST_CODE)
    }
    
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        
        when (requestCode) {
            BLUETOOTH_PERMISSION_REQUEST_CODE -> {
                if (grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    startDeviceDiscovery()
                } else {
                    AlertDialog.Builder(this)
                        .setTitle("Permissions Required")
                        .setMessage("Bluetooth and location permissions are required to discover and connect to printers. Please grant these permissions to use wireless printing.")
                        .setPositiveButton("Grant Permissions") { _, _ ->
                            requestBluetoothPermissions()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
    }
    
    private fun showPrintOptionsDialog(device: BluetoothDevice) {
        val selectedPeriod = spinnerExportPeriod.selectedItemPosition
        val expenses = loadFilteredExpenses(selectedPeriod)
        
        if (expenses.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("No Data to Print")
                .setMessage("No expenses found for the selected period. Please add expenses or select a different time period.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        
        val selectedPaperSize = paperSizeOptions[spinnerPaperSize.selectedItemPosition]
        val paperWidth = when (selectedPaperSize) {
            "Roller48" -> 48
            "Roller58" -> 58
            "Roller80" -> 80
            "Roller112" -> 112
            else -> 80 // Default to 80mm for other paper sizes
        }
        
        val totalAmount = expenses.sumOf { it.price }
        val deviceName = device.name ?: "Unknown Printer"
        
        AlertDialog.Builder(this)
            .setTitle("🖨️ Print Receipt")
            .setMessage("Ready to print ${expenses.size} expenses to $deviceName\n\n" +
                    "Paper size: ${selectedPaperSize}\n" +
                    "Total amount: $%.2f\n\n" +
                    "Proceed with printing?".format(totalAmount))
            .setPositiveButton("Print") { _, _ ->
                printExpensesToDevice(device, expenses, paperWidth)
            }
            .setNegativeButton("Cancel") { _, _ ->
                bluetoothPrinterManager.disconnect()
            }
            .show()
    }
      private fun printExpensesToDevice(device: BluetoothDevice, expenses: List<ExpenseItem>, paperWidth: Int) {
        val customTitle = editTextCustomTitle.text.toString().ifEmpty { 
            languageManager.getString("app_name") + " " + languageManager.getString("report_export")
        }
        
        val currency = currencyManager.getCurrentCurrency()
        val totalAmount = expenses.sumOf { it.price }
        bluetoothPrinterManager.printReceipt(customTitle, expenses, totalAmount, currency, paperWidth)
    }

    // ...existing code...
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
            R.id.nav_export_excel -> {
                // Already in this activity, just close drawer
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
      override fun onDestroy() {
        super.onDestroy()
        if (::bluetoothPrinterManager.isInitialized) {
            bluetoothPrinterManager.disconnect()
        }
    }
    
    private fun loadWifiPrinterSettings() {
        wifiPrinterIP = sharedPreferences.getString("wifi_printer_ip", "") ?: ""
        wifiPrinterPort = sharedPreferences.getInt("wifi_printer_port", 9100)
    }
    
    private fun saveWifiPrinterSettings() {
        sharedPreferences.edit().apply {
            putString("wifi_printer_ip", wifiPrinterIP)
            putInt("wifi_printer_port", wifiPrinterPort)
            apply()
        }
    }
      private fun showWifiPrinterSetupDialog() {
        val dialogView = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            setPadding(50, 30, 50, 30)
        }
        
        val ipLabel = TextView(this).apply {
            text = "📡 Printer IP Address:"
            textSize = 16f
            setPadding(0, 0, 0, 10)
        }
        
        val ipEditText = EditText(this).apply {
            hint = "192.168.1.100"
            setText(wifiPrinterIP)
            inputType = android.text.InputType.TYPE_CLASS_PHONE
        }
        
        val portLabel = TextView(this).apply {
            text = "🔌 Port (default 9100):"
            textSize = 16f
            setPadding(0, 20, 0, 10)
        }
        
        val portEditText = EditText(this).apply {
            hint = "9100"
            setText(wifiPrinterPort.toString())
            inputType = android.text.InputType.TYPE_CLASS_NUMBER
        }
        
        val instructionText = TextView(this).apply {
            text = """
                📋 Instructions:
                • Check printer's network settings
                • Use WiFi router admin panel to find IP
                • Default port is usually 9100
                • Test connection before printing
            """.trimIndent()
            textSize = 12f
            setPadding(0, 20, 0, 0)
            setTextColor(ContextCompat.getColor(context, android.R.color.darker_gray))
        }
        
        dialogView.addView(ipLabel)
        dialogView.addView(ipEditText)
        dialogView.addView(portLabel)
        dialogView.addView(portEditText)
        dialogView.addView(instructionText)
        
        AlertDialog.Builder(this)
            .setTitle("📡 Configure WiFi Printer")
            .setView(dialogView)
            .setPositiveButton("Save & Test") { _, _ ->
                val ip = ipEditText.text.toString().trim()
                val port = portEditText.text.toString().toIntOrNull() ?: 9100
                
                if (ip.isNotEmpty()) {
                    wifiPrinterIP = ip
                    wifiPrinterPort = port
                    saveWifiPrinterSettings()
                    testWifiConnection()
                } else {
                    Toast.makeText(this, "Please enter a valid IP address", Toast.LENGTH_SHORT).show()
                }
            }
            .setNeutralButton("Print Test") { _, _ ->
                val ip = ipEditText.text.toString().trim()
                val port = portEditText.text.toString().toIntOrNull() ?: 9100
                
                if (ip.isNotEmpty()) {
                    wifiPrinterIP = ip
                    wifiPrinterPort = port
                    saveWifiPrinterSettings()
                    startWifiPrint()
                } else {
                    Toast.makeText(this, "Please enter a valid IP address", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun startWifiPrinterDiscovery() {
        if (!wifiManager.isWifiEnabled) {
            AlertDialog.Builder(this)
                .setTitle("📡 WiFi Required")
                .setMessage("WiFi is not enabled. Please enable WiFi to discover network printers.\n\nAfter enabling WiFi, restart this discovery.")
                .setPositiveButton("OK", null)
                .show()
            return
        }
        
        val progressDialog = AlertDialog.Builder(this)
            .setTitle("🔍 Discovering Network Printers...")
            .setMessage("Scanning network for printers...\nThis may take a few seconds.")
            .setCancelable(false)
            .create()
        
        progressDialog.show()
        
        wifiPrintScope.launch {
            val discoveredPrinters = discoverNetworkPrinters()
            
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                showDiscoveredPrinters(discoveredPrinters)
            }
        }
    }
    
    private suspend fun discoverNetworkPrinters(): List<String> {
        val printers = mutableListOf<String>()
        val wifiInfo = wifiManager.connectionInfo
        val dhcp = wifiManager.dhcpInfo
        
        if (dhcp != null) {
            val gateway = dhcp.gateway
            val subnet = gateway and 0xFFFFFF00.toInt()
            
            // Common printer ports
            val printerPorts = listOf(9100, 515, 631, 9101, 9102)
            
            // Scan common IP range (x.x.x.1 to x.x.x.254)
            for (i in 1..254) {
                val ip = subnet or i
                val ipStr = String.format(
                    "%d.%d.%d.%d",
                    ip and 0xFF,
                    (ip shr 8) and 0xFF,
                    (ip shr 16) and 0xFF,
                    (ip shr 24) and 0xFF
                )
                
                for (port in printerPorts) {
                    try {
                        val socket = Socket()
                        socket.connect(InetSocketAddress(ipStr, port), 500) // 500ms timeout
                        socket.close()
                        printers.add("$ipStr:$port")
                        break // Found one port, move to next IP
                    } catch (e: IOException) {
                        // Port not accessible, try next
                    }
                }
                
                // Limit discovery to prevent long waits
                if (printers.size >= 10) break
            }
        }
        
        return printers
    }
    
    private fun showDiscoveredPrinters(printers: List<String>) {
        if (printers.isEmpty()) {
            AlertDialog.Builder(this)
                .setTitle("🔍 Network Discovery")
                .setMessage("""
                    No network printers found automatically.
                    
                    Try these steps:
                    • Ensure printer is connected to same WiFi network
                    • Check printer's network settings for IP address
                    • Manually configure printer IP address
                    • Move closer to WiFi router for better connection
                """.trimIndent())
                .setPositiveButton("Manual Setup") { _, _ ->
                    showWifiPrinterSetupDialog()
                }
                .setNegativeButton("Cancel", null)
                .show()
        } else {
            val printerArray = printers.toTypedArray()
            AlertDialog.Builder(this)
                .setTitle("🖨️ Found Network Printers")
                .setItems(printerArray) { _, which ->
                    val selected = printerArray[which]
                    val parts = selected.split(":")
                    wifiPrinterIP = parts[0]
                    wifiPrinterPort = parts[1].toInt()
                    saveWifiPrinterSettings()
                    testWifiConnection()
                }
                .setNegativeButton("Cancel", null)
                .show()
        }
    }
    
    private fun testWifiConnection() {
        val progressDialog = AlertDialog.Builder(this)
            .setTitle("🔗 Testing Connection...")
            .setMessage("Connecting to $wifiPrinterIP:$wifiPrinterPort")
            .setCancelable(false)
            .create()
        
        progressDialog.show()
        
        wifiPrintScope.launch {
            var success = false
            var errorMessage = ""
            
            try {
                val socket = Socket()
                socket.connect(InetSocketAddress(wifiPrinterIP, wifiPrinterPort), 3000)
                socket.close()
                success = true
            } catch (e: IOException) {
                errorMessage = e.message ?: "Connection failed"
            }
            
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                
                if (success) {
                    AlertDialog.Builder(this@ExportActivity)
                        .setTitle("✅ Connection Successful")
                        .setMessage("Successfully connected to WiFi printer at $wifiPrinterIP:$wifiPrinterPort\n\nReady to print!")
                        .setPositiveButton("Print Now") { _, _ ->
                            startWifiPrint()
                        }
                        .setNegativeButton("OK", null)
                        .show()
                } else {
                    AlertDialog.Builder(this@ExportActivity)
                        .setTitle("❌ Connection Failed")
                        .setMessage("Could not connect to $wifiPrinterIP:$wifiPrinterPort\n\nError: $errorMessage\n\nPlease check:\n• Printer IP address\n• Network connection\n• Printer power status")
                        .setPositiveButton("Retry Setup") { _, _ ->
                            showWifiPrinterSetupDialog()
                        }
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
    }
    
    private fun startWifiPrint() {
        if (wifiPrinterIP.isEmpty()) {
            showWifiPrinterSetupDialog()
            return
        }
        
        val selectedPeriod = spinnerExportPeriod.selectedItemPosition
        val expenses = loadFilteredExpenses(selectedPeriod)
        
        if (expenses.isEmpty()) {
            Toast.makeText(this, "No expenses found for selected period", Toast.LENGTH_SHORT).show()
            return
        }
        
        val progressDialog = AlertDialog.Builder(this)
            .setTitle("🖨️ Printing via WiFi...")
            .setMessage("Sending data to printer at $wifiPrinterIP")
            .setCancelable(false)
            .create()
        
        progressDialog.show()
        
        wifiPrintScope.launch {
            var success = false
            var errorMessage = ""
            
            try {
                val printData = generateWifiPrintData(expenses)
                sendDataToPrinter(printData)
                success = true
            } catch (e: Exception) {
                errorMessage = e.message ?: "Print failed"
            }
            
            withContext(Dispatchers.Main) {
                progressDialog.dismiss()
                
                if (success) {
                    Toast.makeText(this@ExportActivity, "✅ Print completed successfully!", Toast.LENGTH_LONG).show()
                } else {
                    AlertDialog.Builder(this@ExportActivity)
                        .setTitle("❌ Print Failed")
                        .setMessage("Failed to print via WiFi\n\nError: $errorMessage")
                        .setPositiveButton("Retry", { _, _ -> startWifiPrint() })
                        .setNegativeButton("Cancel", null)
                        .show()
                }
            }
        }
    }
    
    private suspend fun sendDataToPrinter(data: ByteArray) {
        withContext(Dispatchers.IO) {
            val socket = Socket()
            socket.connect(InetSocketAddress(wifiPrinterIP, wifiPrinterPort), 5000)
            
            val outputStream = socket.getOutputStream()
            outputStream.write(data)
            outputStream.flush()
            
            // Wait a bit for printing to complete
            delay(1000)
            
            outputStream.close()
            socket.close()
        }
    }
      private fun generateWifiPrintData(expenses: List<ExpenseItem>): ByteArray {
        val customTitle = editTextCustomTitle.text.toString().ifEmpty { 
            languageManager.getString("app_name") + " " + languageManager.getString("report_export")
        }
        
        val selectedPeriod = spinnerExportPeriod.selectedItemPosition
        val periodName = when (selectedPeriod) {
            0 -> languageManager.getString("export_period_today")
            1 -> languageManager.getString("export_period_this_week")
            2 -> languageManager.getString("export_period_this_month")
            3 -> languageManager.getString("export_period_this_year")
            else -> "All"
        }
        
        // Get selected paper size and determine format
        val selectedPaperSize = paperSizeOptions[spinnerPaperSize.selectedItemPosition]
        val paperWidth = when (selectedPaperSize) {
            "Roller48" -> 48
            "Roller58" -> 58
            "Roller80" -> 80
            "Roller112" -> 112
            "A4" -> 210  // A4 width in mm
            "Legal" -> 216  // Legal width in mm
            "Letter" -> 216  // Letter width in mm
            else -> 80 // Default to 80mm
        }
        val isRollerPaper = selectedPaperSize.startsWith("Roller")
        
        val printContent = StringBuilder()
        
        // ESC/POS commands
        val ESC = 0x1B.toByte()
        val GS = 0x1D.toByte()
        
        // Initialize printer
        printContent.append(ESC.toInt().toChar())
        printContent.append("@") // Initialize
        
        // Set character set to UTF-8
        printContent.append(ESC.toInt().toChar())
        printContent.append("t")
        printContent.append(16.toChar()) // UTF-8
        
        // Center align and bold
        printContent.append(ESC.toInt().toChar())
        printContent.append("a")
        printContent.append(1.toChar()) // Center
        
        printContent.append(ESC.toInt().toChar())
        printContent.append("E")
        printContent.append(1.toChar()) // Bold on
        
        // Title
        printContent.append("$customTitle\n")
        printContent.append("================\n")
        
        // Date and period info
        printContent.append(ESC.toInt().toChar())
        printContent.append("E")
        printContent.append(0.toChar()) // Bold off
          val dateFormat = java.text.SimpleDateFormat("dd/MM/yyyy HH:mm", java.util.Locale.getDefault())
        printContent.append("${languageManager.getString("export_date")}: ${dateFormat.format(java.util.Date())}\n")
        printContent.append("${languageManager.getString("export_period")}: $periodName\n")
        printContent.append("${languageManager.getString("total_expenses")}: ${expenses.size}\n")
        printContent.append("Paper: $selectedPaperSize\n")
        printContent.append("WiFi: $wifiPrinterIP\n")
        
        // Different separator lengths based on paper width
        val separatorLength = when (paperWidth) {
            48 -> 32
            58 -> 38
            80 -> 48
            112 -> 64
            else -> if (isRollerPaper) 38 else 48  // Default for roller vs standard
        }
        val separator = "=".repeat(separatorLength)
        printContent.append("$separator\n\n")
          // Left align for expense list
        printContent.append(ESC.toInt().toChar())
        printContent.append("a")
        printContent.append(0.toChar()) // Left align
        
        if (isRollerPaper) {
            // Roller paper format - compact receipt style
            printContent.append("No  Item${" ".repeat(15)}Amount\n")
            val itemSeparator = "-".repeat(separatorLength)
            printContent.append("$itemSeparator\n")
            
            expenses.forEachIndexed { index, expense ->
                val amount = currencyManager.formatCurrency(
                    currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
                )
                
                // Format for different roller widths
                val itemFormat = when (paperWidth) {
                    48 -> "%-2d %-18s %7s"  // Very compact
                    58 -> "%-2d %-22s %9s"  // Compact
                    80 -> "%-2d %-28s %12s" // Standard receipt
                    112 -> "%-2d %-38s %18s" // Wide receipt
                    else -> "%-2d %-22s %9s" // Default compact
                }
                
                val truncatedName = if (expense.name.length > (paperWidth / 3)) {
                    expense.name.take(paperWidth / 3 - 3) + "..."
                } else {
                    expense.name
                }
                
                val line = String.format(itemFormat, index + 1, truncatedName, amount)
                printContent.append("$line\n")
                
                // Add date on next line for roller papers
                printContent.append("    ${expense.date}\n")
                if (expense.description.isNotEmpty() && paperWidth >= 80) {
                    val desc = if (expense.description.length > (paperWidth / 2)) {
                        expense.description.take(paperWidth / 2 - 3) + "..."
                    } else {
                        expense.description
                    }
                    printContent.append("    $desc\n")
                }
                printContent.append("\n")
            }
        } else {
            // Standard paper format - full information
            expenses.forEachIndexed { index, expense ->
                val amount = currencyManager.formatCurrency(
                    currencyManager.getDisplayAmountFromStored(expense.price, expense.currency)
                )
                
                printContent.append("${index + 1}. ${expense.name}\n")
                printContent.append("   Date: ${expense.date}\n")
                printContent.append("   Amount: $amount\n")
                if (expense.description.isNotEmpty()) {
                    printContent.append("   Description: ${expense.description}\n")
                }
                printContent.append("\n")
            }
        }
        
        // Total
        val totalAmount = expenses.sumOf { 
            currencyManager.getDisplayAmountFromStored(it.price, it.currency)
        }
          printContent.append("-".repeat(separatorLength) + "\n")
        printContent.append(ESC.toInt().toChar())
        printContent.append("E")
        printContent.append(1.toChar()) // Bold on
        
        printContent.append("${languageManager.getString("total_amount")}: ${currencyManager.formatCurrency(totalAmount)}\n")
        
        printContent.append(ESC.toInt().toChar())
        printContent.append("E")
        printContent.append(0.toChar()) // Bold off
        
        // Footer with recommendations - adjust for paper size
        printContent.append("\n" + "=".repeat(separatorLength) + "\n")
        
        if (isRollerPaper) {
            // Compact footer for roller papers
            printContent.append("📡 WiFi Tips:\n")
            if (paperWidth >= 80) {
                printContent.append("• Router နှင့်နီးကပ်စွာ ထားပါ\n")
                printContent.append("• $selectedPaperSize paper သုံးပါ\n")
                printContent.append("• Export ပြီးမှ print လုပ်ပါ\n")
            } else {
                // Very compact for small rollers
                printContent.append("• Router နီးကပ်စွာ\n")
                printContent.append("• ${selectedPaperSize}\n")
                printContent.append("• Export→Print\n")
            }
        } else {
            // Full footer for standard papers
            printContent.append("📡 WiFi Print Tips:\n")
            printContent.append("• Router နှင့်နီးကပ်စွာ ထားပါ\n")
            printContent.append("• $selectedPaperSize paper အသုံးပြုပါ\n")
            printContent.append("• Export ပြီးမှ print လုပ်ပါ\n")
            printContent.append("• Data saving အတွက် ကြိုတင်သိမ်းပါ\n")
        }
        printContent.append("=".repeat(separatorLength) + "\n")
        
        // Feed and cut
        printContent.append("\n\n\n")
        printContent.append(GS.toInt().toChar())
        printContent.append("V")
        printContent.append(0.toChar()) // Full cut
        
        return printContent.toString().toByteArray(Charset.forName("UTF-8"))
    }
}
