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
            }
        }
    }
    
    private fun parseUsdToMmkRate(jsonObject: JSONObject): Double {
        return try {
            // Check if the API has USD as base and MMK as target
            if (jsonObject.has("rates")) {
                val rates = jsonObject.getJSONObject("rates")
                
                // If MMK is directly available
                if (rates.has("MMK")) {
                    rates.getDouble("MMK")
                } else {
                    // Fallback to default rate if not found
                    Log.w(TAG, "MMK rate not found in API response, using default")
                    CurrencyManager.DEFAULT_EXCHANGE_RATE
                }
            } else {
                // Alternative structure - check for direct USD/MMK pair
                if (jsonObject.has("USD_MMK")) {
                    jsonObject.getDouble("USD_MMK")
                } else if (jsonObject.has("usd_mmk")) {
                    jsonObject.getDouble("usd_mmk")
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
