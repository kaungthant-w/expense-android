package com.example.myapplication

data class ExpenseItem(
    val id: Long,
    var name: String,
    var price: Double,
    var description: String,
    var date: String,
    var time: String,
    var isDeleted: Boolean = false,
    var deletedAt: String? = null
)
