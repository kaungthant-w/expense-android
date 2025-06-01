package com.example.myapplication

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.util.Log
import org.json.JSONObject
import java.io.BufferedReader
import java.io.InputStreamReader
import java.net.HttpURLConnection
import java.net.URL
import java.util.concurrent.Executors

class CurrencyApiService(private val context: Context) {
    
    companion object {
        private const val TAG = "CurrencyApiService"
        private const val API_URL = "https://myanmar-currency-api.github.io/api/latest.json"
        private const val TIMEOUT_MS = 10000
    }
      interface CurrencyRateCallback {
        fun onSuccess(usdToMmk: Double)
        fun onError(error: String)
    }
    
    interface AllRatesCallback {
        fun onSuccess(rates: Map<String, Double>)
        fun onError(error: String)
    }
    
    fun fetchLatestExchangeRate(callback: CurrencyRateCallback) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        
        executor.execute {
            try {
                val url = URL(API_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                connection.setRequestProperty("Accept", "application/json")
                
                val responseCode = connection.responseCode
                Log.d(TAG, "Response Code: $responseCode")
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()
                    var line: String?
                    
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    
                    reader.close()
                    connection.disconnect()
                    
                    val jsonResponse = response.toString()
                    Log.d(TAG, "API Response: $jsonResponse")
                    
                    // Parse the JSON response
                    val jsonObject = JSONObject(jsonResponse)
                    
                    // The API returns rates with base currency
                    // We need to find USD to MMK rate
                    val usdToMmkRate = parseUsdToMmkRate(jsonObject)
                    
                    handler.post {
                        callback.onSuccess(usdToMmkRate)
                    }
                    
                } else {
                    val errorMessage = "HTTP Error: $responseCode"
                    Log.e(TAG, errorMessage)
                    handler.post {
                        callback.onError(errorMessage)
                    }
                }
                
            } catch (e: Exception) {
                val errorMessage = "Network Error: ${e.message}"
                Log.e(TAG, errorMessage, e)
                handler.post {
                    callback.onError(errorMessage)
                }
            }        }
    }
    
    fun fetchAllExchangeRates(callback: AllRatesCallback) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        
        executor.execute {
            try {
                val url = URL(API_URL)
                val connection = url.openConnection() as HttpURLConnection
                connection.requestMethod = "GET"
                connection.connectTimeout = TIMEOUT_MS
                connection.readTimeout = TIMEOUT_MS
                connection.setRequestProperty("Accept", "application/json")
                
                val responseCode = connection.responseCode
                Log.d(TAG, "All Rates Response Code: $responseCode")
                
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    val inputStream = connection.inputStream
                    val reader = BufferedReader(InputStreamReader(inputStream))
                    val response = StringBuilder()
                    var line: String?
                    
                    while (reader.readLine().also { line = it } != null) {
                        response.append(line)
                    }
                    
                    reader.close()
                    connection.disconnect()
                    
                    val jsonResponse = response.toString()
                    Log.d(TAG, "All Rates API Response: $jsonResponse")
                    
                    // Parse all rates from the JSON response
                    val allRates = parseAllRates(jsonResponse)
                    
                    handler.post {
                        callback.onSuccess(allRates)
                    }
                    
                } else {
                    val errorMessage = "HTTP Error: $responseCode"
                    Log.e(TAG, errorMessage)
                    handler.post {
                        callback.onError(errorMessage)
                    }
                }
                
            } catch (e: Exception) {
                val errorMessage = "Network Error: ${e.message}"
                Log.e(TAG, errorMessage, e)
                handler.post {
                    callback.onError(errorMessage)
                }
            }
        }
    }
    
    private fun parseAllRates(jsonResponse: String): Map<String, Double> {
        val rates = mutableMapOf<String, Double>()
        
        try {
            val jsonObject = JSONObject(jsonResponse)
            
            // Check for different API response structures
            when {
                jsonObject.has("rates") -> {
                    val ratesObject = jsonObject.getJSONObject("rates")
                    
                    // Extract rates for specific currencies
                    val currencies = listOf("USD", "EUR", "SGD", "MYR", "CNY", "THB", "JPY")
                    
                    for (currency in currencies) {
                        if (ratesObject.has(currency)) {
                            rates[currency] = ratesObject.getDouble(currency)
                        } else if (ratesObject.has("${currency}_MMK")) {
                            rates[currency] = ratesObject.getDouble("${currency}_MMK")
                        } else if (ratesObject.has("${currency.lowercase()}_mmk")) {
                            rates[currency] = ratesObject.getDouble("${currency.lowercase()}_mmk")
                        }
                    }
                }                jsonObject.has("data") -> {
                    val dataArray = jsonObject.getJSONArray("data")
                    Log.d(TAG, "Found data array with ${dataArray.length()} currencies")
                    
                    // Extract rates for each currency in the array
                    for (i in 0 until dataArray.length()) {
                        val currencyObj = dataArray.getJSONObject(i)
                        val currency = currencyObj.getString("currency")
                        
                        // Handle Japanese currency code fix (API uses "JPN" instead of "JPY")
                        val standardCurrency = if (currency == "JPN") "JPY" else currency
                        
                        // Use the average of buy and sell rates
                        val buyRate = currencyObj.getString("buy").toDouble()
                        val sellRate = currencyObj.getString("sell").toDouble()
                        val avgRate = (buyRate + sellRate) / 2.0
                        
                        rates[standardCurrency] = avgRate
                        Log.d(TAG, "Added $standardCurrency rate: $avgRate (buy: $buyRate, sell: $sellRate)")
                    }
                }
                else -> {
                    // Direct structure - look for currency codes
                    val currencies = listOf("USD", "EUR", "SGD", "MYR", "CNY", "THB", "JPY")
                    
                    for (currency in currencies) {
                        if (jsonObject.has(currency)) {
                            rates[currency] = jsonObject.getDouble(currency)
                        } else if (jsonObject.has("${currency}_MMK")) {
                            rates[currency] = jsonObject.getDouble("${currency}_MMK")
                        }
                    }
                }
            }
            
            // If no rates found, add default USD rate
            if (rates.isEmpty()) {
                Log.w(TAG, "No exchange rates found in API response, using default USD rate")
                rates["USD"] = CurrencyManager.DEFAULT_EXCHANGE_RATE
            }
            
            Log.d(TAG, "Parsed rates: $rates")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing all exchange rates: ${e.message}", e)
            // Return default rates on error
            rates["USD"] = CurrencyManager.DEFAULT_EXCHANGE_RATE
            rates["EUR"] = CurrencyManager.DEFAULT_EXCHANGE_RATE * 0.85 // Approximate EUR rate
            rates["SGD"] = CurrencyManager.DEFAULT_EXCHANGE_RATE * 0.74 // Approximate SGD rate
            rates["MYR"] = CurrencyManager.DEFAULT_EXCHANGE_RATE * 0.22 // Approximate MYR rate
            rates["CNY"] = CurrencyManager.DEFAULT_EXCHANGE_RATE * 0.14 // Approximate CNY rate
            rates["THB"] = CurrencyManager.DEFAULT_EXCHANGE_RATE * 0.10 // Approximate THB rate
            rates["JPY"] = CurrencyManager.DEFAULT_EXCHANGE_RATE * 2.4  // Approximate JPY rate
        }
        
        return rates
    }    private fun parseUsdToMmkRate(jsonObject: JSONObject): Double {
        return try {
            Log.d(TAG, "Parsing JSON for USD/MMK rate: $jsonObject")
            
            // Check for Myanmar Currency API structure: {"data": [{"currency": "USD", "buy": "4480", "sell": "4380"}, ...]}
            if (jsonObject.has("data")) {
                val dataArray = jsonObject.getJSONArray("data")
                Log.d(TAG, "Found data array with ${dataArray.length()} currencies")
                
                // Find USD currency in the array
                for (i in 0 until dataArray.length()) {
                    val currencyObj = dataArray.getJSONObject(i)
                    if (currencyObj.getString("currency") == "USD") {
                        // Use the average of buy and sell rates
                        val buyRate = currencyObj.getString("buy").toDouble()
                        val sellRate = currencyObj.getString("sell").toDouble()
                        val avgRate = (buyRate + sellRate) / 2.0
                        Log.d(TAG, "Found USD rate - Buy: $buyRate, Sell: $sellRate, Average: $avgRate")
                        return avgRate
                    }
                }
                
                Log.w(TAG, "USD not found in data array, using default rate")
                CurrencyManager.DEFAULT_EXCHANGE_RATE
            } else if (jsonObject.has("rates")) {
                // Fallback for other API structures
                val rates = jsonObject.getJSONObject("rates")
                Log.d(TAG, "Found rates object: $rates")
                
                if (rates.has("MMK")) {
                    val mmkRate = rates.getDouble("MMK")
                    Log.d(TAG, "Found MMK rate: $mmkRate")
                    mmkRate
                } else {
                    Log.w(TAG, "MMK rate not found in rates object, using default")
                    CurrencyManager.DEFAULT_EXCHANGE_RATE
                }
            } else {
                // Alternative structure - check for direct USD/MMK pair
                if (jsonObject.has("USD_MMK")) {
                    val rate = jsonObject.getDouble("USD_MMK")
                    Log.d(TAG, "Found USD_MMK rate: $rate")
                    rate
                } else if (jsonObject.has("usd_mmk")) {
                    val rate = jsonObject.getDouble("usd_mmk")
                    Log.d(TAG, "Found usd_mmk rate: $rate")
                    rate
                } else {
                    // If structure is different, try to find any MMK reference
                    findMmkRate(jsonObject)
                }
            }
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing exchange rate: ${e.message}", e)
            CurrencyManager.DEFAULT_EXCHANGE_RATE
        }
    }
    
    private fun findMmkRate(jsonObject: JSONObject): Double {
        // Recursively search for MMK rate in the JSON structure
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.get(key)
            
            if (key.contains("MMK", ignoreCase = true) && value is Number) {
                return value.toDouble()
            }
            
            if (value is JSONObject) {
                val nestedRate = findMmkRateInObject(value)
                if (nestedRate > 0) return nestedRate
            }
        }
        
        Log.w(TAG, "Could not find MMK rate in API response structure")
        return CurrencyManager.DEFAULT_EXCHANGE_RATE
    }
    
    private fun findMmkRateInObject(jsonObject: JSONObject): Double {
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.get(key)
            
            if (key.contains("MMK", ignoreCase = true) && value is Number) {
                return value.toDouble()
            }
        }
        return 0.0
    }
    
    fun updateCurrencyManagerWithLatestRate(callback: CurrencyRateCallback? = null) {
        fetchLatestExchangeRate(object : CurrencyRateCallback {
            override fun onSuccess(usdToMmk: Double) {
                // Update the CurrencyManager with the latest rate
                val currencyManager = CurrencyManager.getInstance(context)
                currencyManager.setExchangeRate(usdToMmk)
                
                Log.i(TAG, "Updated exchange rate to: $usdToMmk MMK per USD")
                callback?.onSuccess(usdToMmk)
            }
            
            override fun onError(error: String) {
                Log.w(TAG, "Failed to update exchange rate: $error")
                callback?.onError(error)
            }
        })
    }
}
