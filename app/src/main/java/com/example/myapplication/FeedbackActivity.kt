package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class FeedbackActivity : AppCompatActivity(), NavigationView.OnNavigationItemSelectedListener {
    
    private lateinit var radioGroupRating: RadioGroup
    private lateinit var editTextFeedback: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var buttonSubmit: Button
    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
          setupActionBar()
        initViews()
        setupClickListeners()
        setupNavigationDrawer()
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
        supportActionBar?.title = "💬 Feedback"
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }    private fun initViews() {
        radioGroupRating = findViewById(R.id.radioGroupRating)
        editTextFeedback = findViewById(R.id.editTextFeedback)
        editTextEmail = findViewById(R.id.editTextEmail)
        buttonSubmit = findViewById(R.id.buttonSubmit)
        drawerLayout = findViewById(R.id.drawer_layout)
        navigationView = findViewById(R.id.navigation_view)
        
        // Back button click listener
        findViewById<android.widget.ImageButton>(R.id.buttonBack).setOnClickListener {
            finish()
        }
    }
    
    private fun setupClickListeners() {
        buttonSubmit.setOnClickListener {
            submitFeedback()
        }
    }
      private fun setupNavigationDrawer() {
        navigationView.setNavigationItemSelectedListener(this)
        
        // Set feedback as checked
        navigationView.setCheckedItem(R.id.nav_feedback)
    }
    
    private fun submitFeedback() {
        val selectedRatingId = radioGroupRating.checkedRadioButtonId
        if (selectedRatingId == -1) {
            Toast.makeText(this, "Please select a rating", Toast.LENGTH_SHORT).show()
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
            Toast.makeText(this, "Please enter your feedback", Toast.LENGTH_SHORT).show()
            return
        }
        
        sendFeedbackEmail(rating, feedback, email)
    }
      private fun sendFeedbackEmail(rating: String, feedback: String, userEmail: String) {
        val subject = "HsuPar Expense App Feedback"
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
            Toast.makeText(this, "Thank you for your feedback! 😊", Toast.LENGTH_LONG).show()
            clearFields()
        } catch (e: Exception) {
            Toast.makeText(this, "No email app found. Please install an email app to send feedback.", Toast.LENGTH_LONG).show()
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
    
    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
            R.id.nav_summary -> {
                startActivity(Intent(this, SummaryActivity::class.java))
                finish()
            }
            R.id.nav_analytics -> {
                startActivity(Intent(this, AnalyticsActivity::class.java))
                finish()
            }
            R.id.nav_all_list -> {
                startActivity(Intent(this, AllListActivity::class.java))
                finish()
            }
            R.id.nav_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
                finish()
            }
            R.id.nav_currency_exchange -> {
                startActivity(Intent(this, CurrencyExchangeActivity::class.java))
                finish()
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
                finish()
            }
            R.id.nav_feedback -> {
                // Already on feedback, just close drawer
                drawerLayout.closeDrawer(GravityCompat.START)
                return true
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
