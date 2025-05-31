package com.example.myapplication

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.BufferedReader
import java.io.InputStreamReader

class LanguageManager private constructor(private val context: Context) {
    
    companion object {
        const val LANGUAGE_PREFS = "language_prefs"
        const val LANGUAGE_KEY = "selected_language"
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_MYANMAR = "mm"
        
        @Volatile
        private var INSTANCE: LanguageManager? = null
        
        fun getInstance(context: Context): LanguageManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: LanguageManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
    
    private var currentLanguageStrings: Map<String, String> = loadLanguageStrings(getCurrentLanguage())
    
    fun getCurrentLanguage(): String {
        return sharedPreferences.getString(LANGUAGE_KEY, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }
    
    fun setLanguage(languageCode: String) {
        sharedPreferences.edit().putString(LANGUAGE_KEY, languageCode).apply()
        currentLanguageStrings = loadLanguageStrings(languageCode)
    }
    
    fun getString(key: String): String {
        return currentLanguageStrings[key] ?: key
    }
    
    fun getString(key: String, defaultValue: String): String {
        return currentLanguageStrings[key] ?: defaultValue
    }
    
    private fun loadLanguageStrings(languageCode: String): Map<String, String> {
        return try {
            val fileName = "lang/strings_$languageCode.json"
            val inputStream = context.assets.open(fileName)
            val bufferedReader = BufferedReader(InputStreamReader(inputStream))
            val jsonString = bufferedReader.use { it.readText() }
            
            val gson = Gson()
            val type = object : TypeToken<Map<String, String>>() {}.type
            gson.fromJson(jsonString, type) ?: emptyMap()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to empty map if file loading fails
            emptyMap()
        }
    }
    
    fun isEnglish(): Boolean = getCurrentLanguage() == LANGUAGE_ENGLISH
    fun isMyanmar(): Boolean = getCurrentLanguage() == LANGUAGE_MYANMAR
    
    // Helper methods for common strings
    fun getAppName(): String = getString("app_name")
    fun getSettings(): String = getString("settings")
    fun getLanguageSettings(): String = getString("language_settings")
    fun getThemeSettings(): String = getString("theme_settings")
    fun getCurrencySettings(): String = getString("currency_settings")
    fun getFeedback(): String = getString("feedback")
    fun getAbout(): String = getString("about")
    fun getSave(): String = getString("save")
    fun getCancel(): String = getString("cancel")
    fun getApply(): String = getString("apply")
    
    // Refresh strings after language change
    fun refreshStrings() {
        currentLanguageStrings = loadLanguageStrings(getCurrentLanguage())
    }
}
