package com.example.myapplication.pdf

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import com.example.myapplication.models.Expense
import java.text.SimpleDateFormat
import java.util.*

class PdfReportBuilder(private val context: Context) {
    private lateinit var document: PdfDocument
    private lateinit var page: PdfDocument.Page
    private lateinit var canvas: Canvas
    private lateinit var paint: Paint
    private var currentY = 50f
    private var pageWidth = 0
    private var pageHeight = 0
    
    // Page configuration for roll paper (thermal printer style)
    private val PAGE_WIDTH = 226 // 80mm roll paper width in points
    private val PAGE_HEIGHT = 842 // A4 height, will expand as needed
    
    fun initializeDocument(): PdfReportBuilder {
        document = PdfDocument()
        paint = Paint().apply {
            color = Color.BLACK
            textSize = 12f
            isAntiAlias = true
        }
        return this
    }
    
    fun createNewPage(): PdfReportBuilder {
        val pageInfo = PdfDocument.PageInfo.Builder(PAGE_WIDTH, PAGE_HEIGHT, 1).create()
        page = document.startPage(pageInfo)
        canvas = page.canvas
        pageWidth = pageInfo.pageWidth
        pageHeight = pageInfo.pageHeight
        currentY = 30f
        return this
    }
    
    fun addHeader(title: String, subtitle: String): PdfReportBuilder {
        // App title
        paint.apply {
            textSize = 16f
            isFakeBoldText = true
            textAlign = Paint.Align.CENTER
        }
        canvas.drawText(title, (pageWidth / 2).toFloat(), currentY, paint)
        currentY += 25f
        
        // Subtitle (period)
        paint.apply {
            textSize = 12f
            isFakeBoldText = false
        }
        canvas.drawText(subtitle, (pageWidth / 2).toFloat(), currentY, paint)
        currentY += 20f
        
        return this
    }
    
    fun addDivider(): PdfReportBuilder {
        canvas.drawLine(10f, currentY, (pageWidth - 10).toFloat(), currentY, paint)
        currentY += 15f
        return this
    }
    
    fun addTableHeader(nameHeader: String, amountHeader: String): PdfReportBuilder {
        paint.apply {
            textSize = 10f
            isFakeBoldText = true
            textAlign = Paint.Align.LEFT
        }
        
        canvas.drawText(nameHeader, 10f, currentY, paint)
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(amountHeader, (pageWidth - 10).toFloat(), currentY, paint)
        currentY += 15f
        
        addDivider()
        return this
    }
    
    fun addExpenseItems(
        expenses: List<Expense>,
        currencyStrategy: CurrencyStrategy,
        languageStrategy: LanguageStrategy
    ): PdfReportBuilder {
        paint.apply {
            textSize = 9f
            isFakeBoldText = false
        }
        
        val dateFormat = SimpleDateFormat("MM/dd HH:mm", Locale.getDefault())
        
        expenses.forEach { expense ->
            val convertedAmount = currencyStrategy.convertAmount(expense.amount)
            val formattedAmount = currencyStrategy.formatAmount(convertedAmount)
            
            // Item name and description
            paint.textAlign = Paint.Align.LEFT
            canvas.drawText(expense.name, 10f, currentY, paint)
            currentY += 12f
            
            if (expense.description.isNotEmpty()) {
                paint.apply {
                    textSize = 8f
                    color = Color.GRAY
                }
                canvas.drawText(expense.description, 15f, currentY, paint)
                currentY += 10f
                paint.apply {
                    textSize = 9f
                    color = Color.BLACK
                }
            }
            
            // Date and amount on same line
            val dateStr = dateFormat.format(Date(expense.date))
            canvas.drawText(dateStr, 10f, currentY, paint)
            
            paint.textAlign = Paint.Align.RIGHT
            canvas.drawText(formattedAmount, (pageWidth - 10).toFloat(), currentY, paint)
            currentY += 15f
        }
        
        return this
    }
    
    fun addTotal(
        totalLabel: String,
        totalAmount: Double,
        currencyStrategy: CurrencyStrategy
    ): PdfReportBuilder {
        addDivider()
        
        val convertedTotal = currencyStrategy.convertAmount(totalAmount)
        val formattedTotal = currencyStrategy.formatAmount(convertedTotal)
        
        paint.apply {
            textSize = 12f
            isFakeBoldText = true
            textAlign = Paint.Align.LEFT
        }
        canvas.drawText(totalLabel, 10f, currentY, paint)
        
        paint.textAlign = Paint.Align.RIGHT
        canvas.drawText(formattedTotal, (pageWidth - 10).toFloat(), currentY, paint)
        currentY += 20f
        
        return this
    }
    
    fun addFooter(
        thankYouMessage: String,
        appName: String,
        generatedTime: String
    ): PdfReportBuilder {
        addDivider()
        
        paint.apply {
            textSize = 10f
            isFakeBoldText = false
            textAlign = Paint.Align.CENTER
        }
        
        canvas.drawText(thankYouMessage, (pageWidth / 2).toFloat(), currentY, paint)
        currentY += 15f
        
        canvas.drawText(appName, (pageWidth / 2).toFloat(), currentY, paint)
        currentY += 15f
        
        paint.apply {
            textSize = 8f
            color = Color.GRAY
        }
        canvas.drawText(generatedTime, (pageWidth / 2).toFloat(), currentY, paint)
        
        return this
    }
    
    fun build(): PdfDocument {
        document.finishPage(page)
        return document
    }
}