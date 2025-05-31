package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.RadioButton
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView

class ThemeActivity : AppCompatActivity() {
    
    private lateinit var cardLightTheme: CardView
    private lateinit var cardDarkTheme: CardView
    private lateinit var cardSystemTheme: CardView
    private lateinit var radioButtonLight: RadioButton
    private lateinit var radioButtonDark: RadioButton
    private lateinit var radioButtonSystem: RadioButton
    private lateinit var sharedPreferences: SharedPreferences
    
    companion object {
        const val THEME_PREFS = "theme_preferences"
        const val THEME_KEY = "selected_theme"
        const val THEME_LIGHT = "light"
        const val THEME_DARK = "dark"
        const val THEME_SYSTEM = "system"
    }
      override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_theme)
        
        setupActionBar()
        initViews()
        loadCurrentTheme()
        setupClickListeners()
    }
    
    private fun applyTheme() {
        val themePrefs = getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
        val savedTheme = themePrefs.getString(THEME_KEY, THEME_SYSTEM)
        
        when (savedTheme) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }
    
    private fun setupActionBar() {
        supportActionBar?.title = "🎨 Theme Settings"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
      private fun initViews() {
        cardLightTheme = findViewById(R.id.cardLightTheme)
        cardDarkTheme = findViewById(R.id.cardDarkTheme)
        cardSystemTheme = findViewById(R.id.cardSystemTheme)
        radioButtonLight = findViewById(R.id.radioButtonLight)
        radioButtonDark = findViewById(R.id.radioButtonDark)
        radioButtonSystem = findViewById(R.id.radioButtonSystem)
        sharedPreferences = getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
    }
      private fun loadCurrentTheme() {
        val currentTheme = sharedPreferences.getString(THEME_KEY, THEME_SYSTEM)
        clearAllSelections()
        when (currentTheme) {
            THEME_LIGHT -> radioButtonLight.isChecked = true
            THEME_DARK -> radioButtonDark.isChecked = true
            THEME_SYSTEM -> radioButtonSystem.isChecked = true
        }
    }
    
    private fun clearAllSelections() {
        radioButtonLight.isChecked = false
        radioButtonDark.isChecked = false
        radioButtonSystem.isChecked = false
    }
      private fun setupClickListeners() {
        cardLightTheme.setOnClickListener {
            selectTheme(THEME_LIGHT)
        }
        
        cardDarkTheme.setOnClickListener {
            selectTheme(THEME_DARK)
        }
        
        cardSystemTheme.setOnClickListener {
            selectTheme(THEME_SYSTEM)
        }
    }
    
    private fun selectTheme(theme: String) {
        // Clear all selections first
        clearAllSelections()
        
        // Set the selected theme
        when (theme) {
            THEME_LIGHT -> radioButtonLight.isChecked = true
            THEME_DARK -> radioButtonDark.isChecked = true
            THEME_SYSTEM -> radioButtonSystem.isChecked = true
        }
        
        // Save and apply the theme
        saveTheme(theme)
        applyThemeChange(theme)
    }
    
    private fun saveTheme(theme: String) {
        sharedPreferences.edit()
            .putString(THEME_KEY, theme)
            .apply()
    }
      private fun applyThemeChange(theme: String) {
        when (theme) {
            THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
        
        // Recreate activity to apply theme immediately
        recreate()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
