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
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.app.AppCompatDelegate
import java.text.SimpleDateFormat
import java.util.*

class ExpenseDetailActivity : AppCompatActivity() {
    
    private lateinit var editTextName: EditText
    private lateinit var editTextPrice: EditText
    private lateinit var editTextDescription: EditText
    private lateinit var editTextDate: EditText
    private lateinit var editTextTime: EditText
    private lateinit var buttonSave: Button
    private lateinit var buttonDelete: Button
    private lateinit var buttonBack: ImageButton
    private lateinit var currencyManager: CurrencyManager
    private lateinit var languageManager: LanguageManager
      private var expenseId: Long = -1
    private var isNewExpense = true
    
    override fun onCreate(savedInstanceState: Bundle?) {
        applyTheme()
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_detail)
          currencyManager = CurrencyManager.getInstance(this)
        languageManager = LanguageManager.getInstance(this)
        initViews()
        setupData()
        setupClickListeners()
        setupDateTimeFields()
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
        editTextName = findViewById(R.id.editTextName)
        editTextPrice = findViewById(R.id.editTextPrice)
        editTextDescription = findViewById(R.id.editTextDescription)
        editTextDate = findViewById(R.id.editTextDate)
        editTextTime = findViewById(R.id.editTextTime)
        buttonSave = findViewById(R.id.buttonSave)
        buttonDelete = findViewById(R.id.buttonDelete)
        buttonBack = findViewById(R.id.buttonBack)
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
            editTextPrice.setText(intent.getDoubleExtra("expense_price", 0.0).toString())
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
        val name = editTextName.text.toString().trim()
        val priceText = editTextPrice.text.toString().trim()
        val description = editTextDescription.text.toString().trim()
        val date = editTextDate.text.toString().trim()
        val time = editTextTime.text.toString().trim()
          // Validation
        if (name.isEmpty()) {
            editTextName.error = languageManager.getString("name_required")
            editTextName.requestFocus()
            return
        }
        
        if (priceText.isEmpty()) {
            editTextPrice.error = languageManager.getString("price_required")
            editTextPrice.requestFocus()
            return
        }
        
        val price = priceText.toDoubleOrNull()
        if (price == null || price <= 0) {
            editTextPrice.error = languageManager.getString("invalid_price_format")
            editTextPrice.requestFocus()
            return
        }

        // Use CurrencyManager for native currency storage
        val storageAmount = currencyManager.getStorageAmount(price)
        val storageCurrency = currencyManager.getStorageCurrency()
        
        // Create result intent
        val resultIntent = Intent().apply {
            putExtra("expense_id", expenseId)
            putExtra("expense_name", name)
            putExtra("expense_price", storageAmount)
            putExtra("expense_description", description)
            putExtra("expense_date", date)
            putExtra("expense_time", time)
            putExtra("expense_currency", storageCurrency)
            putExtra("is_new_expense", isNewExpense)
        }
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
        val resultIntent = Intent().apply {
            putExtra("expense_id", expenseId)
            putExtra("delete_expense", true)
        }
          setResult(RESULT_OK, resultIntent)
        Toast.makeText(this, languageManager.getString("expense_deleted"), Toast.LENGTH_SHORT).show()
        finish()
    }
    
    private fun updateUITexts() {
        // Update button texts
        buttonSave.text = languageManager.getString("save")
        buttonDelete.text = languageManager.getString("delete")
        
        // Update EditText hints
        editTextName.hint = languageManager.getString("expense_name")
        editTextPrice.hint = languageManager.getString("expense_price")
        editTextDescription.hint = languageManager.getString("expense_description")
        editTextDate.hint = languageManager.getString("expense_date")
        editTextTime.hint = languageManager.getString("expense_time")
        
        // Update title based on mode
        supportActionBar?.title = if (isNewExpense) {
            languageManager.getString("add_expense_title")
        } else {
            languageManager.getString("edit_expense_title")
        }
    }
}
