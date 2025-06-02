package com.example.myapplication

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

abstract class BaseActivity : AppCompatActivity() {
    
    protected lateinit var languageManager: LanguageManager
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languageManager = LanguageManager.getInstance(this)
    }
    
    // Override this method in activities that need special handling for language changes
    open fun onLanguageChanged() {
        // Default implementation - recreate the activity to refresh all UI
        recreate()
    }
}