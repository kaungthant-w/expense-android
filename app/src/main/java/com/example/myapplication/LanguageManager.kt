package com.example.myapplication

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.io.InputStreamReader

class LanguageManager private constructor(private val context: Context) {
      companion object {
        const val LANGUAGE_PREFS = "language_preferences"
        const val LANGUAGE_KEY = "selected_language"
        const val LANGUAGE_ENGLISH = "en"
        const val LANGUAGE_MYANMAR = "mm"
        const val LANGUAGE_CHINESE = "zh"
        const val LANGUAGE_JAPANESE = "ja"
        const val LANGUAGE_THAI = "th"
        const val LANGUAGE_CHANGED_ACTION = "com.example.myapplication.LANGUAGE_CHANGED"
        
        @Volatile
        private var INSTANCE: LanguageManager? = null
        
        fun getInstance(context: Context): LanguageManager {
            return INSTANCE ?: synchronized(LanguageManager::class.java) {
                INSTANCE ?: LanguageManager(context.applicationContext).also { INSTANCE = it }
            }
        }
    }
    
    private val sharedPreferences: SharedPreferences = 
        context.getSharedPreferences(LANGUAGE_PREFS, Context.MODE_PRIVATE)
    private val gson = Gson()
    private var currentStrings: Map<String, String> = mapOf()
    
    init {
        loadLanguageStrings()
    }
    
    fun getCurrentLanguage(): String {
        return sharedPreferences.getString(LANGUAGE_KEY, LANGUAGE_ENGLISH) ?: LANGUAGE_ENGLISH
    }    fun setLanguage(language: String) {
        if (language in listOf(LANGUAGE_ENGLISH, LANGUAGE_MYANMAR, LANGUAGE_CHINESE, LANGUAGE_JAPANESE, LANGUAGE_THAI)) {
            val oldLanguage = getCurrentLanguage()
            
            // Debug log
            android.util.Log.d("LanguageManager", "Setting language from $oldLanguage to $language")
            
            sharedPreferences.edit()
                .putString(LANGUAGE_KEY, language)
                .apply()
            loadLanguageStrings()
            
            // Only broadcast if language actually changed
            if (oldLanguage != language) {
                // Debug log
                android.util.Log.d("LanguageManager", "Broadcasting language change: $oldLanguage -> $language")
                
                // Broadcast language change to all activities
                val intent = Intent(LANGUAGE_CHANGED_ACTION)
                intent.putExtra("old_language", oldLanguage)
                intent.putExtra("new_language", language)
                try {
                    LocalBroadcastManager.getInstance(context).sendBroadcast(intent)
                    android.util.Log.d("LanguageManager", "Broadcast sent successfully")
                } catch (e: Exception) {
                    android.util.Log.e("LanguageManager", "Error sending broadcast: ${e.message}")
                }
            } else {
                android.util.Log.d("LanguageManager", "Language unchanged, no broadcast needed")
            }
        }
    }
    
    fun getString(key: String): String {
        return currentStrings[key] ?: key
    }
      fun getAvailableLanguages(): List<Pair<String, String>> {
        return listOf(
            Pair(LANGUAGE_ENGLISH, "English"),
            Pair(LANGUAGE_MYANMAR, "မြန်မာ"),
            Pair(LANGUAGE_CHINESE, "中文"),
            Pair(LANGUAGE_JAPANESE, "日本語"),
            Pair(LANGUAGE_THAI, "ไทย")
        )
    }
    
    private fun loadLanguageStrings() {
        val language = getCurrentLanguage()
        val fileName = "lang/strings_$language.json"
        
        try {
            val inputStream = context.assets.open(fileName)
            val reader = InputStreamReader(inputStream)
            val type = object : TypeToken<Map<String, String>>() {}.type
            currentStrings = gson.fromJson(reader, type) ?: mapOf()
            reader.close()
        } catch (e: Exception) {
            e.printStackTrace()
            // Fallback to English if loading fails
            if (language != LANGUAGE_ENGLISH) {
                try {
                    val inputStream = context.assets.open("lang/strings_en.json")
                    val reader = InputStreamReader(inputStream)
                    val type = object : TypeToken<Map<String, String>>() {}.type
                    currentStrings = gson.fromJson(reader, type) ?: mapOf()
                    reader.close()
                } catch (fallbackException: Exception) {
                    fallbackException.printStackTrace()
                    currentStrings = getDefaultStrings()
                }
            } else {
                currentStrings = getDefaultStrings()
            }
        }
    }
    
    private fun getDefaultStrings(): Map<String, String> {
        return mapOf(
            "app_name" to "HSU Expense",
            "settings" to "Settings",
            "language_settings" to "Language Settings",
            "theme_settings" to "Theme Settings",
            "export_data" to "Export Data",
            "import_data" to "Import Data",
            "about" to "About",
            "english" to "English",            "myanmar" to "မြန်မာ",
            "chinese" to "中文",
            "japanese" to "日本語",
            "thai" to "ไทย",
            "select_language" to "Select Language",
            "current_language" to "Current Language",
            "apply" to "Apply",
            "language_changed" to "Language changed successfully",
            "restart_required" to "Please restart the app for all changes to take effect",
            "back" to "Back",
            "cancel" to "Cancel",
            "ok" to "OK"
        )
    }
    
    fun isCurrentLanguage(language: String): Boolean {
        return getCurrentLanguage() == language
    }
}
