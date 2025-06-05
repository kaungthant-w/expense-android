package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.MenuItem
import android.widget.Button
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class FeedbackActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {
    
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
        updateUITexts()
        updateNavigationMenuTitles()
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
        supportActionBar?.title = languageManager.getString("feedback_title")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }    private fun updateUITexts() {
        // Update action bar title
        supportActionBar?.title = languageManager.getString("feedback_title")
        
        // Update page title
        findViewById<TextView>(R.id.textViewPageTitle)?.text = languageManager.getString("we_value_your_feedback")
        
        // Update rating question
        findViewById<TextView>(R.id.textViewRatingQuestion)?.text = languageManager.getString("feedback_rating_question")
        
        // Update hints for input fields
        editTextFeedback.hint = languageManager.getString("feedback_hint")
        editTextEmail.hint = languageManager.getString("email_hint")
        
        // Update button text
        buttonSubmit.text = languageManager.getString("submit_feedback")
        
        // Update radio button texts
        findViewById<RadioButton>(R.id.radioButtonExcellent).text = languageManager.getString("rating_excellent")
        findViewById<RadioButton>(R.id.radioButtonGood).text = languageManager.getString("rating_good")
        findViewById<RadioButton>(R.id.radioButtonAverage).text = languageManager.getString("rating_average")
        
        // Update other text views
        findViewById<TextView>(R.id.textViewFeedbackLabel)?.text = "📝 " + languageManager.getString("feedback_label")
    }
    
    private fun initViews() {
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
            Toast.makeText(this, languageManager.getString("please_select_rating"), Toast.LENGTH_SHORT).show()
            return
        }
        
        val rating = when (selectedRatingId) {
            R.id.radioButtonExcellent -> languageManager.getString("rating_excellent")
            R.id.radioButtonGood -> languageManager.getString("rating_good")
            R.id.radioButtonAverage -> languageManager.getString("rating_average")
            else -> "Unknown"
        }
        
        val feedback = editTextFeedback.text.toString().trim()
        val email = editTextEmail.text.toString().trim()
        
        if (feedback.isEmpty()) {
            Toast.makeText(this, languageManager.getString("please_enter_feedback"), Toast.LENGTH_SHORT).show()
            return
        }
        
        sendFeedbackEmail(rating, feedback, email)
    }
    
    private fun sendFeedbackEmail(rating: String, feedback: String, userEmail: String) {
        val subject = languageManager.getString("feedback_subject")
        val body = """
            ${languageManager.getString("email_rating")}: $rating
            
            ${languageManager.getString("email_feedback")}:
            $feedback
            
            ${if (userEmail.isNotEmpty()) "${languageManager.getString("email_user_email")}: $userEmail" else ""}
            
            ${languageManager.getString("email_app_version")}: 1.0
            ${languageManager.getString("email_device")}: ${android.os.Build.MODEL}
            ${languageManager.getString("email_android_version")}: ${android.os.Build.VERSION.RELEASE}
            
            ---
            ${languageManager.getString("email_developer")}: ${languageManager.getString("developer_name")}
            ${languageManager.getString("email")}: ${languageManager.getString("developer_email")}
        """.trimIndent()
        
        val intent = Intent(Intent.ACTION_SENDTO).apply {
            data = Uri.parse("mailto:")
            putExtra(Intent.EXTRA_EMAIL, arrayOf("kyawmyothant.dev@gmail.com"))
            putExtra(Intent.EXTRA_SUBJECT, subject)
            putExtra(Intent.EXTRA_TEXT, body)
        }
        
        try {
            startActivity(Intent.createChooser(intent, languageManager.getString("send_feedback_via")))
            Toast.makeText(this, languageManager.getString("thank_you_feedback"), Toast.LENGTH_LONG).show()
            clearFields()
        } catch (e: Exception) {
            Toast.makeText(this, languageManager.getString("no_email_app"), Toast.LENGTH_LONG).show()
        }
    }
    
    private fun clearFields() {
        radioGroupRating.clearCheck()
        editTextFeedback.text.clear()
        editTextEmail.text.clear()
    }
      private fun updateNavigationMenuTitles() {
        val menu = navigationView.menu
        menu.findItem(R.id.nav_home)?.title = languageManager.getString("nav_home")
        menu.findItem(R.id.nav_all_list)?.title = languageManager.getString("nav_all_list")
        menu.findItem(R.id.nav_history)?.title = languageManager.getString("nav_history")
        menu.findItem(R.id.nav_summary)?.title = languageManager.getString("nav_summary")
        menu.findItem(R.id.nav_currency_exchange)?.title = languageManager.getString("nav_currency_exchange")
        menu.findItem(R.id.nav_settings)?.title = languageManager.getString("nav_settings")
        menu.findItem(R.id.nav_feedback)?.title = languageManager.getString("nav_feedback")
        menu.findItem(R.id.nav_about)?.title = languageManager.getString("nav_about")
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
            }            R.id.nav_summary -> {
                startActivity(Intent(this, SummaryActivity::class.java))
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
            R.id.nav_about -> {
                startActivity(Intent(this, AboutActivity::class.java))
                finish()
            }
        }
        
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
    
    @Deprecated("Deprecated in Java")
    override fun onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START)
        } else {
            super.onBackPressed()
        }
    }
}
