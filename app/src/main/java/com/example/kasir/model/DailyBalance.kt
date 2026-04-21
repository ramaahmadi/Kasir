package com.example.kasir.model

import java.util.Date

data class DailyBalance(
    val date: Date,
    val openingBalance: Double,
    val closingBalance: Double = 0.0,
    val totalSales: Double = 0.0,
    val transactionCount: Int = 0
)
