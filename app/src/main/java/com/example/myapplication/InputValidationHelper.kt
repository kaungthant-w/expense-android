package com.example.myapplication

import android.text.Html
import android.text.TextUtils
import android.widget.EditText
import android.text.TextWatcher
import android.text.Editable
import android.text.InputFilter
import android.text.Spanned
import java.util.regex.Pattern

/**
 * Input Validation and Security Helper Class
 * Provides comprehensive validation and security measures for user input
 */
object InputValidationHelper {
    
    // Security patterns for validation
    private val XSS_PATTERN = Pattern.compile(
        "(<script[^>]*>.*?</script>)|(<iframe[^>]*>.*?</iframe>)|" +
        "(<object[^>]*>.*?</object>)|(<embed[^>]*>.*?</embed>)|" +        "(javascript:)|(on\\w+\\s*=)|(<\\s*\\w+[^>]*\\s+on\\w+\\s*=)",
        Pattern.CASE_INSENSITIVE or Pattern.DOTALL
    )
      private val SQL_INJECTION_PATTERN = Pattern.compile(
        "('|--|;|\\||\\*|%|\\?|\\[|\\]|\\{|\\})" +
        "|(union(\\s+(all|distinct))?\\s+select)" +
        "|(insert(\\s+into)?)" +
        "|(update\\s+\\w+\\s+set)" +
        "|(delete(\\s+from)?)" +
        "|(drop\\s+(table|database|schema|view|index))" +
        "|(alter\\s+(table|database|schema))" +
        "|(exec(ute)?\\s)" +
        "|(script\\s*:)",
        Pattern.CASE_INSENSITIVE
    )
    
    private val HTML_TAG_PATTERN = Pattern.compile(
        "<[^>]+>", 
        Pattern.CASE_INSENSITIVE
    )
    
    private val SPECIAL_CHARS_PATTERN = Pattern.compile(
        "[<>\"'&\\x00-\\x1f\\x7f-\\x9f]"
    )
    
    // Numeric validation patterns
    private val DECIMAL_PATTERN = Pattern.compile("^[0-9]*\\.?[0-9]*$")
    private val EXPONENTIAL_PATTERN = Pattern.compile("[eE][+-]?[0-9]+")
    private val VALID_DECIMAL_PATTERN = Pattern.compile("^[0-9]+(\\.([0-9]{1,2}))?$")
    
    // Character limits
    const val NAME_MAX_LENGTH = 20
    const val DESCRIPTION_MAX_LENGTH = 300
    const val NAME_MIN_LENGTH = 1
    
    // Numeric input limits
    const val PRICE_MAX_LENGTH = 12  // Total characters including decimal point
    const val PRICE_MAX_VALUE = 999999999.99  // 9 digits + 2 decimal places
    const val PRICE_MIN_VALUE = 0.01  // Minimum positive value
    const val DECIMAL_PLACES = 2
    
    /**
     * Validation result class
     */
    data class ValidationResult(
        val isValid: Boolean,
        val errorMessage: String = "",
        val sanitizedInput: String = "",
        val securityThreatDetected: Boolean = false
    )
    
    /**
     * Numeric validation result class
     */
    data class NumericValidationResult(
        val isValid: Boolean,
        val errorMessage: String = "",
        val sanitizedValue: Double = 0.0,
        val formattedValue: String = ""
    )
      /**
     * Enhanced numeric validation for price/amount fields
     */
    fun validatePriceInput(input: String, languageManager: LanguageManager): NumericValidationResult {
        val trimmedInput = input.trim()
        
        // Check if empty
        if (trimmedInput.isEmpty()) {
            return NumericValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("price_required")
            )
        }
        
        // Check total length (including decimal point)
        if (trimmedInput.length > PRICE_MAX_LENGTH) {
            return NumericValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("price_too_long")
            )
        }
        
        // Check for exponential notation (1e6, 1E-3, etc.)
        if (EXPONENTIAL_PATTERN.matcher(trimmedInput).find()) {
            return NumericValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("price_no_exponential")
            )
        }
        
        // Check if input contains only valid decimal characters
        if (!DECIMAL_PATTERN.matcher(trimmedInput).matches()) {
            return NumericValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("invalid_price_format")
            )
        }
        
        // Parse the number
        val value = try {
            trimmedInput.toDouble()
        } catch (e: NumberFormatException) {
            return NumericValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("invalid_price_format")
            )
        }
        
        // Check for negative or zero values
        if (value <= 0) {
            return NumericValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("price_must_be_positive")
            )
        }
        
        // Check for overflow (maximum value)
        if (value > PRICE_MAX_VALUE) {
            return NumericValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("price_too_large")
            )
        }
        
        // Check for proper decimal format (max 2 decimal places)
        if (!VALID_DECIMAL_PATTERN.matcher(trimmedInput).matches()) {
            return NumericValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("price_max_two_decimals")
            )
        }
        
        // Format the value to ensure proper decimal places
        val formattedValue = String.format("%.2f", value)
        
        return NumericValidationResult(
            isValid = true,
            sanitizedValue = value,
            formattedValue = formattedValue
        )
    }
    
    /**
     * Create input filter for price fields to restrict input in real-time
     */
    fun createPriceInputFilter(): InputFilter {
        return object : InputFilter {
            override fun filter(
                source: CharSequence,
                start: Int,
                end: Int,
                dest: Spanned,
                dstart: Int,
                dend: Int
            ): CharSequence? {
                val result = StringBuilder(dest).apply {
                    replace(dstart, dend, source.subSequence(start, end).toString())
                }.toString()
                
                // Allow empty string for deletion
                if (result.isEmpty()) return null
                
                // Check total length
                if (result.length > PRICE_MAX_LENGTH) {
                    return ""
                }
                
                // Allow only digits and single decimal point
                if (!DECIMAL_PATTERN.matcher(result).matches()) {
                    return ""
                }
                
                // Prevent exponential notation
                if (EXPONENTIAL_PATTERN.matcher(result).find()) {
                    return ""
                }
                
                // Check decimal places (max 2 after decimal point)
                val decimalIndex = result.indexOf('.')
                if (decimalIndex >= 0 && result.length - decimalIndex > 3) {
                    return ""
                }
                
                // Try parsing to check for valid number
                try {
                    val value = result.toDouble()
                    if (value > PRICE_MAX_VALUE) {
                        return ""
                    }
                } catch (e: NumberFormatException) {
                    return ""
                }
                
                return null // Accept the input
            }
        }
    }
    
    /**
     * Create text watcher for real-time price validation
     */
    fun createPriceTextWatcher(
        editText: EditText,
        languageManager: LanguageManager,
        onValidationChange: ((Boolean) -> Unit)? = null
    ): TextWatcher {
        return object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            
            override fun afterTextChanged(s: Editable?) {
                val input = s?.toString() ?: ""
                
                if (input.isEmpty()) {
                    editText.error = null
                    onValidationChange?.invoke(false)
                    return
                }
                
                val validationResult = validatePriceInput(input, languageManager)
                
                if (!validationResult.isValid) {
                    editText.error = validationResult.errorMessage
                    onValidationChange?.invoke(false)
                } else {
                    editText.error = null
                    onValidationChange?.invoke(true)
                }
            }
        }
    }
    
    /**
     * Setup enhanced price input field with all validations
     */
    fun setupPriceInputField(
        editText: EditText,
        languageManager: LanguageManager,
        preventCopyPaste: Boolean = false,
        onValidationChange: ((Boolean) -> Unit)? = null
    ) {
        // Set input type to decimal number with number keyboard
        editText.inputType = android.text.InputType.TYPE_CLASS_NUMBER or 
                           android.text.InputType.TYPE_NUMBER_FLAG_DECIMAL
        
        // Apply input filter
        editText.filters = arrayOf(createPriceInputFilter())
        
        // Add text watcher for real-time validation
        editText.addTextChangedListener(createPriceTextWatcher(editText, languageManager, onValidationChange))
        
        // Disable copy/paste if requested
        if (preventCopyPaste) {
            editText.setOnLongClickListener { true } // Disable long click (copy/paste menu)
            editText.isLongClickable = false
            editText.setTextIsSelectable(false)
        }
        
        // Set max length as backup
        editText.filters = editText.filters + InputFilter.LengthFilter(PRICE_MAX_LENGTH)
    }
    
    /**
     * Comprehensive validation for Name field
     */
    fun validateName(input: String, languageManager: LanguageManager): ValidationResult {
        val trimmedInput = input.trim()
        
        // Check if empty
        if (trimmedInput.isEmpty()) {
            return ValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("name_required")
            )
        }
        
        // Check minimum length
        if (trimmedInput.length < NAME_MIN_LENGTH) {
            return ValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("name_too_short")
            )
        }
        
        // Check maximum length
        if (trimmedInput.length > NAME_MAX_LENGTH) {
            return ValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("name_too_long")
            )
        }
        
        // Security validation
        val securityResult = performSecurityValidation(trimmedInput)
        if (securityResult.securityThreatDetected) {
            return ValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("name_security_error"),
                securityThreatDetected = true
            )
        }
        
        // Check for valid characters (allow letters, numbers, spaces, common punctuation)
        if (!isValidNameFormat(trimmedInput)) {
            return ValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("name_invalid_characters")
            )
        }
        
        // Sanitize input
        val sanitized = sanitizeInput(trimmedInput)
        
        return ValidationResult(
            isValid = true,
            sanitizedInput = sanitized
        )
    }
    
    /**
     * Comprehensive validation for Description field
     */
    fun validateDescription(input: String, languageManager: LanguageManager): ValidationResult {
        val trimmedInput = input.trim()
        
        // Description is optional, so empty is allowed
        if (trimmedInput.isEmpty()) {
            return ValidationResult(
                isValid = true,
                sanitizedInput = ""
            )
        }
        
        // Check maximum length
        if (trimmedInput.length > DESCRIPTION_MAX_LENGTH) {
            return ValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("description_too_long")
            )
        }
        
        // Security validation
        val securityResult = performSecurityValidation(trimmedInput)
        if (securityResult.securityThreatDetected) {
            return ValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("description_security_error"),
                securityThreatDetected = true
            )
        }
        
        // Sanitize input
        val sanitized = sanitizeInput(trimmedInput)
        
        return ValidationResult(
            isValid = true,
            sanitizedInput = sanitized
        )
    }
    
    /**
     * Security validation to detect potential threats
     */
    private fun performSecurityValidation(input: String): ValidationResult {
        // Check for XSS patterns
        if (XSS_PATTERN.matcher(input).find()) {
            return ValidationResult(
                isValid = false,
                securityThreatDetected = true
            )
        }
        
        // Check for SQL injection patterns
        if (SQL_INJECTION_PATTERN.matcher(input).find()) {
            return ValidationResult(
                isValid = false,
                securityThreatDetected = true
            )
        }
        
        // Check for HTML tags
        if (HTML_TAG_PATTERN.matcher(input).find()) {
            return ValidationResult(
                isValid = false,
                securityThreatDetected = true
            )
        }
        
        return ValidationResult(isValid = true)
    }
    
    /**
     * Check if name contains only valid characters
     */
    private fun isValidNameFormat(input: String): Boolean {
        // Allow letters, numbers, spaces, and basic punctuation
        val validPattern = Pattern.compile("^[\\p{L}\\p{N}\\s.,!?&@#$%()\\-_+=]*$")
        return validPattern.matcher(input).matches() && !SPECIAL_CHARS_PATTERN.matcher(input).find()
    }
    
    /**
     * Sanitize input by removing/escaping dangerous characters
     */
    private fun sanitizeInput(input: String): String {
        var sanitized = input
        
        // Remove null characters and control characters
        sanitized = sanitized.replace(Regex("[\\x00-\\x1f\\x7f-\\x9f]"), "")
          // Escape HTML entities
        sanitized = Html.escapeHtml(sanitized)
        
        // Remove potential script tags and dangerous patterns
        sanitized = sanitized.replace(Regex("<[^>]*>"), "")
        sanitized = sanitized.replace(Regex("javascript:", RegexOption.IGNORE_CASE), "")
        sanitized = sanitized.replace(Regex("on\\w+\\s*=", RegexOption.IGNORE_CASE), "")
        
        return sanitized.trim()
    }
    
    /**
     * Apply validation to EditText field with visual feedback
     */
    fun applyValidationToField(
        editText: EditText,
        validationResult: ValidationResult
    ) {
        if (!validationResult.isValid) {
            editText.error = validationResult.errorMessage
            editText.requestFocus()
        } else {
            editText.error = null
            // Update with sanitized input if different
            if (validationResult.sanitizedInput != editText.text.toString()) {
                editText.setText(validationResult.sanitizedInput)
                editText.setSelection(editText.text.length)
            }
        }
    }
    
    /**
     * Generate a secure token for CSRF protection (basic implementation)
     */
    fun generateCSRFToken(): String {
        val timestamp = System.currentTimeMillis()
        val random = (Math.random() * 10000).toInt()
        return "csrf_${timestamp}_${random}"
    }
    
    /**
     * Validate CSRF token (basic implementation)
     */
    fun validateCSRFToken(token: String, sessionToken: String?): Boolean {
        if (sessionToken == null || token != sessionToken) {
            return false
        }
        
        // Extract timestamp from token
        val parts = token.split("_")
        if (parts.size != 3 || parts[0] != "csrf") {
            return false
        }
        
        return try {
            val tokenTime = parts[1].toLong()
            val currentTime = System.currentTimeMillis()
            // Token valid for 1 hour (3600000 ms)
            currentTime - tokenTime < 3600000
        } catch (e: NumberFormatException) {
            false
        }
    }
    
    /**
     * Check if input contains potential security threats
     */
    fun containsSecurityThreats(input: String): Boolean {
        return XSS_PATTERN.matcher(input).find() ||
               SQL_INJECTION_PATTERN.matcher(input).find() ||
               HTML_TAG_PATTERN.matcher(input).find()
    }
    
    /**
     * Clean and prepare input for storage
     */
    fun prepareForStorage(input: String): String {
        return sanitizeInput(input).take(if (input.length <= NAME_MAX_LENGTH) NAME_MAX_LENGTH else DESCRIPTION_MAX_LENGTH)
    }
    
    /**
     * Comprehensive validation for numeric input (e.g., price)
     */
    fun validateNumericInput(input: String, languageManager: LanguageManager): NumericValidationResult {
        val trimmedInput = input.trim()
        
        // Check if empty
        if (trimmedInput.isEmpty()) {
            return NumericValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("numeric_input_required")
            )
        }
        
        // Check for valid decimal format
        if (!VALID_DECIMAL_PATTERN.matcher(trimmedInput).matches()) {
            return NumericValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("invalid_decimal_format")
            )
        }
        
        // Check for exponential notation
        if (EXPONENTIAL_PATTERN.matcher(trimmedInput).find()) {
            return NumericValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("exponential_notation_not_allowed")
            )
        }
        
        // Parse and validate range
        return try {
            val value = trimmedInput.toDouble()
            
            // Check for overflow
            if (value > PRICE_MAX_VALUE) {
                NumericValidationResult(
                    isValid = false,
                    errorMessage = languageManager.getString("value_too_large")
                )
            } else if (value < PRICE_MIN_VALUE) {
                NumericValidationResult(
                    isValid = false,
                    errorMessage = languageManager.getString("value_too_small")
                )
            } else {
                NumericValidationResult(
                    isValid = true,
                    sanitizedValue = value,
                    formattedValue = String.format("%.2f", value)
                )
            }
        } catch (e: NumberFormatException) {
            NumericValidationResult(
                isValid = false,
                errorMessage = languageManager.getString("malformed_number")
            )
        }
    }
}
