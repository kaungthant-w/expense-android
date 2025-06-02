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
    private lateinit var languageManager: LanguageManager
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
        languageManager = LanguageManager.getInstance(this)
        sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
        setupActionBar()
        initViews()
        setupNavigationDrawer()
        setupClickListeners()
        updateUI()
        updateNavigationMenuTitles()
        
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
        supportActionBar?.title = languageManager.getString("currency_exchange_title")
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
        
        // Set up static texts with translation
        setupStaticTexts()
    }
    
    private fun setupStaticTexts() {
        // Set all static text elements using LanguageManager
        try {
            // Main title
            findViewById<TextView>(R.id.textCurrencyExchangeTitle)?.text = 
                languageManager.getString("currency_exchange_title")
            
            // Live exchange rates section
            findViewById<TextView>(R.id.textLiveExchangeRatesTitle)?.text = 
                languageManager.getString("live_exchange_rates")
            buttonRefreshRates.text = languageManager.getString("refresh_rates")
            
            // Table headers
            findViewById<TextView>(R.id.textCurrencyLabel)?.text = 
                languageManager.getString("currency")
            findViewById<TextView>(R.id.textRateLabel)?.text = 
                languageManager.getString("rate_mmk")
            
            // Current exchange rate section
            findViewById<TextView>(R.id.textCurrentExchangeRateTitle)?.text = 
                languageManager.getString("current_exchange_rate")
            buttonFetchRate.text = languageManager.getString("fetch_latest_rate")
            buttonCustomRate.text = languageManager.getString("custom_rate")
            
            // Manual rate entry section
            findViewById<TextView>(R.id.textManualRateEntryTitle)?.text = 
                languageManager.getString("manual_rate_entry")
            findViewById<com.google.android.material.textfield.TextInputLayout>(R.id.textInputCustomRate)?.hint = 
                languageManager.getString("enter_rate_hint")
            buttonApplyCustomRate.text = languageManager.getString("apply_rate")
            buttonCancelCustomRate.text = languageManager.getString("cancel_rate")
            
            // Currency selection section
            findViewById<TextView>(R.id.textSelectDisplayCurrencyTitle)?.text = 
                languageManager.getString("select_display_currency")
            findViewById<TextView>(R.id.textUsDollarLabel)?.text = 
                languageManager.getString("us_dollar")
            findViewById<TextView>(R.id.textMyanmarKyatLabel)?.text = 
                languageManager.getString("myanmar_kyat")
                
        } catch (e: Exception) {
            // Log error but continue - fallback to hardcoded text in XML
            android.util.Log.e("CurrencyExchange", "Error setting up static texts: ${e.message}")
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
    }    private fun switchToCurrency(currency: String) {
        currencyManager.setCurrentCurrency(currency)
        updateUI()
        
        val message = when (currency) {
            CurrencyManager.CURRENCY_USD -> languageManager.getString("switched_to_usd")
            CurrencyManager.CURRENCY_MMK -> languageManager.getString("switched_to_mmk")
            else -> languageManager.getString("currency_switched")
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
      private fun updateUI() {
        val currentCurrency = currencyManager.getCurrentCurrency()
        val exchangeRate = currencyManager.getExchangeRate()
        
        // Update currency selection visual feedback
        updateCurrencySelection(currentCurrency)
          // Update exchange rate display
        textCurrentRate.text = languageManager.getString("exchange_rate_display")
            .replace("{rate}", String.format("%.2f", exchangeRate))
        
        // Update switch button text
        buttonSwitchCurrency.text = when (currentCurrency) {
            CurrencyManager.CURRENCY_USD -> languageManager.getString("switch_to_mmk")
            CurrencyManager.CURRENCY_MMK -> languageManager.getString("switch_to_usd")
            else -> languageManager.getString("switch_to_mmk")
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
        Toast.makeText(this, languageManager.getString("currency_exchange_enter_custom_rate"), Toast.LENGTH_SHORT).show()
    }
    
    private fun hideCustomRateInput() {
        layoutCustomRateInput.visibility = View.GONE
        editTextCustomRate.text?.clear()
    }
      private fun applyCustomRate() {
        val customRateText = editTextCustomRate.text.toString().trim()
        
        if (customRateText.isEmpty()) {
            Toast.makeText(this, languageManager.getString("currency_exchange_enter_rate_error"), Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val customRate = customRateText.toDouble()
            
            if (customRate <= 0) {
                Toast.makeText(this, languageManager.getString("currency_exchange_rate_positive_error"), Toast.LENGTH_SHORT).show()
                return
            }
            
            if (customRate < 100 || customRate > 10000) {
                Toast.makeText(this, languageManager.getString("currency_exchange_rate_unusual_warning")
                    .replace("{rate}", customRate.toString()), Toast.LENGTH_LONG).show()
            }
            
            // Apply the custom rate
            currencyManager.setExchangeRate(customRate)
            hideCustomRateInput()
            updateUI()
            
            Toast.makeText(this,
                languageManager.getString("currency_exchange_custom_rate_applied")
                    .replace("{rate}", String.format("%.2f", customRate)), 
                Toast.LENGTH_LONG).show()
                
        } catch (e: NumberFormatException) {
            Toast.makeText(this, languageManager.getString("currency_exchange_invalid_number_error"), Toast.LENGTH_LONG).show()
        }
    }
      private fun fetchLatestExchangeRate() {
        buttonFetchRate.isEnabled = false
        buttonFetchRate.text = languageManager.getString("currency_exchange_fetching")
        
        currencyApiService.fetchLatestExchangeRate(object : CurrencyApiService.CurrencyRateCallback {
            override fun onSuccess(usdToMmk: Double) {
                runOnUiThread {
                    currencyManager.setExchangeRate(usdToMmk)
                    updateUI()
                    buttonFetchRate.isEnabled = true
                    buttonFetchRate.text = languageManager.getString("currency_exchange_fetch_latest")
                    Toast.makeText(this@CurrencyExchangeActivity, 
                        languageManager.getString("currency_exchange_rate_updated")
                            .replace("{rate}", String.format("%.2f", usdToMmk)), 
                        Toast.LENGTH_LONG).show()
                }
            }
            
            override fun onError(error: String) {
                runOnUiThread {
                    buttonFetchRate.isEnabled = true
                    buttonFetchRate.text = languageManager.getString("currency_exchange_fetch_latest")
                    
                    // Show error message and suggest custom rate input
                    val errorMsg = languageManager.getString("currency_exchange_fetch_error")
                        .replace("{error}", error)
                    Toast.makeText(this@CurrencyExchangeActivity, errorMsg, Toast.LENGTH_LONG).show()
                    
                    // Automatically show custom rate input on API failure
                    showCustomRateInput()
                }
            }
        })
    }
      private fun fetchAllExchangeRates() {
        buttonRefreshRates.isEnabled = false
        buttonRefreshRates.text = languageManager.getString("currency_exchange_loading")
        
        // Reset all rates to loading state
        val loadingText = languageManager.getString("currency_exchange_loading_rates")
        textRateUsd.text = loadingText
        textRateEur.text = loadingText
        textRateSgd.text = loadingText
        textRateMyr.text = loadingText
        textRateCny.text = loadingText
        textRateThb.text = loadingText
        textRateJpy.text = loadingText
          currencyApiService.fetchAllExchangeRates(object : CurrencyApiService.AllRatesCallback {
            override fun onSuccess(rates: Map<String, Double>) {
                runOnUiThread {
                    updateExchangeRateTable(rates)
                    buttonRefreshRates.isEnabled = true
                    buttonRefreshRates.text = languageManager.getString("currency_exchange_refresh")
                    
                    val currentTime = java.text.SimpleDateFormat("MMM dd, yyyy HH:mm", java.util.Locale.getDefault())
                        .format(java.util.Date())
                    textLastUpdated.text = languageManager.getString("currency_exchange_last_updated")
                        .replace("{time}", currentTime)
                    
                    Toast.makeText(this@CurrencyExchangeActivity, 
                        languageManager.getString("currency_exchange_rates_updated"), 
                        Toast.LENGTH_SHORT).show()
                }
            }
            
            override fun onError(error: String) {
                runOnUiThread {
                    buttonRefreshRates.isEnabled = true
                    buttonRefreshRates.text = languageManager.getString("currency_exchange_refresh")
                    
                    // Set error state for all rates
                    val errorText = languageManager.getString("currency_exchange_error")
                    textRateUsd.text = errorText
                    textRateEur.text = errorText
                    textRateSgd.text = errorText
                    textRateMyr.text = errorText
                    textRateCny.text = errorText
                    textRateThb.text = errorText
                    textRateJpy.text = errorText
                    textLastUpdated.text = languageManager.getString("currency_exchange_failed_update")
                    
                    Toast.makeText(this@CurrencyExchangeActivity, 
                        languageManager.getString("currency_exchange_fetch_rates_error")
                            .replace("{error}", error), 
                        Toast.LENGTH_LONG).show()
                }
            }
        })
    }
      private fun updateExchangeRateTable(rates: Map<String, Double>) {
        val naText = languageManager.getString("currency_exchange_na")
        
        // Update USD rate (this is also used for the main currency conversion)
        rates["USD"]?.let { usdRate ->
            textRateUsd.text = String.format("%.2f", usdRate)
            
            // Also update the main currency manager rate
            currencyManager.setExchangeRate(usdRate)
            updateUI()
        } ?: run {
            textRateUsd.text = naText
        }
        
        // Update other currency rates
        rates["EUR"]?.let { textRateEur.text = String.format("%.2f", it) } ?: run { textRateEur.text = naText }
        rates["SGD"]?.let { textRateSgd.text = String.format("%.2f", it) } ?: run { textRateSgd.text = naText }
        rates["MYR"]?.let { textRateMyr.text = String.format("%.2f", it) } ?: run { textRateMyr.text = naText }
        rates["CNY"]?.let { textRateCny.text = String.format("%.2f", it) } ?: run { textRateCny.text = naText }
        rates["THB"]?.let { textRateThb.text = String.format("%.2f", it) } ?: run { textRateThb.text = naText }
        rates["JPY"]?.let { textRateJpy.text = String.format("%.2f", it) } ?: run { textRateJpy.text = naText }
    }
    
    private fun updateNavigationMenuTitles() {
        val menu = navigationView.menu
        menu.findItem(R.id.nav_home)?.title = languageManager.getString("nav_home")
        menu.findItem(R.id.nav_all_list)?.title = languageManager.getString("nav_all_list")
        menu.findItem(R.id.nav_history)?.title = languageManager.getString("nav_history")
        menu.findItem(R.id.nav_summary)?.title = languageManager.getString("nav_summary")
        menu.findItem(R.id.nav_analytics)?.title = languageManager.getString("nav_analytics")
        menu.findItem(R.id.nav_currency_exchange)?.title = languageManager.getString("nav_currency_exchange")
        menu.findItem(R.id.nav_settings)?.title = languageManager.getString("nav_settings")
        menu.findItem(R.id.nav_feedback)?.title = languageManager.getString("nav_feedback")
        menu.findItem(R.id.nav_about)?.title = languageManager.getString("nav_about")
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

    override fun onResume() {
        super.onResume()
        // Update static texts when resuming (in case language was changed)
        setupStaticTexts()
        // Update dynamic texts as well
        updateUI()
    }
}
