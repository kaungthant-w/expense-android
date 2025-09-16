package com.hsu.expense

import android.content.Context
import android.content.SharedPreferences
import android.os.Bundle
import android.widget.ImageButton
import android.widget.RadioButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.cardview.widget.CardView

class ThemeActivity : BaseActivity() {
      private lateinit var cardLightTheme: CardView
    private lateinit var cardDarkTheme: CardView
    private lateinit var cardSystemTheme: CardView
    private lateinit var radioButtonLight: RadioButton
    private lateinit var radioButtonDark: RadioButton
    private lateinit var radioButtonSystem: RadioButton
    private lateinit var buttonBack: ImageButton
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
        setupBackButton()
        setupTranslations()
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
    }    private fun setupActionBar() {
        supportActionBar?.title = languageManager.getString("theme_settings_title")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
      private fun initViews() {
        cardLightTheme = findViewById(R.id.cardLightTheme)
        cardDarkTheme = findViewById(R.id.cardDarkTheme)
        cardSystemTheme = findViewById(R.id.cardSystemTheme)
        radioButtonLight = findViewById(R.id.radioButtonLight)
        radioButtonDark = findViewById(R.id.radioButtonDark)
        radioButtonSystem = findViewById(R.id.radioButtonSystem)
        buttonBack = findViewById(R.id.buttonBack)
        sharedPreferences = getSharedPreferences(THEME_PREFS, Context.MODE_PRIVATE)
    }

    private fun setupBackButton() {
        buttonBack.setOnClickListener {
            finish()
        }
    }
      private fun setupTranslations() {
        // Update main title
        val themeTitleText = findViewById<TextView>(R.id.textThemeTitle)
        themeTitleText?.text = languageManager.getString("choose_your_theme")
        
        // Update theme selection header
        val themeDescriptionText = findViewById<TextView>(R.id.textThemeDescription)
        themeDescriptionText?.text = languageManager.getString("theme_selection_description")
        
        // Update light theme
        val lightThemeTitle = findViewById<TextView>(R.id.textLightThemeTitle)
        val lightThemeDesc = findViewById<TextView>(R.id.textLightThemeDescription)
        lightThemeTitle?.text = languageManager.getString("light_theme_title")
        lightThemeDesc?.text = languageManager.getString("light_theme_description")
        
        // Update dark theme
        val darkThemeTitle = findViewById<TextView>(R.id.textDarkThemeTitle)
        val darkThemeDesc = findViewById<TextView>(R.id.textDarkThemeDescription)
        darkThemeTitle?.text = languageManager.getString("dark_theme_title")
        darkThemeDesc?.text = languageManager.getString("dark_theme_description")
          // Update system theme
        val systemThemeTitle = findViewById<TextView>(R.id.textSystemThemeTitle)
        val systemThemeDesc = findViewById<TextView>(R.id.textSystemThemeDescription)
        systemThemeTitle?.text = languageManager.getString("system_theme_title")
        systemThemeDesc?.text = languageManager.getString("system_theme_description")
        
        // Update theme info text
        findViewById<TextView>(R.id.textViewThemeInfo)?.text = languageManager.getString("theme_changes_immediate")
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
        // clear selections first
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
