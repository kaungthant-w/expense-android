package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import java.text.NumberFormat
import java.util.*

class CurrencyManager private constructor(private val context: Context) {
    
    companion object {
        const val CURRENCY_USD = "USD"
        const val CURRENCY_MMK = "MMK"
        const val DEFAULT_EXCHANGE_RATE = 3600.0 // Default MMK per USD
        
        private const val PREFS_NAME = "currency_prefs"
        private const val KEY_CURRENT_CURRENCY = "current_currency"
        private const val KEY_EXCHANGE_RATE = "exchange_rate"
        
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
    
    fun convertFromUsd(usdAmount: Double): Double {
        return usdAmount * getExchangeRate()
    }
    
    fun convertToUsd(mmkAmount: Double): Double {
        return mmkAmount / getExchangeRate()
    }
    
    fun formatCurrency(amount: Double): String {
        return when (getCurrentCurrency()) {
            CURRENCY_USD -> {
                val formatter = NumberFormat.getCurrencyInstance(Locale.US)
                formatter.format(amount)
            }
            CURRENCY_MMK -> {
                val formatter = NumberFormat.getNumberInstance(Locale.US)
                "${formatter.format(amount)} MMK"
            }
            else -> amount.toString()
        }
    }
    
    fun getCurrencySymbol(): String {
        return when (getCurrentCurrency()) {
            CURRENCY_USD -> "$"
            CURRENCY_MMK -> "MMK"
            else -> ""
        }
    }
    
    fun getDisplayAmount(originalAmount: Double): Double {
        return if (getCurrentCurrency() == CURRENCY_MMK) {
            convertFromUsd(originalAmount)
        } else {
            originalAmount
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
     * For MMK expenses stored natively, returns the amount directly.
     * For USD expenses, converts to MMK if MMK currency is selected.
     */    fun getDisplayAmountFromStored(storedAmount: Double, storedCurrency: String): Double {
        val currentCurrency = getCurrentCurrency()
        
        return when {
            // If stored currency matches current currency, display directly
            storedCurrency == currentCurrency -> storedAmount
            
            // If stored in USD but displaying in MMK, convert
            storedCurrency == CURRENCY_USD && currentCurrency == CURRENCY_MMK -> 
                convertFromUsd(storedAmount)
            
            // If stored in MMK but displaying in USD, convert
            storedCurrency == CURRENCY_MMK && currentCurrency == CURRENCY_USD -> 
                convertToUsd(storedAmount)
            
            // Default case
            else -> storedAmount
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