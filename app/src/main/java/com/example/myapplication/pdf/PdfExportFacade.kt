package com.example.myapplication.pdf

import android.content.Context
import android.content.Intent
import android.graphics.pdf.PdfDocument
import android.net.Uri
import android.os.Environment
import androidx.core.content.FileProvider
import com.example.myapplication.models.Expense
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

class PdfExportFacade(private val context: Context) {
    
    companion object {
        private val SUPPORTED_PERIODS = listOf("today", "weekly", "monthly", "yearly")
        private val SUPPORTED_CURRENCIES = listOf("USD", "MMK", "JPY", "CNY", "THB")
        private val SUPPORTED_LANGUAGES = listOf("en", "mm", "ja", "zh", "th")
    }
      fun exportPdf(
        period: String,
        expenses: List<Expense>,
        targetCurrency: String = "USD",
        targetLanguage: String = "en",
        customAppTitle: String = ""
    ): Boolean {
        try {
            // Create strategies
            val periodStrategy = PeriodStrategyFactory.createStrategy(period)
            val currencyStrategy = CurrencyStrategyFactory.createStrategy(targetCurrency)
            val languageStrategy = LanguageStrategyFactory.createStrategy(targetLanguage)
            
            // Filter expenses based on period
            val filteredExpenses = periodStrategy.filterExpenses(expenses)
            
            if (filteredExpenses.isEmpty()) {
                return false
            }
            
            // Calculate total
            val totalAmount = filteredExpenses.sumOf { it.amount }            // Determine app title (use custom if provided, otherwise default HSU app title)
            val appTitle = if (customAppTitle.isNotEmpty()) {
                customAppTitle
            } else {
                "HSU Expense App" // Default title if no custom title provided
            }
            
            // Generate thank you messages using templates
            val thankYouMessage = languageStrategy.getString(context, "pdf_thank_you")
                .replace("{appTitle}", appTitle)
            
            val thankYouAppName = languageStrategy.getString(context, "pdf_thank_you_app")
                .replace("{appTitle}", appTitle)
            
            // Generate PDF using Builder pattern
            val document = PdfReportBuilder(context)
                .initializeDocument()
                .createNewPage()
                .addHeader(
                    title = appTitle,
                    subtitle = languageStrategy.getString(context, "pdf_period_${period}")
                )
                .addDivider()
                .addTableHeader(
                    nameHeader = languageStrategy.getString(context, "pdf_item_header"),
                    amountHeader = languageStrategy.getString(context, "pdf_amount_header")
                )
                .addExpenseItems(filteredExpenses, currencyStrategy, languageStrategy)
                .addTotal(
                    totalLabel = languageStrategy.getString(context, "pdf_total"),
                    totalAmount = totalAmount,
                    currencyStrategy = currencyStrategy
                )                .addFooter(
                    thankYouMessage = thankYouMessage,
                    appName = thankYouAppName,
                    generatedTime = languageStrategy.getString(context, "pdf_created") + 
                                   " " + SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date())
                )
                .build()
            
            // Save and share PDF
            return savePdfAndShare(document, period, targetLanguage)
            
        } catch (e: Exception) {
            e.printStackTrace()
            return false
        }
    }
    
    private fun savePdfAndShare(document: PdfDocument, period: String, language: String): Boolean {
        return try {
            // Create filename with timestamp
            val timestamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val filename = "ExpenseReport_${period}_${language}_${timestamp}.pdf"
            
            // Save to Downloads directory
            val downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS)
            val file = File(downloadsDir, filename)
            
            FileOutputStream(file).use { outputStream ->
                document.writeTo(outputStream)
            }
            document.close()
            
            // Share PDF
            sharePdf(file)
            
            true
        } catch (e: Exception) {
            e.printStackTrace()
            document.close()
            false
        }
    }
    
    private fun sharePdf(file: File) {
        try {
            val uri: Uri = FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
            
            val shareIntent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            val chooserIntent = Intent.createChooser(shareIntent, "Share PDF Report")
            chooserIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            context.startActivity(chooserIntent)
            
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
    
    fun getAvailablePeriods(): List<String> = SUPPORTED_PERIODS
    
    fun getSupportedCurrencies(): List<String> = SUPPORTED_CURRENCIES
    
    fun getSupportedLanguages(): List<String> = SUPPORTED_LANGUAGES
    
    fun hasExpensesForPeriod(period: String, expenses: List<Expense>): Boolean {
        val periodStrategy = PeriodStrategyFactory.createStrategy(period)
        return periodStrategy.filterExpenses(expenses).isNotEmpty()
    }
}