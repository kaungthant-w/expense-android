package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.ImageButton
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.button.MaterialButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText

class CurrencyExchangeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    private lateinit var currencyManager: CurrencyManager
    private lateinit var currencyApiService: CurrencyApiService
    private lateinit var sharedPreferences: SharedPreferences
    
    // Navigation Drawer components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    
    private lateinit var cardUsd: CardView
    private lateinit var cardMmk: CardView
    private lateinit var textCurrentRate: TextView
    private lateinit var buttonFetchRate: MaterialButton
    private lateinit var buttonCustomRate: MaterialButton
    private lateinit var buttonSwitchCurrency: MaterialButton
    private lateinit var layoutCustomRateInput: LinearLayout
    private lateinit var editTextCustomRate: TextInputEditText
    private lateinit var buttonApplyCustomRate: MaterialButton
    private lateinit var buttonCancelCustomRate: MaterialButton
    
    // Currency exchange table views
    private lateinit var buttonRefreshRates: MaterialButton
    private lateinit var textRateUsd: TextView
    private lateinit var textRateEur: TextView
    private lateinit var textRateSgd: TextView
    private lateinit var textRateMyr: TextView
    private lateinit var textRateCny: TextView
    private lateinit var textRateThb: TextView
    private lateinit var textRateJpy: TextView
    private lateinit var textLastUpdated: TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_currency_exchange)
        
        currencyManager = CurrencyManager.getInstance(this)
        currencyApiService = CurrencyApiService(this)
        sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
        setupActionBar()
        initViews()
        setupNavigationDrawer()
        setupClickListeners()
        updateUI()
        
        // Load initial exchange rates
        fetchAllExchangeRates()
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
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
        supportActionBar?.title = "💱 Currency Exchange"
    }
    
    private fun initViews() {
        // Navigation drawer components
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        
        // Setup back button click listener
        findViewById<ImageButton>(R.id.buttonBack).setOnClickListener {
            finish()
        }
        
        cardUsd = findViewById(R.id.cardUsd)
        cardMmk = findViewById(R.id.cardMmk)
        textCurrentRate = findViewById(R.id.textCurrentRate)
        buttonFetchRate = findViewById(R.id.buttonFetchRate)
        buttonCustomRate = findViewById(R.id.buttonCustomRate)
        buttonSwitchCurrency = findViewById(R.id.buttonSwitchCurrency)
        layoutCustomRateInput = findViewById(R.id.layoutCustomRateInput)
        editTextCustomRate = findViewById(R.id.editTextCustomRate)
        buttonApplyCustomRate = findViewById(R.id.buttonApplyCustomRate)
        buttonCancelCustomRate = findViewById(R.id.buttonCancelCustomRate)
        
        // Currency exchange table views
        buttonRefreshRates = findViewById(R.id.buttonRefreshRates)
        textRateUsd = findViewById(R.id.textRateUsd)
        textRateEur = findViewById(R.id.textRateEur)
        textRateSgd = findViewById(R.id.textRateSgd)
        textRateMyr = findViewById(R.id.textRateMyr)
        textRateCny = findViewById(R.id.textRateCny)
        textRateThb = findViewById(R.id.textRateThb)
        textRateJpy = findViewById(R.id.textRateJpy)
        textLastUpdated = findViewById(R.id.textLastUpdated)
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
        cardUsd.setOnClickListener {
            switchToCurrency(CurrencyManager.CURRENCY_USD)
        }
        
        cardMmk.setOnClickListener {
            switchToCurrency(CurrencyManager.CURRENCY_MMK)
        }
        
        buttonFetchRate.setOnClickListener {
            fetchLatestExchangeRate()
        }
        
        buttonCustomRate.setOnClickListener {
            showCustomRateInput()
        }
        
        buttonApplyCustomRate.setOnClickListener {
            applyCustomRate()
        }
        
        buttonCancelCustomRate.setOnClickListener {
            hideCustomRateInput()
        }
        
        buttonSwitchCurrency.setOnClickListener {
            val currentCurrency = currencyManager.getCurrentCurrency()
            val newCurrency = if (currentCurrency == CurrencyManager.CURRENCY_USD) {
                CurrencyManager.CURRENCY_MMK
            } else {
                CurrencyManager.CURRENCY_USD
            }
            switchToCurrency(newCurrency)
        }
        
        buttonRefreshRates.setOnClickListener {
            fetchAllExchangeRates()
        }
    }
    
    private fun switchToCurrency(currency: String) {
        currencyManager.setCurrentCurrency(currency)
        updateUI()
        
        val message = when (currency) {
            CurrencyManager.CURRENCY_USD -> "Switched to USD - Viewing original amounts"
            CurrencyManager.CURRENCY_MMK -> "Switched to MMK - Converting amounts using exchange rate"
            else -> "Currency switched"
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun updateUI() {
        val currentCurrency = currencyManager.getCurrentCurrency()
        val exchangeRate = currencyManager.getExchangeRate()
        
        // Update currency selection visual feedback
        updateCurrencySelection(currentCurrency)
        
        // Update exchange rate display
        textCurrentRate.text = "Exchange Rate: 1 USD = ${String.format("%.2f", exchangeRate)} MMK"
        
        // Update switch button text
        buttonSwitchCurrency.text = when (currentCurrency) {
            CurrencyManager.CURRENCY_USD -> "💱 Switch to MMK"
            CurrencyManager.CURRENCY_MMK -> "💱 Switch to USD"
            else -> "💱 Switch Currency"
        }
    }
    
    private fun updateCurrencySelection(currency: String) {
        when (currency) {
            CurrencyManager.CURRENCY_USD -> {
                cardUsd.setCardBackgroundColor(getColor(android.R.color.holo_blue_light))
                cardMmk.setCardBackgroundColor(getColor(android.R.color.white))
            }
            CurrencyManager.CURRENCY_MMK -> {
                cardMmk.setCardBackgroundColor(getColor(android.R.color.holo_green_light))
                cardUsd.setCardBackgroundColor(getColor(android.R.color.white))
            }
        }
    }
    
    private fun showCustomRateInput() {
        layoutCustomRateInput.visibility = View.VISIBLE
        editTextCustomRate.setText(currencyManager.getExchangeRate().toString())
        editTextCustomRate.requestFocus()
        Toast.makeText(this, "💡 Enter a custom exchange rate", Toast.LENGTH_SHORT).show()
    }
    
    private fun hideCustomRateInput() {
        layoutCustomRateInput.visibility = View.GONE
        editTextCustomRate.text?.clear()
    }
    
    private fun applyCustomRate() {
        val customRateText = editTextCustomRate.text.toString().trim()
        
        if (customRateText.isEmpty()) {
            Toast.makeText(this, "❌ Please enter an exchange rate", Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val customRate = customRateText.toDouble()
            
            if (customRate <= 0) {
                Toast.makeText(this, "❌ Exchange rate must be greater than 0", Toast.LENGTH_SHORT).show()
                return
            }
            
            if (customRate < 100 || customRate > 10000) {
                Toast.makeText(this, "⚠️ Are you sure? Rate seems unusual (${customRate} MMK per USD)", Toast.LENGTH_LONG).show()
            }
            
            // Apply the custom rate
            currencyManager.setExchangeRate(customRate)
            hideCustomRateInput()
            updateUI()
            
            Toast.makeText(this,
                "✅ Custom rate applied: 1 USD = ${String.format("%.2f", customRate)} MMK", 
                Toast.LENGTH_LONG).show()
                
        } catch (e: NumberFormatException) {
            Toast.makeText(this, "❌ Invalid number format. Please enter a valid decimal number", Toast.LENGTH_LONG).show()
        }
    }
    
    private fun fetchLatestExchangeRate() {
        buttonFetchRate.isEnabled = false
        buttonFetchRate.text = "⏳ Fetching..."
        
        currencyApiService.fetchLatestExchangeRate(object : CurrencyApiService.CurrencyRateCallback {
            override fun onSuccess(usdToMmk: Double) {
                runOnUiThread {
                    currencyManager.setExchangeRate(usdToMmk)
                    updateUI()
                    buttonFetchRate.isEnabled = true
                    buttonFetchRate.text = "🔄 Fetch Latest Rate"
                    Toast.makeText(this@CurrencyExchangeActivity, 
                        "✅ Rate updated: 1 USD = ${String.format("%.2f", usdToMmk)} MMK", 
                        Toast.LENGTH_LONG).show()
                }
            }
            
            override fun onError(error: String) {
                runOnUiThread {
                    buttonFetchRate.isEnabled = true
                    buttonFetchRate.text = "🔄 Fetch Latest Rate"
                    
                    // Show error message and suggest custom rate input
                    val errorMsg = "❌ Failed to fetch rate: $error\n💡 Try using 'Custom Rate' to set manually"
                    Toast.makeText(this@CurrencyExchangeActivity, errorMsg, Toast.LENGTH_LONG).show()
                    
                    // Automatically show custom rate input on API failure
                    showCustomRateInput()
                }
            }
        })
    }
    
    private fun fetchAllExchangeRates() {
        buttonRefreshRates.isEnabled = false
        buttonRefreshRates.text = "⏳ Loading..."
        
        // Reset all rates to loading state
        textRateUsd.text = "Loading..."
        textRateEur.text = "Loading..."
        textRateSgd.text = "Loading..."
        textRateMyr.text = "Loading..."
        textRateCny.text = "Loading..."
        textRateThb.text = "Loading..."
        textRateJpy.text = "Loading..."
        
        currencyApiService.fetchAllExchangeRates(object : CurrencyApiService.AllRatesCallback {
            override fun onSuccess(rates: Map<String, Double>) {
                runOnUiThread {
                    updateExchangeRateTable(rates)
                    buttonRefreshRates.isEnabled = true
                    buttonRefreshRates.text = "🔄 Refresh"
                    
                    val currentTime = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    textLastUpdated.text = "📅 Last updated: $currentTime"
                    
                    Toast.makeText(this@CurrencyExchangeActivity, 
                        "✅ Exchange rates updated successfully", 
                        Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onError(error: String) {
                runOnUiThread {
                    buttonRefreshRates.isEnabled = true
                    buttonRefreshRates.text = "🔄 Refresh"
                    
                    // Set error state for all rates
                    textRateUsd.text = "Error"
                    textRateEur.text = "Error"
                    textRateSgd.text = "Error"
                    textRateMyr.text = "Error"
                    textRateCny.text = "Error"
                    textRateThb.text = "Error"
                    textRateJpy.text = "Error"
                    textLastUpdated.text = "📅 Failed to update rates"
                    
                    Toast.makeText(this@CurrencyExchangeActivity, 
                        "❌ Failed to fetch rates: $error", 
                        Toast.LENGTH_LONG).show()
                }
            }
        })
    }
    
    private fun updateExchangeRateTable(rates: Map<String, Double>) {
        // Update USD rate (this is also used for the main currency conversion)
        rates["USD"]?.let { usdRate ->
            textRateUsd.text = String.format("%.2f", usdRate)
            
            // Also update the main currency manager rate
            currencyManager.setExchangeRate(usdRate)
            updateUI()
        } ?: run {
            textRateUsd.text = "N/A"
        }
        
        // Update other currency rates
        rates["EUR"]?.let { textRateEur.text = String.format("%.2f", it) } ?: run { textRateEur.text = "N/A" }
        rates["SGD"]?.let { textRateSgd.text = String.format("%.2f", it) } ?: run { textRateSgd.text = "N/A" }
        rates["MYR"]?.let { textRateMyr.text = String.format("%.2f", it) } ?: run { textRateMyr.text = "N/A" }
        rates["CNY"]?.let { textRateCny.text = String.format("%.2f", it) } ?: run { textRateCny.text = "N/A" }
        rates["THB"]?.let { textRateThb.text = String.format("%.2f", it) } ?: run { textRateThb.text = "N/A" }
        rates["JPY"]?.let { textRateJpy.text = String.format("%.2f", it) } ?: run { textRateJpy.text = "N/A" }
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
}
