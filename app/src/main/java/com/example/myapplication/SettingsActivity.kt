package com.example.myapplication

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader
import java.io.OutputStreamWriter
import java.text.SimpleDateFormat
import java.util.*

class SettingsActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    // Navigation Drawer components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    
    // Data import/export launchers
    private lateinit var exportLauncher: ActivityResultLauncher<Intent>
    private lateinit var importLauncher: ActivityResultLauncher<Intent>
    
    private val gson = Gson()

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        setupActionBar()
        initViews()
        setupNavigationDrawer()
        setupLaunchers()
        setupClickListeners()
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
        supportActionBar?.title = "⚙️ Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        
        // Back button click listener
        findViewById<android.widget.ImageButton>(R.id.buttonBack).setOnClickListener {
            finish()
        }
    }
    
    private fun setupLaunchers() {
        // Export launcher
        exportLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    exportDataToFile(uri)
                }
            }
        }
        
        // Import launcher
        importLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
            if (result.resultCode == Activity.RESULT_OK) {
                result.data?.data?.let { uri ->
                    importDataFromFile(uri)
                }
            }
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

    private fun setupClickListeners() {
        // Theme Settings Card
        findViewById<CardView>(R.id.cardThemeSettings)?.setOnClickListener {
            startActivity(Intent(this, ThemeActivity::class.java))
        }
        
        // Export Data Card
        findViewById<CardView>(R.id.cardExportData)?.setOnClickListener {
            showExportConfirmationDialog()
        }
        
        // Import Data Card
        findViewById<CardView>(R.id.cardImportData)?.setOnClickListener {
            showImportConfirmationDialog()
        }
    }

    private fun showExportConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("📤 Export Data")
            .setMessage("This will export all your expense data to a backup file. You can use this file to restore your data later.\n\nDo you want to continue?")
            .setPositiveButton("Export") { _, _ ->
                startExportProcess()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun showImportConfirmationDialog() {
        AlertDialog.Builder(this)
            .setTitle("📥 Import Data")
            .setMessage("⚠️ WARNING: This will replace ALL your current expense data with the data from the backup file.\n\nThis action cannot be undone. Make sure you have a backup of your current data if needed.\n\nDo you want to continue?")
            .setPositiveButton("Import") { _, _ ->
                startImportProcess()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
    
    private fun startExportProcess() {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd_HH-mm-ss", Locale.getDefault())
        val timestamp = dateFormat.format(Date())
        val fileName = "expense_backup_$timestamp.json"
        
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
            putExtra(Intent.EXTRA_TITLE, fileName)
        }
        
        exportLauncher.launch(intent)
    }
    
    private fun startImportProcess() {
        val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/json"
        }
        
        importLauncher.launch(intent)
    }

    private fun exportDataToFile(uri: Uri) {
        try {
            // Get all expense data from SharedPreferences
            val sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
            val expensesJson = sharedPreferences.getString("expenses", "[]") ?: "[]"
            
            // Create export data structure
            val exportData = mapOf(
                "app_name" to "HsuPar Expense",
                "export_version" to "1.0",
                "export_date" to SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date()),
                "expenses" to expensesJson,
                "total_expenses" to getExpenseCount(expensesJson)
            )
            
            // Write to file
            contentResolver.openOutputStream(uri)?.use { outputStream ->
                OutputStreamWriter(outputStream).use { writer ->
                    gson.toJson(exportData, writer)
                }
            }
            
            Toast.makeText(this, "✅ Data exported successfully!", Toast.LENGTH_LONG).show()
            
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Export failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun importDataFromFile(uri: Uri) {
        try {
            // Read from file
            contentResolver.openInputStream(uri)?.use { inputStream ->
                InputStreamReader(inputStream).use { reader ->
                    val importData = gson.fromJson(reader, Map::class.java)
                    
                    // Validate import data
                    val expensesData = importData["expenses"] as? String
                    if (expensesData.isNullOrEmpty()) {
                        Toast.makeText(this, "❌ Invalid backup file: No expense data found", Toast.LENGTH_LONG).show()
                        return
                    }
                    
                    // Validate that it's proper expense data
                    try {
                        val type = object : TypeToken<List<ExpenseItem>>() {}.type
                        val expenses: List<ExpenseItem> = gson.fromJson(expensesData, type)
                        
                        // Save to SharedPreferences
                        val sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
                        sharedPreferences.edit()
                            .putString("expenses", expensesData)
                            .apply()
                        
                        val importInfo = StringBuilder()
                        importInfo.append("✅ Data imported successfully!\n\n")
                        importInfo.append("📊 Import Summary:\n")
                        importInfo.append("• Total expenses: ${expenses.size}\n")
                        importInfo.append("• Active expenses: ${expenses.count { !it.isDeleted }}\n")
                        importInfo.append("• Deleted expenses: ${expenses.count { it.isDeleted }}\n")
                        
                        importData["export_date"]?.let { exportDate ->
                            importInfo.append("• Export date: $exportDate\n")
                        }
                        
                        importInfo.append("\nRestart the app to see all imported data.")
                        
                        // Show success dialog
                        AlertDialog.Builder(this)
                            .setTitle("📥 Import Complete")
                            .setMessage(importInfo.toString())
                            .setPositiveButton("OK") { _, _ ->
                                // Optional: You could restart the app or refresh data here
                                Toast.makeText(this, "💡 Tip: Restart the app to see all imported data", Toast.LENGTH_LONG).show()
                            }
                            .show()
                            
                    } catch (e: Exception) {
                        Toast.makeText(this, "❌ Invalid backup file: Unable to parse expense data", Toast.LENGTH_LONG).show()
                    }
                }
            }
            
        } catch (e: Exception) {
            Toast.makeText(this, "❌ Import failed: ${e.message}", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun getExpenseCount(expensesJson: String): Int {
        return try {
            val type = object : TypeToken<List<ExpenseItem>>() {}.type
            val expenses: List<ExpenseItem> = gson.fromJson(expensesJson, type)
            expenses.size
        } catch (e: Exception) {
            0
        }
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
                // Already in this activity, just close drawer
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
    
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
