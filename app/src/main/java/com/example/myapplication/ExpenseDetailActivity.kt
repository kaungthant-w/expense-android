package com.example.myapplication

import android.app.AlertDialog
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.text.SimpleDateFormat
import java.util.*

class ExpenseDetailActivity : BaseActivity() {
    private lateinit var editTextName: EditText
    private lateinit var editTextPrice: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextDate: EditText
    private lateinit var editTextTime: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonDelete: Button
    private lateinit var buttonBack: ImageButton
    private lateinit var titleTextView: TextView
    private lateinit var nameLabel: TextView
    private lateinit var priceLabel: TextView
    private lateinit var descriptionLabel: TextView
    private lateinit var dateLabel: TextView
    private lateinit var timeLabel: TextView
    private lateinit var currencyManager: CurrencyManager
    private var expenseId: Long = -1
    private var isNewExpense = true
    private var currentCSRFToken: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_detail)
          
        currencyManager = CurrencyManager.getInstance(this)
        initViews()
        setupData()
        setupClickListeners()
        setupDateTimeFields()
        
        // Initialize CSRF token for security
        currentCSRFToken = InputValidationHelper.generateCSRFToken()
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
      private fun initViews() {
        editTextName = findViewById<EditText>(R.id.editTextName)
        editTextPrice = findViewById<EditText>(R.id.editTextPrice)
        editTextDescription = findViewById<EditText>(R.id.editTextDescription)
        editTextDate = findViewById<EditText>(R.id.editTextDate)
        editTextTime = findViewById<EditText>(R.id.editTextTime)
        buttonSave = findViewById<Button>(R.id.buttonSave)
        buttonDelete = findViewById<Button>(R.id.buttonDelete)
        buttonBack = findViewById<ImageButton>(R.id.buttonBack)
        titleTextView = findViewById<TextView>(R.id.titleTextView)
        nameLabel = findViewById<TextView>(R.id.nameLabel)
        priceLabel = findViewById<TextView>(R.id.priceLabel)
        descriptionLabel = findViewById<TextView>(R.id.descriptionLabel)
        dateLabel = findViewById<TextView>(R.id.dateLabel)
        timeLabel = findViewById<TextView>(R.id.timeLabel)
        
        // Setup enhanced price input field with validation and security
        InputValidationHelper.setupPriceInputField(editTextPrice, languageManager, preventCopyPaste = true)
    }
    
    private fun setupClickListeners() {
        buttonBack.setOnClickListener {
            finish()
        }
        
        buttonSave.setOnClickListener {
            saveExpense()
        }
        
        buttonDelete.setOnClickListener {
            showDeleteConfirmation()
        }
    }
    
    private fun setupData() {
        expenseId = intent.getLongExtra("expense_id", -1)
          
        if (expenseId != -1L) {
            // Editing existing expense
            isNewExpense = false
            editTextName.setText(intent.getStringExtra("expense_name") ?: "")
            
            // Get stored price and currency to display in current currency format
            val storedPrice = intent.getDoubleExtra("expense_price", 0.0)
            val storedCurrency = intent.getStringExtra("expense_currency") ?: "USD"
            val displayAmount = currencyManager.getDisplayAmountFromStored(storedPrice, storedCurrency)
            editTextPrice.setText(displayAmount.toString())
            
            editTextDescription.setText(intent.getStringExtra("expense_description") ?: "")
            editTextDate.setText(intent.getStringExtra("expense_date") ?: "")
            editTextTime.setText(intent.getStringExtra("expense_time") ?: "")
            buttonDelete.isEnabled = true
        } else {
            // Creating new expense
            isNewExpense = true
            val calendar = Calendar.getInstance()
            val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
            val timeFormat = SimpleDateFormat("HH:mm", Locale.getDefault())
            editTextDate.setText(dateFormat.format(calendar.time))
            editTextTime.setText(timeFormat.format(calendar.time))
            buttonDelete.isEnabled = false
        }
        
        // Update UI texts after determining the mode
        updateUITexts()
    }
    
    private fun setupDateTimeFields() {
        editTextDate.setOnClickListener {
            showDatePicker()
        }
        
        editTextTime.setOnClickListener {
            showTimePicker()
        }
    }
    
    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val datePickerDialog = DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                val selectedDate = String.format("%02d/%02d/%d", dayOfMonth, month + 1, year)
                editTextDate.setText(selectedDate)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        )
        datePickerDialog.show()
    }
    
    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val timePickerDialog = TimePickerDialog(
            this,
            { _, hourOfDay, minute ->
                val selectedTime = String.format("%02d:%02d", hourOfDay, minute)
                editTextTime.setText(selectedTime)
            },
            calendar.get(Calendar.HOUR_OF_DAY),
            calendar.get(Calendar.MINUTE),
            true
        )
        timePickerDialog.show()
    }

    private fun saveExpense() {
        // CSRF token validation for security
        val token = currentCSRFToken ?: ""
        if (!InputValidationHelper.validateCSRFToken(token, currentCSRFToken)) {
            Toast.makeText(this, languageManager.getString("csrf_token_invalid"), Toast.LENGTH_LONG).show()
            return
        }
        
        val name = editTextName.text.toString().trim()
        val priceText = editTextPrice.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val date = editTextDate.text.toString().trim()
        val time = editTextTime.text.toString().trim()
          
        // Security validation for name using InputValidationHelper
        val nameValidation = InputValidationHelper.validateName(name, languageManager)
        if (!nameValidation.isValid) {
            InputValidationHelper.applyValidationToField(editTextName, nameValidation)
            if (nameValidation.securityThreatDetected) {
                Toast.makeText(this, languageManager.getString("name_security_error"), Toast.LENGTH_LONG).show()
            }
            return
        }
        
        // Security validation for description using InputValidationHelper
        val descriptionValidation = InputValidationHelper.validateDescription(description, languageManager)
        if (!descriptionValidation.isValid) {
            InputValidationHelper.applyValidationToField(editTextDescription, descriptionValidation)
            if (descriptionValidation.securityThreatDetected) {
                Toast.makeText(this, languageManager.getString("description_security_error"), Toast.LENGTH_LONG).show()
            }
            return
        }
          // Enhanced price validation using InputValidationHelper
        val priceValidation = InputValidationHelper.validatePriceInput(priceText, languageManager)
        if (!priceValidation.isValid) {
            editTextPrice.error = priceValidation.errorMessage
            editTextPrice.requestFocus()
            return
        }

        // Use CurrencyManager for native currency storage
        val storageAmount = currencyManager.getStorageAmount(priceValidation.sanitizedValue)
        val storageCurrency = currencyManager.getStorageCurrency()
        
        // Create result intent with sanitized inputs
        val resultIntent = Intent()
        resultIntent.putExtra("expense_id", expenseId)
        resultIntent.putExtra("expense_name", nameValidation.sanitizedInput)
        resultIntent.putExtra("expense_price", storageAmount)
        resultIntent.putExtra("expense_description", descriptionValidation.sanitizedInput)
        resultIntent.putExtra("expense_date", date)
        resultIntent.putExtra("expense_time", time)
        resultIntent.putExtra("expense_currency", storageCurrency)
        resultIntent.putExtra("is_new_expense", isNewExpense)
          
        setResult(RESULT_OK, resultIntent)
        Toast.makeText(this, if (isNewExpense) languageManager.getString("expense_added") else languageManager.getString("expense_updated"), Toast.LENGTH_SHORT).show()
        finish()
    }
      
    private fun showDeleteConfirmation() {
        AlertDialog.Builder(this)
            .setTitle(languageManager.getString("delete_expense_title"))
            .setMessage(languageManager.getString("delete_expense_message"))
            .setPositiveButton(languageManager.getString("delete")) { _, _ ->
                deleteExpense()
            }
            .setNegativeButton(languageManager.getString("cancel"), null)
            .show()
    }
    
    private fun deleteExpense() {
        val resultIntent = Intent()
        resultIntent.putExtra("expense_id", expenseId)
        resultIntent.putExtra("delete_expense", true)
          
        setResult(RESULT_OK, resultIntent)
        Toast.makeText(this, languageManager.getString("expense_deleted"), Toast.LENGTH_SHORT).show()
        finish()
    }
      
    private fun updateUITexts() {
        // Update title based on mode
        titleTextView.text = if (isNewExpense) {
            languageManager.getString("add_expense_title")
        } else {
            languageManager.getString("edit_expense_title")
        }
        
        // Update labels
        nameLabel.text = "💼 " + languageManager.getString("expense_name")
        priceLabel.text = "💵 " + languageManager.getString("expense_price")
        descriptionLabel.text = "📝 " + languageManager.getString("expense_description")
        dateLabel.text = "📅 " + languageManager.getString("expense_date")
        timeLabel.text = "🕐 " + languageManager.getString("expense_time")
        
        // Update button texts
        buttonSave.text = "💾 " + languageManager.getString("save")
        buttonDelete.text = "🗑️ " + languageManager.getString("delete")
        
        // Update EditText hints
        editTextName.hint = languageManager.getString("expense_name_hint")
        editTextPrice.hint = languageManager.getString("expense_price_hint")
        editTextDescription.hint = languageManager.getString("expense_description_hint")
        editTextDate.hint = languageManager.getString("expense_date_hint")
        editTextTime.hint = languageManager.getString("expense_time_hint")
    }
}
