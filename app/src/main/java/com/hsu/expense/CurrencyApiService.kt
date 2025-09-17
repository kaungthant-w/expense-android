package com.hsu.expense

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
        private const val TIMEOUT_MS = 10000
        private const val ALL_CURRENCIES_URL = "https://myanmarexchangerateapi.onrender.com/currency/all/latest"

        private fun getApiUrl(currency: String) = "https://myanmarexchangerateapi.onrender.com/currency/$currency/latest"
    }
      interface CurrencyRateCallback {
        fun onSuccess(usdToMmk: Double)
        fun onError(error: String)
    }
    
    interface AllRatesCallback {
        fun onSuccess(rates: Map<String, Double>)
        fun onError(error: String)
    }
    
    private fun parseRate(jsonResponse: String): Double {
        val jsonObject = JSONObject(jsonResponse)
        val data = jsonObject.getJSONObject("data")
        val buyRate = data.getDouble("buyRate")
        val sellRate = data.getDouble("sellRate")
        return (buyRate + sellRate) / 2.0
    }
    
    private fun parseAllCurrenciesResponse(jsonResponse: String): Map<String, Double> {
        val rates = mutableMapOf<String, Double>()
        
        try {
            val jsonObject = JSONObject(jsonResponse)
            val dataArray = jsonObject.getJSONArray("data")
            
            Log.d(TAG, "Found ${dataArray.length()} currencies in API response")
            
            for (i in 0 until dataArray.length()) {
                try {
                    val currencyObj = dataArray.getJSONObject(i)
                    val currencyCode = currencyObj.getString("currencyCode")
                    val buyRate = currencyObj.getDouble("buyRate")
                    val sellRate = currencyObj.getDouble("sellRate")
                    val avgRate = (buyRate + sellRate) / 2.0
                    
                    rates[currencyCode] = avgRate
                    Log.d(TAG, "Parsed $currencyCode: buy=$buyRate, sell=$sellRate, avg=$avgRate")
                } catch (e: Exception) {
                    Log.e(TAG, "Error parsing currency at index $i: ${e.message}")
                }
            }
            
            Log.d(TAG, "Successfully parsed ${rates.size} currency rates")
            
        } catch (e: Exception) {
            Log.e(TAG, "Error parsing all currencies response: ${e.message}", e)
            // Return empty map on error - let the calling code handle fallbacks
        }
        
        return rates
    }
    
    fun fetchLatestExchangeRate(callback: CurrencyRateCallback) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        
        executor.execute {
            try {
                val url = URL(getApiUrl("USD"))
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
                    val usdToMmkRate = parseRate(jsonResponse)
                    
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
    
    fun fetchAllExchangeRates(callback: AllRatesCallback) {
        val executor = Executors.newSingleThreadExecutor()
        val handler = Handler(Looper.getMainLooper())
        
        executor.execute {
            try {
                val url = URL(ALL_CURRENCIES_URL)
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
                    val allRates = parseAllCurrenciesResponse(jsonResponse)
                    
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
