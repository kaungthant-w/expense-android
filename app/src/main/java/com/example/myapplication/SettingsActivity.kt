package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView

class SettingsActivity : AppCompatActivity() {
    
    private lateinit var languageManager: LanguageManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)
        
        languageManager = LanguageManager.getInstance(this)
        
        setupActionBar()
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
        supportActionBar?.title = languageManager.getString("settings", "⚙️ Settings")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }private fun setupClickListeners() {
        // Theme Settings Card
        findViewById<CardView>(R.id.cardThemeSettings)?.setOnClickListener {
            startActivity(Intent(this, ThemeActivity::class.java))
        }
        
        // Language Settings Card
        findViewById<CardView>(R.id.cardLanguageSettings)?.setOnClickListener {
            startActivity(Intent(this, LanguageActivity::class.java))
        }
        
          // Currency Settings Card
        findViewById<CardView>(R.id.cardCurrencySettings)?.setOnClickListener {
            startActivity(Intent(this, CurrencyExchangeActivity::class.java))
        }
        
        // Feedback Card
        findViewById<CardView>(R.id.cardFeedback)?.setOnClickListener {
            startActivity(Intent(this, FeedbackActivity::class.java))
        }
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
