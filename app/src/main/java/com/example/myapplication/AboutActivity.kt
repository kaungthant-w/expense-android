package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.view.MenuItem
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.appcompat.app.ActionBarDrawerToggle
import androidx.appcompat.app.AppCompatDelegate
import androidx.core.view.GravityCompat
import androidx.drawerlayout.widget.DrawerLayout
import com.google.android.material.navigation.NavigationView

class AboutActivity : BaseActivity(), NavigationView.OnNavigationItemSelectedListener {

    private lateinit var drawerLayout: DrawerLayout
    private lateinit var navigationView: NavigationView

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_about)

        setupActionBar()
        initViews()
        setupNavigationDrawer()
        updateNavigationMenuTitles()
        updateTextElements()

        // Setup back press handling
        onBackPressedDispatcher.addCallback(
                this,
                object : OnBackPressedCallback(true) {
                    override fun handleOnBackPressed() {
                        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
                            drawerLayout.closeDrawer(GravityCompat.START)
                        } else {
                            finish()
                        }
                    }
                }
        )
    }

    private fun applyTheme() {
        val themePrefs = getSharedPreferences(ThemeActivity.THEME_PREFS, Context.MODE_PRIVATE)
        val savedTheme = themePrefs.getString(ThemeActivity.THEME_KEY, ThemeActivity.THEME_SYSTEM)

        when (savedTheme) {
            ThemeActivity.THEME_LIGHT ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeActivity.THEME_DARK ->
                    AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            ThemeActivity.THEME_SYSTEM ->
                    AppCompatDelegate.setDefaultNightMode(
                            AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM
                    )
        }
    }

    private fun setupActionBar() {
        supportActionBar?.title = languageManager.getString("about_title")
        supportActionBar?.setDisplayHomeAsUpEnabled(true)
    }

    private fun initViews() {
        drawerLayout = findViewById(R.id.drawerLayout)
        navigationView = findViewById(R.id.navigationView)

        // Setup back button click listener
        findViewById<android.widget.ImageButton>(R.id.buttonBack).setOnClickListener { finish() }
    }

    private fun updateNavigationMenuTitles() {        val menu = navigationView.menu
        menu.findItem(R.id.nav_home)?.title = languageManager.getString("nav_home")
        menu.findItem(R.id.nav_all_list)?.title = languageManager.getString("nav_all_list")
        menu.findItem(R.id.nav_history)?.title = languageManager.getString("nav_history")
        menu.findItem(R.id.nav_summary)?.title = languageManager.getString("nav_summary")
        menu.findItem(R.id.nav_currency_exchange)?.title =
                languageManager.getString("nav_currency_exchange")
        menu.findItem(R.id.nav_settings)?.title = languageManager.getString("nav_settings")
        menu.findItem(R.id.nav_feedback)?.title = languageManager.getString("nav_feedback")
        menu.findItem(R.id.nav_about)?.title = languageManager.getString("nav_about")
    }

    private fun updateTextElements() {
        // Update header title
        findViewById<TextView>(R.id.textViewHeaderTitle)?.text =
                languageManager.getString("about_title")

        // Update about section
        findViewById<TextView>(R.id.textViewAboutSectionTitle)?.text =
                languageManager.getString("about_section_title")
        findViewById<TextView>(R.id.textViewAppName)?.text =
                languageManager.getString("app_name_display")
        findViewById<TextView>(R.id.textViewAppVersion)?.text =
                languageManager.getString("app_version")
        findViewById<TextView>(R.id.textViewAppDescription)?.text =
                languageManager.getString("app_description")

        // Update developer section
        findViewById<TextView>(R.id.textViewDevelopedBy)?.text =
                languageManager.getString("developed_by")
        findViewById<TextView>(R.id.textViewDeveloperName)?.text =
                languageManager.getString("developer_name")
        findViewById<TextView>(R.id.textViewDeveloperEmail)?.text =
                languageManager.getString("developer_email")

        // Update supporter section
        findViewById<TextView>(R.id.textViewSupportedBy)?.text =
                languageManager.getString("supported_by")
        findViewById<TextView>(R.id.textViewSupporterName)?.text =
                languageManager.getString("supporter_name")
        findViewById<TextView>(R.id.textViewSupporterMessage)?.text =
                languageManager.getString("supporter_message")
    }

    private fun setupNavigationDrawer() {
        val toggle =
                ActionBarDrawerToggle(
                        this,
                        drawerLayout,
                        null,
                        R.string.navigation_drawer_open,
                        R.string.navigation_drawer_close
                )
        drawerLayout.addDrawerListener(toggle)
        toggle.syncState()

        navigationView.setNavigationItemSelectedListener(this)
    }

    override fun onSupportNavigateUp(): Boolean {
        finish()
        return true
    }

    override fun onNavigationItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.nav_home -> {
                startActivity(Intent(this, MainActivity::class.java))
            }            R.id.nav_summary -> {
                startActivity(Intent(this, SummaryActivity::class.java))
            }
            R.id.nav_all_list -> {
                startActivity(Intent(this, AllListActivity::class.java))
            }
            R.id.nav_history -> {
                startActivity(Intent(this, HistoryActivity::class.java))
            }
            R.id.nav_currency_exchange -> {
                startActivity(Intent(this, CurrencyExchangeActivity::class.java))
            }
            R.id.nav_settings -> {
                startActivity(Intent(this, SettingsActivity::class.java))
            }
            R.id.nav_feedback -> {
                startActivity(Intent(this, FeedbackActivity::class.java))
            }
            R.id.nav_about -> {
                // Already in this activity, just close drawer
            }
            else -> {
                // Handle any other menu items
            }
        }
        drawerLayout.closeDrawer(GravityCompat.START)
        return true
    }
}
