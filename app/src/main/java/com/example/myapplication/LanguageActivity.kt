package com.example.myapplication

import android.content.Intent
import android.os.Bundle
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class LanguageActivity : AppCompatActivity() {
    
    private lateinit var languageManager: LanguageManager
    private lateinit var radioGroupLanguage: RadioGroup
    private lateinit var radioEnglish: RadioButton
    private lateinit var radioMyanmar: RadioButton
    private lateinit var tvLanguageDescription: TextView
    private lateinit var tvLanguageNote: TextView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)
        
        languageManager = LanguageManager.getInstance(this)
        
        initViews()
        setupActionBar()
        setupLanguageSelection()
    }
      private fun initViews() {
        radioGroupLanguage = findViewById(R.id.radioGroupLanguage)
        radioEnglish = findViewById(R.id.radioEnglish)
        radioMyanmar = findViewById(R.id.radioMyanmar)
        tvLanguageDescription = findViewById(R.id.tvLanguageDescription)
        tvLanguageNote = findViewById(R.id.tvLanguageNote)
        
        // Set texts using LanguageManager
        tvLanguageDescription.text = languageManager.getString("language_description", "Choose your preferred language for the app interface")
        tvLanguageNote.text = languageManager.getString("language_restart_note", "The app will restart to apply the new language settings. All your data will be preserved.")
    }
    
    private fun setupActionBar() {
        supportActionBar?.title = languageManager.getString("language_settings")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun setupLanguageSelection() {
        // Set current selection based on saved language
        when (languageManager.getCurrentLanguage()) {
            "en" -> radioEnglish.isChecked = true
            "mm" -> radioMyanmar.isChecked = true
        }
        
        // Handle language selection changes
        radioGroupLanguage.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.radioEnglish -> {
                    changeLanguage("en")
                }
                R.id.radioMyanmar -> {
                    changeLanguage("mm")
                }
            }
        }
    }
    
    private fun changeLanguage(languageCode: String) {
        if (languageManager.getCurrentLanguage() != languageCode) {
            languageManager.setLanguage(languageCode)
            
            // Restart the app to apply the new language
            restartApp()
        }
    }
    
    private fun restartApp() {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_NEW_TASK)
        startActivity(intent)
        finishAffinity()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        onBackPressed()
        return true
    }
}
