package com.example.myapplication.pdf

import android.content.Context
import com.example.myapplication.LanguageManager

interface LanguageStrategy {
    fun getLanguageCode(): String
    fun getString(context: Context, key: String): String
}

class EnglishLanguageStrategy : LanguageStrategy {
    override fun getLanguageCode(): String = "en"
    override fun getString(context: Context, key: String): String {
        return LanguageManager.getInstance(context).getStringForLanguage(key, "en")
    }
}

class MyanmarLanguageStrategy : LanguageStrategy {
    override fun getLanguageCode(): String = "mm"
    override fun getString(context: Context, key: String): String {
        return LanguageManager.getInstance(context).getStringForLanguage(key, "mm")
    }
}

class JapaneseLanguageStrategy : LanguageStrategy {
    override fun getLanguageCode(): String = "ja"
    override fun getString(context: Context, key: String): String {
        return LanguageManager.getInstance(context).getStringForLanguage(key, "ja")
    }
}

class ChineseLanguageStrategy : LanguageStrategy {
    override fun getLanguageCode(): String = "zh"
    override fun getString(context: Context, key: String): String {
        return LanguageManager.getInstance(context).getStringForLanguage(key, "zh")
    }
}

class ThaiLanguageStrategy : LanguageStrategy {
    override fun getLanguageCode(): String = "th"
    override fun getString(context: Context, key: String): String {
        return LanguageManager.getInstance(context).getStringForLanguage(key, "th")
    }
}

object LanguageStrategyFactory {
    fun createStrategy(languageCode: String): LanguageStrategy {
        return when (languageCode.lowercase()) {
            "en" -> EnglishLanguageStrategy()
            "mm" -> MyanmarLanguageStrategy()
            "ja" -> JapaneseLanguageStrategy()
            "zh" -> ChineseLanguageStrategy()
            "th" -> ThaiLanguageStrategy()
            else -> EnglishLanguageStrategy() // Default to English
        }
    }
}