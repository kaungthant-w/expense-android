package com.hsu.expense

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity

class SplashActivity : AppCompatActivity() {
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "Festival Notifications"
            val descriptionText = "Notifications for festival alerts and celebrations"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val channel = NotificationChannel("festival_channel", name, importance).apply {
                description = descriptionText
                enableVibration(true)
                enableLights(true)
            }
            
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    companion object {
        private const val ONBOARDING_PREFS = "onboarding_prefs"
        private const val ONBOARDING_COMPLETED = "onboarding_completed"
    }
      override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // Create notification channel for festival notifications
        createNotificationChannel()
        
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
