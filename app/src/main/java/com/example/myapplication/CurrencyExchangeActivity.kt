package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CurrencyExchangeActivity : AppCompatActivity() {
    
    private lateinit var currencyManager: CurrencyManager
    private lateinit var currencyApiService: CurrencyApiService
    private lateinit var sharedPreferences: SharedPreferences
    private lateinit var languageManager: LanguageManager
    
    private lateinit var cardUsd: CardView
    private lateinit var cardMmk: CardView
    private lateinit var textCurrentRate: TextView
    private lateinit var textTotalExpensesUsd: TextView
    private lateinit var textTotalExpensesMmk: TextView
    private lateinit var buttonFetchRate: MaterialButton
    private lateinit var buttonCustomRate: MaterialButton
    private lateinit var buttonSwitchCurrency: MaterialButton
    private lateinit var layoutCustomRateInput: LinearLayout
    private lateinit var editTextCustomRate: TextInputEditText
    private lateinit var buttonApplyCustomRate: MaterialButton
    private lateinit var buttonCancelCustomRate: MaterialButton
    private lateinit var recyclerViewExpenses: RecyclerView
    private lateinit var exchangeAdapter: CurrencyExchangeAdapter
    
    private val expensesList = mutableListOf<ExpenseItem>()
    private val gson = Gson()
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
        setupClickListeners()
        loadExpenses()
        updateUI()
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
        supportActionBar?.title = languageManager.getString("currency_exchange_title", "💱 Currency Exchange")
    }
      private fun initViews() {
        cardUsd = findViewById(R.id.cardUsd)
        cardMmk = findViewById(R.id.cardMmk)
        textCurrentRate = findViewById(R.id.textCurrentRate)
        textTotalExpensesUsd = findViewById(R.id.textTotalExpensesUsd)
        textTotalExpensesMmk = findViewById(R.id.textTotalExpensesMmk)
        buttonFetchRate = findViewById(R.id.buttonFetchRate)
        buttonCustomRate = findViewById(R.id.buttonCustomRate)
        buttonSwitchCurrency = findViewById(R.id.buttonSwitchCurrency)
        layoutCustomRateInput = findViewById(R.id.layoutCustomRateInput)
        editTextCustomRate = findViewById(R.id.editTextCustomRate)
        buttonApplyCustomRate = findViewById(R.id.buttonApplyCustomRate)
        buttonCancelCustomRate = findViewById(R.id.buttonCancelCustomRate)
        recyclerViewExpenses = findViewById(R.id.recyclerViewExpenses)
        
        setupRecyclerView()
    }
    
    private fun setupRecyclerView() {
        exchangeAdapter = CurrencyExchangeAdapter(expensesList, currencyManager)
        recyclerViewExpenses.apply {
            layoutManager = LinearLayoutManager(this@CurrencyExchangeActivity)
            adapter = exchangeAdapter
        }    }
    
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
    }
    
    private fun switchToCurrency(currency: String) {
        currencyManager.setCurrentCurrency(currency)
        updateUI()
        exchangeAdapter.notifyDataSetChanged()
          val message = when (currency) {
            CurrencyManager.CURRENCY_USD -> languageManager.getString("switched_to_usd", "Switched to USD - Viewing original amounts")
            CurrencyManager.CURRENCY_MMK -> languageManager.getString("switched_to_mmk", "Switched to MMK - Converting amounts using exchange rate")
            else -> languageManager.getString("currency_switched", "Currency switched")
        }
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
    
    private fun loadExpenses() {
        val json = sharedPreferences.getString("expenses", null)
        if (!json.isNullOrEmpty()) {
            val type = object : TypeToken<List<ExpenseItem>>() {}.type
            val savedExpenses: List<ExpenseItem> = gson.fromJson(json, type)
            expensesList.clear()
            // Load only active expenses (not deleted)
            val activeExpenses = savedExpenses.filter { !it.isDeleted }
            expensesList.addAll(activeExpenses)
        }
    }
    
    private fun updateUI() {
        val currentCurrency = currencyManager.getCurrentCurrency()
        val exchangeRate = currencyManager.getExchangeRate()
        
        // Update currency selection visual feedback
        updateCurrencySelection(currentCurrency)
          // Update exchange rate display
        val rateDisplay = languageManager.getString("exchange_rate_display", "Exchange Rate: 1 USD = {0} MMK")
        textCurrentRate.text = rateDisplay.replace("{0}", String.format("%.2f", exchangeRate))
        
        // Calculate totals
        val totalUsd = expensesList.sumOf { it.price }
        val totalMmk = currencyManager.convertFromUsd(totalUsd)
        
        val usdDisplay = languageManager.getString("total_in_usd", "Total in USD: {0}")
        val mmkDisplay = languageManager.getString("total_in_mmk", "Total in MMK: {0} MMK")
        
        textTotalExpensesUsd.text = usdDisplay.replace("{0}", currencyManager.formatCurrency(totalUsd))
        textTotalExpensesMmk.text = mmkDisplay.replace("{0}", String.format("%.2f", totalMmk))
        
        // Update switch button text
        buttonSwitchCurrency.text = when (currentCurrency) {
            CurrencyManager.CURRENCY_USD -> languageManager.getString("switch_to_mmk", "💱 Switch to MMK")
            CurrencyManager.CURRENCY_MMK -> languageManager.getString("switch_to_usd", "💱 Switch to USD")
            else -> languageManager.getString("switch_currency", "💱 Switch Currency")
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
    }    private fun showCustomRateInput() {
        layoutCustomRateInput.visibility = View.VISIBLE
        editTextCustomRate.setText(currencyManager.getExchangeRate().toString())
        editTextCustomRate.requestFocus()
        Toast.makeText(this, languageManager.getString("enter_custom_rate", "💡 Enter a custom exchange rate"), Toast.LENGTH_SHORT).show()
    }
    
    private fun hideCustomRateInput() {
        layoutCustomRateInput.visibility = View.GONE
        editTextCustomRate.text?.clear()
    }
      private fun applyCustomRate() {
        val customRateText = editTextCustomRate.text.toString().trim()
        
        if (customRateText.isEmpty()) {
            Toast.makeText(this, languageManager.getString("enter_exchange_rate", "❌ Please enter an exchange rate"), Toast.LENGTH_SHORT).show()
            return
        }
        
        try {
            val customRate = customRateText.toDouble()
            
            if (customRate <= 0) {
                Toast.makeText(this, languageManager.getString("rate_must_be_positive", "❌ Exchange rate must be greater than 0"), Toast.LENGTH_SHORT).show()
                return
            }
            
            if (customRate < 100 || customRate > 10000) {
                val warningMsg = languageManager.getString("rate_seems_unusual", "⚠️ Are you sure? Rate seems unusual ({0} MMK per USD)")
                Toast.makeText(this, warningMsg.replace("{0}", customRate.toString()), Toast.LENGTH_LONG).show()
            }
            
            // Apply the custom rate
            currencyManager.setExchangeRate(customRate)
            hideCustomRateInput()
            updateUI()
            exchangeAdapter.notifyDataSetChanged()
            
            val successMsg = languageManager.getString("custom_rate_applied", "✅ Custom rate applied: 1 USD = {0} MMK")
            Toast.makeText(this, successMsg.replace("{0}", String.format("%.2f", customRate)), Toast.LENGTH_LONG).show()
                
        } catch (e: NumberFormatException) {
            Toast.makeText(this, languageManager.getString("invalid_number_format", "❌ Invalid number format. Please enter a valid decimal number"), Toast.LENGTH_LONG).show()
        }
    }
      private fun fetchLatestExchangeRate() {
        buttonFetchRate.isEnabled = false
        buttonFetchRate.text = languageManager.getString("fetching_rate", "⏳ Fetching...")
        
        currencyApiService.fetchLatestExchangeRate(object : CurrencyApiService.CurrencyRateCallback {
            override fun onSuccess(usdToMmk: Double) {
                runOnUiThread {
                    currencyManager.setExchangeRate(usdToMmk)
                    updateUI()
                    exchangeAdapter.notifyDataSetChanged()
                    buttonFetchRate.isEnabled = true
                    buttonFetchRate.text = languageManager.getString("fetch_rate_button", "🔄 Fetch Latest Rate")
                    
                    val successMsg = languageManager.getString("rate_updated", "✅ Rate updated: 1 USD = {0} MMK")
                    Toast.makeText(this@CurrencyExchangeActivity, 
                        successMsg.replace("{0}", String.format("%.2f", usdToMmk)), 
                        Toast.LENGTH_LONG).show()
                }
            }
            
            override fun onError(error: String) {
                runOnUiThread {
                    buttonFetchRate.isEnabled = true
                    buttonFetchRate.text = languageManager.getString("fetch_rate_button", "🔄 Fetch Latest Rate")
                    
                    // Show error message and suggest custom rate input
                    val errorMsg = languageManager.getString("fetch_rate_failed", "❌ Failed to fetch rate: {0}\\n💡 Try using 'Custom Rate' to set manually")
                    Toast.makeText(this@CurrencyExchangeActivity, errorMsg.replace("{0}", error), Toast.LENGTH_LONG).show()
                    
                    // Automatically show custom rate input on API failure
                    showCustomRateInput()
                }
            }
        })
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
