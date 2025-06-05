package com.example.myapplication

import android.content.Context
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate

class LanguageActivity : BaseActivity() {
    
    private lateinit var spinnerLanguage: Spinner
    private lateinit var buttonApply: Button
    private lateinit var languageAdapter: LanguageSpinnerAdapter
    private var selectedLanguageCode: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_language)
        
        initViews()
        setupSpinner()
        setupClickListeners()
        updateUITexts()
    }

    override fun onLanguageChanged() {
        // Update the UI immediately when language changes
        updateUITexts()
        // Don't recreate this activity since user is actively changing language
    }    private fun applyTheme() {
        val themePrefs = getSharedPreferences(ThemeActivity.THEME_PREFS, Context.MODE_PRIVATE)
        val savedTheme = themePrefs.getString(ThemeActivity.THEME_KEY, ThemeActivity.THEME_SYSTEM)
        
        when (savedTheme) {
            ThemeActivity.THEME_LIGHT -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO)
            ThemeActivity.THEME_DARK -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES)
            ThemeActivity.THEME_SYSTEM -> AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM)
        }
    }

    private fun initViews() {
        spinnerLanguage = findViewById(R.id.spinnerLanguage)
        buttonApply = findViewById(R.id.buttonApply)
        
        // Setup back button
        findViewById<ImageButton>(R.id.buttonBack).setOnClickListener {
            finish()
        }
    }    private fun setupSpinner() {
        val languages = languageManager.getAvailableLanguages()
        languageAdapter = LanguageSpinnerAdapter(this, languages)
        spinnerLanguage.adapter = languageAdapter
          // Set current language as selected
        val currentLanguage = languageManager.getCurrentLanguage()
        val currentIndex = languages.indexOfFirst { it.first == currentLanguage }
        if (currentIndex >= 0) {
            spinnerLanguage.setSelection(currentIndex)
        }
        selectedLanguageCode = currentLanguage
        
        spinnerLanguage.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val newLanguageCode = languages[position].first
                
                // Only apply change if it's different from current language
                if (newLanguageCode != languageManager.getCurrentLanguage()) {
                    selectedLanguageCode = newLanguageCode
                    
                    // Apply language change immediately
                    languageManager.setLanguage(selectedLanguageCode)
                    
                    // Update this activity's UI
                    updateUITexts()
                    
                    // Show confirmation message
                    showLanguageChangedMessage()
                } else {
                    selectedLanguageCode = newLanguageCode
                }
            }
            
            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    private fun setupClickListeners() {
        buttonApply.setOnClickListener {
            // Language changes are now applied immediately when selected
            // Just close the activity
            finish()
        }
    }    private fun updateUITexts() {
        // Update title
        findViewById<TextView>(R.id.textViewTitle).text = languageManager.getString("language_settings")
        
        // Update description text to use "select_preferred_language"
        findViewById<TextView>(R.id.textViewDescription)?.text = languageManager.getString("select_preferred_language")
        
        // Update select language label
        findViewById<TextView>(R.id.textViewSelectLanguage)?.text = languageManager.getString("select_language")
        
        // Update apply button
        buttonApply.text = languageManager.getString("apply")
        
        // Update language info text to use "language_changes_immediate"
        findViewById<TextView>(R.id.textViewLanguageInfo)?.text = languageManager.getString("language_changes_immediate")
        
        // Refresh spinner adapter to use new language
        val languages = languageManager.getAvailableLanguages()
        languageAdapter.updateLanguages(languages)
    }

    private fun showLanguageChangedMessage() {
        val message = languageManager.getString("language_changed")
        Toast.makeText(this, message, Toast.LENGTH_SHORT).show()
    }
}

// Custom adapter for language spinner
class LanguageSpinnerAdapter(
    private val context: Context,
    private var languages: List<Pair<String, String>>
) : BaseAdapter() {

    override fun getCount(): Int = languages.size

    override fun getItem(position: Int): Any = languages[position]

    override fun getItemId(position: Int): Long = position.toLong()

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: View.inflate(context, android.R.layout.simple_spinner_item, null)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = languages[position].second
        textView.textSize = 16f
        textView.setPadding(16, 12, 16, 12)
        return view
    }

    override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view = convertView ?: View.inflate(context, android.R.layout.simple_spinner_dropdown_item, null)
        val textView = view.findViewById<TextView>(android.R.id.text1)
        textView.text = languages[position].second
        textView.textSize = 16f
        textView.setPadding(24, 16, 24, 16)
        return view
    }
      fun updateLanguages(newLanguages: List<Pair<String, String>>) {
        languages = newLanguages
        notifyDataSetChanged()
    }
}
