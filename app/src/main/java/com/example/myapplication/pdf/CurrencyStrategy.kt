package com.example.myapplication.pdf

interface CurrencyStrategy {
    fun convertAmount(amount: Double): Double
    fun formatAmount(amount: Double): String
    fun getCurrencyCode(): String
    fun getCurrencySymbol(): String
}

class USDCurrencyStrategy : CurrencyStrategy {
    override fun convertAmount(amount: Double): Double = amount // Base currency
    override fun formatAmount(amount: Double): String = "$${String.format("%.2f", amount)}"
    override fun getCurrencyCode(): String = "USD"
    override fun getCurrencySymbol(): String = "$"
}

class MMKCurrencyStrategy : CurrencyStrategy {
    private val exchangeRate = 2100.0 // 1 USD = 2100 MMK (approximate)
    
    override fun convertAmount(amount: Double): Double = amount * exchangeRate
    override fun formatAmount(amount: Double): String = "${String.format("%.0f", amount)} MMK"
    override fun getCurrencyCode(): String = "MMK"
    override fun getCurrencySymbol(): String = "MMK"
}

class JPYCurrencyStrategy : CurrencyStrategy {
    private val exchangeRate = 150.0 // 1 USD = 150 JPY (approximate)
    
    override fun convertAmount(amount: Double): Double = amount * exchangeRate
    override fun formatAmount(amount: Double): String = "¥${String.format("%.0f", amount)}"
    override fun getCurrencyCode(): String = "JPY"
    override fun getCurrencySymbol(): String = "¥"
}

class CNYCurrencyStrategy : CurrencyStrategy {
    private val exchangeRate = 7.3 // 1 USD = 7.3 CNY (approximate)
    
    override fun convertAmount(amount: Double): Double = amount * exchangeRate
    override fun formatAmount(amount: Double): String = "¥${String.format("%.2f", amount)}"
    override fun getCurrencyCode(): String = "CNY"
    override fun getCurrencySymbol(): String = "¥"
}

class THBCurrencyStrategy : CurrencyStrategy {
    private val exchangeRate = 36.0 // 1 USD = 36 THB (approximate)
    
    override fun convertAmount(amount: Double): Double = amount * exchangeRate
    override fun formatAmount(amount: Double): String = "฿${String.format("%.2f", amount)}"
    override fun getCurrencyCode(): String = "THB"
    override fun getCurrencySymbol(): String = "฿"
}

object CurrencyStrategyFactory {
    fun createStrategy(currencyCode: String): CurrencyStrategy {
        return when (currencyCode.uppercase()) {
            "USD" -> USDCurrencyStrategy()
            "MMK" -> MMKCurrencyStrategy()
            "JPY" -> JPYCurrencyStrategy()
            "CNY" -> CNYCurrencyStrategy()
            "THB" -> THBCurrencyStrategy()
            else -> USDCurrencyStrategy() // Default to USD
        }
    }
}