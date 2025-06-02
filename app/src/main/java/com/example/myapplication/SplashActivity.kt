package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    
    companion object {
        private const val ONBOARDING_PREFS = "onboarding_prefs"
        private const val ONBOARDING_COMPLETED = "onboarding_completed"
    }
      override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        try {
            // Check if onboarding has been completed
            val sharedPrefs = getSharedPreferences(ONBOARDING_PREFS, Context.MODE_PRIVATE)
            val isOnboardingCompleted = sharedPrefs.getBoolean(ONBOARDING_COMPLETED, false)
            
            val intent = if (isOnboardingCompleted) {
                Intent(this, MainActivity::class.java)
            } else {
                Intent(this, OnboardingActivity::class.java)
            }
            
            startActivity(intent)
            finish()
        } catch (e: Exception) {
            // Fallback to MainActivity if there's any error
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
            finish()
        }
    }
}
