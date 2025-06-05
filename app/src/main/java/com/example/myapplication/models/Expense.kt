package com.example.myapplication.models

data class Expense(
    val id: Long,
    val name: String,
    val amount: Double,
    val description: String,
    val date: Long // timestamp in milliseconds
)
