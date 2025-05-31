package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import android.view.MenuItem
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.navigation.NavigationView
import com.google.android.material.textfield.TextInputEditText
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class CurrencyExchangeActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
      private lateinit var currencyManager: CurrencyManager
    private lateinit var currencyApiService: CurrencyApiService
    private lateinit var sharedPreferences: SharedPreferences
    
    // Navigation Drawer components
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    private lateinit var fabMenu: FloatingActionButton
    
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
        sharedPreferences = getSharedPreferences("expense_prefs", Context.MODE_PRIVATE)
        setupActionBar()
        initViews()
        setupNavigationDrawer()
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
        supportActionBar?.title = "💱 Currency Exchange"
    }    private fun initViews() {
        // Navigation drawer components
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)
        fabMenu = findViewById(R.id.fabMenu)
        
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
    
    private fun setupNavigationDrawer() {
        val toggle = ActionBarDrawerToggle(
            this, drawerLayout, null,
            R.string.navigation_drawer_open, R.string.navigation_drawer_close
        )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()
        
        navigationView.setNavigationItemSelectedListener(this)
        
        fabMenu.setOnClickListener {
            drawerLayout.openDrawer(GravityCompat.START)
        }
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
            CurrencyManager.CURRENCY_USD -> "Switched to USD - Viewing original amounts"
            CurrencyManager.CURRENCY_MMK -> "Switched to MMK - Converting amounts using exchange rate"
            else -> "Currency switched"
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
        textCurrentRate.text = "Exchange Rate: 1 USD = ${String.format("%.2f", exchangeRate)} MMK"
        
        // Calculate totals
        val totalUsd = expensesList.sumOf { it.price }
        val totalMmk = currencyManager.convertFromUsd(totalUsd)
        
        textTotalExpensesUsd.text = "Total in USD: ${currencyManager.formatCurrency(totalUsd)}"
        textTotalExpensesMmk.text = "Total in MMK: ${String.format("%.2f", totalMmk)} MMK"
        
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
            exchangeAdapter.notifyDataSetChanged()
            
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
                    exchangeAdapter.notifyDataSetChanged()
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
    }    override fun onSupportNavigateUp(): Boolean {
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
