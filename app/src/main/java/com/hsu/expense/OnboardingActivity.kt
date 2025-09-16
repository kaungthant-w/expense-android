package com.hsu.expense

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.appcompat.app.AppCompatActivity

class OnboardingActivity : AppCompatActivity() {
    
    private lateinit var languageManager: LanguageManager
    private lateinit var currencyManager: CurrencyManager
      private lateinit var spinnerLanguage: Spinner
    private lateinit var spinnerCurrency: Spinner
    private lateinit var buttonGetStarted: Button
    private lateinit var textViewWelcome: TextView
    private lateinit var textViewLanguageLabel: TextView
    private lateinit var textViewCurrencyLabel: TextView
    
    private var selectedLanguageCode: String = "en"
    private var selectedCurrencyCode: String = "USD"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
          try {
            setContentView(R.layout.activity_onboarding_simple)
            
            // Initialize managers
            languageManager = LanguageManager.getInstance(this)
            currencyManager = CurrencyManager.getInstance(this)
            
            initViews()
            setupLanguageSpinner()
            setupCurrencySpinner()
            setupClickListeners()
            updateTexts()
        } catch (e: Exception) {
            // Handle any error and redirect to MainActivity
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    private fun initViews() {
        try {
            spinnerLanguage = findViewById(R.id.spinnerLanguage)
            spinnerCurrency = findViewById(R.id.spinnerCurrency)
            buttonGetStarted = findViewById(R.id.buttonGetStarted)
            textViewWelcome = findViewById(R.id.textViewWelcome)
            textViewLanguageLabel = findViewById(R.id.textViewLanguageLabel)
            textViewCurrencyLabel = findViewById(R.id.textViewCurrencyLabel)
        } catch (e: Exception) {
            // Handle view initialization error
            finish()
        }
    }
      private fun setupLanguageSpinner() {
        val languages = listOf(
            Pair("en", "English"),
            Pair("mm", "မြန်မာ"),
            Pair("zh", "中文"),
            Pair("ja", "日本語"),
            Pair("th", "ไทย")
        )
        
        val languageNames = languages.map { it.second }
        val languageAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, languageNames)
        languageAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerLanguage.adapter = languageAdapter
        
        // Set default selection (English)
        spinnerLanguage.setSelection(0)
        selectedLanguageCode = "en"
        
        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedLanguageCode = languages[position].first
                // Update language immediately to see changes
                languageManager.setLanguage(selectedLanguageCode)
                updateTexts()
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }    private fun setupCurrencySpinner() {
        val currencies = listOf(
            Pair("USD", "💵 USD"),
            Pair("MMK", "🇲🇲 MMK (Myanmar Kyat)"),
            Pair("SGD", "🇸🇬 SGD (Singapore Dollar)"),
            Pair("THB", "🇹🇭 THB (Thai Baht)"),
            Pair("JPY", "🇯🇵 JPY (Japanese Yen)"),
            Pair("CNY", "🇨🇳 CNY (Chinese Yuan)"),
            Pair("MYR", "🇲🇾 MYR (Malaysian Ringgit)"),
            Pair("EUR", "🇪🇺 EUR (Euro)"),
            Pair("KRW", "🇰🇷 KRW (South Korean Won)")
        )
        
        val currencyNames = currencies.map { it.second }
        val currencyAdapter = ArrayAdapter(this, android.R.layout.simple_spinner_item, currencyNames)
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        spinnerCurrency.adapter = currencyAdapter
        
        // Set default selection (USD)
        spinnerCurrency.setSelection(0)
        selectedCurrencyCode = "USD"
        
        spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedCurrencyCode = currencies[position].first
            }
            
            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
    }
    
    private fun setupClickListeners() {
        buttonGetStarted.setOnClickListener {
            saveSettingsAndContinue()
        }
    }
      private fun updateTexts() {
        try {
            if (::languageManager.isInitialized && ::textViewWelcome.isInitialized) {
                textViewWelcome.text = languageManager.getString("welcome_to_expense_tracker")
                textViewLanguageLabel.text = languageManager.getString("choose_language")
                textViewCurrencyLabel.text = languageManager.getString("choose_currency")
                buttonGetStarted.text = languageManager.getString("get_started")
            } else {
                // Use default texts if managers are not initialized
                textViewWelcome.text = "welcome to HSU Expense"
                textViewLanguageLabel.text = "Choose your language"
                textViewCurrencyLabel.text = "Choose your currency"
                buttonGetStarted.text = "Get Started"
            }
        } catch (e: Exception) {
            // Use default texts if language strings fail
            textViewWelcome.text = "welcome to HSU Expense"
            textViewLanguageLabel.text = "Choose your language"
            textViewCurrencyLabel.text = "Choose your currency"
            buttonGetStarted.text = "Get Started"
        }
    }
    
    private fun saveSettingsAndContinue() {
        try {
            // Save onboarding completion status
            val onboardingPrefs = getSharedPreferences("onboarding_prefs", Context.MODE_PRIVATE)
            onboardingPrefs.edit()
                .putBoolean("onboarding_completed", true)
                .putString("initial_language", selectedLanguageCode)
                .putString("initial_currency", selectedCurrencyCode)
                .apply()
            
            // Save language setting
            languageManager.setLanguage(selectedLanguageCode)
            
            // Save currency setting
            currencyManager.setCurrentCurrency(selectedCurrencyCode)
            
            // Show completion message
            Toast.makeText(this, languageManager.getString("onboarding_complete"), Toast.LENGTH_SHORT).show()
            
            // Start MainActivity and finish this activity
            val intent = Intent(this, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            // Fallback navigation if anything fails
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
    
    override fun onBackPressed() {
        // Prevent going back during onboarding
        // User must complete the setup
    }
}
