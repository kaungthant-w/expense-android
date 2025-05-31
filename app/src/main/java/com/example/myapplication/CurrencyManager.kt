package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import java.text.NumberFormat
import java.util.*

class CurrencyManager private constructor(private val context: Context) {
    
    companion object {
        const val CURRENCY_USD = "USD"
        const val CURRENCY_MMK = "MMK"
        const val DEFAULT_EXCHANGE_RATE = 2100.0 // Default MMK per USD
        
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
}