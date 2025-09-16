package com.hsu.expense

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.localbroadcastmanager.content.LocalBroadcastManager

abstract class BaseActivity : AppCompatActivity() {
    
    protected lateinit var languageManager: LanguageManager
    private lateinit var languageChangeReceiver: BroadcastReceiver
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        languageManager = LanguageManager.getInstance(this)
        setupLanguageChangeReceiver()
    }
      override fun onResume() {
        super.onResume()
        // Register for language change broadcasts
        try {
            LocalBroadcastManager.getInstance(this)
                .registerReceiver(languageChangeReceiver, IntentFilter(LanguageManager.LANGUAGE_CHANGED_ACTION))
        } catch (e: Exception) {
            // Handle registration error
        }
    }
    
    override fun onPause() {
        super.onPause()
        // Unregister from language change broadcasts
        try {
            LocalBroadcastManager.getInstance(this)
                .unregisterReceiver(languageChangeReceiver)
        } catch (e: Exception) {
            // Handle unregistration error
        }
    }
      private fun setupLanguageChangeReceiver() {
        languageChangeReceiver = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                if (intent?.action == LanguageManager.LANGUAGE_CHANGED_ACTION) {
                    val oldLanguage = intent.getStringExtra("old_language")
                    val newLanguage = intent.getStringExtra("new_language")
                    android.util.Log.d("BaseActivity", "${this@BaseActivity.javaClass.simpleName}: Received language change broadcast: $oldLanguage -> $newLanguage")
                    onLanguageChanged()
                }
            }
        }
    }
    
    // Override this method in activities that need special handling for language changes
    open fun onLanguageChanged() {
        // Default implementation - recreate the activity to refresh all UI
        recreate()
    }
}