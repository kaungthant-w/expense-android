package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class FeedbackActivity : AppCompatActivity() {
    
    private lateinit var radioGroupRating: RadioGroup
    private lateinit var editTextFeedback: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var languageManager: LanguageManager
      override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        
        languageManager = LanguageManager.getInstance(this)
        setupActionBar()
        initViews()
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
        supportActionBar?.title = languageManager.getString("feedback_title", "💬 Feedback")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }
    
    private fun initViews() {
        radioGroupRating = findViewById(R.id.radioGroupRating)
        editTextFeedback = findViewById(R.id.editTextFeedback)
        editTextEmail = findViewById(R.id.editTextEmail)
        buttonSubmit = findViewById(R.id.buttonSubmit)
    }
    
    private fun setupClickListeners() {
        buttonSubmit.setOnClickListener {
            submitFeedback()
        }
    }
      private fun submitFeedback() {
        val selectedRatingId = radioGroupRating.checkedRadioButtonId
        if (selectedRatingId == -1) {
            Toast.makeText(this, languageManager.getString("please_select_rating", "Please select a rating"), Toast.LENGTH_SHORT).show()
            return
        }
          val rating = when (selectedRatingId) {
            R.id.radioButtonExcellent -> "⭐⭐⭐⭐⭐ Excellent"
            R.id.radioButtonGood -> "⭐⭐⭐⭐ Good"
            R.id.radioButtonAverage -> "⭐⭐⭐ Average"
            else -> "Unknown"
        }
        
        val feedback = editTextFeedback.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
          if (feedback.isEmpty()) {
            Toast.makeText(this, languageManager.getString("please_enter_feedback", "Please enter your feedback"), Toast.LENGTH_SHORT).show()
            return
        }
        
        sendFeedbackEmail(rating, feedback, email)
    }
      private fun sendFeedbackEmail(rating: String, feedback: String, userEmail: String) {
        val subject = "Expense Tracker App Feedback"
        val body = """
            Rating: $rating
            
            Feedback:
            $feedback
            
            ${if (userEmail.isNotEmpty()) "User Email: $userEmail" else ""}
            
            App Version: 1.0
            Device: ${android.os.Build.MODEL}
            Android Version: ${android.os.Build.VERSION.RELEASE}
            
            ---
            Developer: Kyaw Myo Thant
            Email: kyawmyothant.dev@gmail.com
        """.trimIndent()
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("kyawmyothant.dev@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
          try {
            startActivity(Intent.createChooser(intent, "Send feedback via..."))
            Toast.makeText(this, languageManager.getString("feedback_sent_successfully", "Thank you for your feedback! 😊"), Toast.LENGTH_LONG).show()
            clearFields()
        } catch (e: Exception) {
            Toast.makeText(this, languageManager.getString("no_email_app_found", "No email app found. Please install an email app to send feedback."), Toast.LENGTH_LONG).show()
        }
    }
    
    private fun clearFields() {
        radioGroupRating.clearCheck()
        editTextFeedback.text.clear()
        editTextEmail.text.clear()
    }
    
    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }
}
