package com.example.myapplication

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class FeedbackActivity : AppCompatActivity() {
    
    private lateinit var radioGroupRating: RadioGroup
    private lateinit var editTextFeedback: EditText
    private lateinit var editTextEmail: EditText
    private lateinit var buttonSubmit: Button
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_feedback)
        
        setupActionBar()
        initViews()
        setupClickListeners()
    }
    
    private fun setupActionBar() {
        supportActionBar?.title = "💬 Feedback"
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
}
