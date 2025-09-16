package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import java.text.NumberFormat
import java.util.*

class CurrencyManager private constructor(private val context: Context) {    companion object {
        const val CURRENCY_USD = "USD"
        const val CURRENCY_MMK = "MMK"
        const val CURRENCY_SGD = "SGD"
        const val CURRENCY_THB = "THB"
        const val CURRENCY_JPY = "JPY"
        const val CURRENCY_CNY = "CNY"
        const val CURRENCY_MYR = "MYR"
        const val CURRENCY_EUR = "EUR"
        const val CURRENCY_KRW = "KRW"
        
        const val DEFAULT_EXCHANGE_RATE = 3600.0 // Default MMK per USD
        
        private const val PREFS_NAME = "currency_prefs"
        private const val KEY_CURRENT_CURRENCY = "current_currency"
        private const val KEY_EXCHANGE_RATE = "exchange_rate"
        private const val KEY_EXCHANGE_RATES = "exchange_rates"
        
        // Supported currencies list
        val SUPPORTED_CURRENCIES = listOf(CURRENCY_USD, CURRENCY_MMK, CURRENCY_SGD, CURRENCY_THB, CURRENCY_JPY, CURRENCY_CNY, CURRENCY_MYR, CURRENCY_EUR, CURRENCY_KRW)
        
        @Volatile
        private var INSTANCE: CurrencyManager? = null
        
        fun getInstance(context: Context): CurrencyManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: CurrencyManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    
    fun getCurrentCurrency(): String {
        return sharedPreferences.getString(KEY_CURRENT_CURRENCY, CURRENCY_USD) ?: CURRENCY_USD
    }
    
    fun setCurrentCurrency(currency: String) {
        sharedPreferences.edit()
            .putString(KEY_CURRENT_CURRENCY, currency)
            .apply()
    }
    
    fun getExchangeRate(): Double {
        return sharedPreferences.getFloat(KEY_EXCHANGE_RATE, DEFAULT_EXCHANGE_RATE.toFloat()).toDouble()
    }
      fun setExchangeRate(rate: Double) {
        sharedPreferences.edit()
            .putFloat(KEY_EXCHANGE_RATE, rate.toFloat())
            .apply()
    }
    
    // New methods for multiple currency exchange rates
    fun getAllExchangeRates(): Map<String, Double> {
        val rates = mutableMapOf<String, Double>()
        val ratesString = sharedPreferences.getString(KEY_EXCHANGE_RATES, "")
        
        if (!ratesString.isNullOrEmpty()) {
            try {
                val pairs = ratesString.split(",")
                for (pair in pairs) {
                    val parts = pair.split(":")
                    if (parts.size == 2) {
                        rates[parts[0]] = parts[1].toDouble()
                    }
                }
            } catch (e: Exception) {
                // Return default rates if parsing fails
                return getDefaultRates()
            }
        }
        
        // Ensure all supported currencies have rates
        return getDefaultRates().plus(rates)
    }
    
    fun setAllExchangeRates(rates: Map<String, Double>) {
        val ratesString = rates.map { "${it.key}:${it.value}" }.joinToString(",")
        sharedPreferences.edit()
            .putString(KEY_EXCHANGE_RATES, ratesString)
            .apply()
    }
    
    fun getExchangeRateForCurrency(currency: String): Double {
        return getAllExchangeRates()[currency] ?: getDefaultRateForCurrency(currency)
    }
      private fun getDefaultRates(): Map<String, Double> {
        return mapOf(
            CURRENCY_USD to 3600.0,
            CURRENCY_MMK to 1.0,
            CURRENCY_SGD to 2650.0,
            CURRENCY_THB to 102.0,
            CURRENCY_JPY to 24.0,
            CURRENCY_CNY to 500.0
        )
    }
      private fun getDefaultRateForCurrency(currency: String): Double {
        return when (currency) {
            CURRENCY_USD -> 3600.0
            CURRENCY_MMK -> 1.0
            CURRENCY_SGD -> 2650.0
            CURRENCY_THB -> 102.0
            CURRENCY_JPY -> 24.0
            CURRENCY_CNY -> 500.0
            else -> 1.0
        }
    }
      fun convertFromUsd(usdAmount: Double, targetCurrency: String = getCurrentCurrency()): Double {
        return when (targetCurrency) {
            CURRENCY_USD -> usdAmount
            CURRENCY_MMK -> usdAmount * getExchangeRateForCurrency(CURRENCY_USD)
            else -> {
                // Convert USD to target currency via MMK
                val mmkAmount = usdAmount * getExchangeRateForCurrency(CURRENCY_USD)
                mmkAmount / getExchangeRateForCurrency(targetCurrency)
            }
        }
    }
    
    fun convertToUsd(amount: Double, fromCurrency: String = getCurrentCurrency()): Double {
        return when (fromCurrency) {
            CURRENCY_USD -> amount
            CURRENCY_MMK -> amount / getExchangeRateForCurrency(CURRENCY_USD)
            else -> {
                // Convert to MMK first, then to USD
                val mmkAmount = amount * getExchangeRateForCurrency(fromCurrency)
                mmkAmount / getExchangeRateForCurrency(CURRENCY_USD)
            }
        }
    }
    
    fun convertBetweenCurrencies(amount: Double, fromCurrency: String, toCurrency: String): Double {
        if (fromCurrency == toCurrency) return amount
        
        // Convert to USD first, then to target currency
        val usdAmount = convertToUsd(amount, fromCurrency)
        return convertFromUsd(usdAmount, toCurrency)
    }      fun formatCurrency(amount: Double): String {
        return when (getCurrentCurrency()) {
            CURRENCY_USD -> {
                val formatter = NumberFormat.getCurrencyInstance(Locale.US)
                formatter.format(amount)
            }
            CURRENCY_MMK -> {
                val formatter = NumberFormat.getNumberInstance(Locale.US)
                "${formatter.format(amount)} MMK"
            }
            CURRENCY_SGD -> {
                val formatter = NumberFormat.getNumberInstance(Locale.US)
                "${formatter.format(amount)} SGD"
            }
            CURRENCY_THB -> {
                val formatter = NumberFormat.getNumberInstance(Locale.US)
                "${formatter.format(amount)} THB"
            }
            CURRENCY_JPY -> {
                val formatter = NumberFormat.getNumberInstance(Locale.US)
                "${formatter.format(amount.toInt())} JPY" // JPY doesn't use decimals
            }
            CURRENCY_CNY -> {
                val formatter = NumberFormat.getNumberInstance(Locale.US)
                "¥${formatter.format(amount)}"
            }
            else -> amount.toString()
        }
    }
      fun getCurrencySymbol(): String {
        return when (getCurrentCurrency()) {
            CURRENCY_USD -> "$"
            CURRENCY_MMK -> "MMK"
            CURRENCY_SGD -> "SGD"
            CURRENCY_THB -> "THB"
            CURRENCY_JPY -> "¥"
            CURRENCY_CNY -> "¥"
            else -> ""
        }
    }
      fun getCurrencyDisplayName(): String {
        return when (getCurrentCurrency()) {
            CURRENCY_USD -> "US Dollar"
            CURRENCY_MMK -> "Myanmar Kyat"
            CURRENCY_SGD -> "Singapore Dollar"
            CURRENCY_THB -> "Thai Baht"
            CURRENCY_JPY -> "Japanese Yen"
            CURRENCY_CNY -> "Chinese Yuan"
            else -> getCurrentCurrency()
        }
    }
      fun getDisplayAmount(originalAmount: Double): Double {
        val currentCurrency = getCurrentCurrency()
        return if (currentCurrency == CURRENCY_USD) {
            originalAmount
        } else {
            convertFromUsd(originalAmount, currentCurrency)
        }
    }
    
    // NEW METHODS FOR NATIVE CURRENCY STORAGE
    
    /**
     * Get the storage amount - converts input price to storage format based on current currency.
     * If USD is selected, stores directly. If MMK is selected, stores in MMK natively.
     */
    fun getStorageAmount(inputAmount: Double): Double {
        return when (getCurrentCurrency()) {
            CURRENCY_USD -> inputAmount // Store as USD
            CURRENCY_MMK -> inputAmount // Store as MMK (native)
            else -> inputAmount
        }
    }
      /**
     * Get the display amount from stored amount - handles display conversion.
     * For expenses stored in any currency, converts to the current display currency.
     */    
    fun getDisplayAmountFromStored(storedAmount: Double, storedCurrency: String): Double {
        val currentCurrency = getCurrentCurrency()
        
        return when {
            // If stored currency matches current currency, display directly
            storedCurrency == currentCurrency -> storedAmount
            
            // Convert between different currencies
            else -> convertBetweenCurrencies(storedAmount, storedCurrency, currentCurrency)
        }
    }
    
    /**
     * Get the currency that should be used for storing new expenses
     */
    fun getStorageCurrency(): String {
        return getCurrentCurrency()
    }
    
    /**
     * Check if an amount is stored in native MMK format
     */
    fun isNativeMmkAmount(storedCurrency: String): Boolean {
        return storedCurrency == CURRENCY_MMK
    }
}