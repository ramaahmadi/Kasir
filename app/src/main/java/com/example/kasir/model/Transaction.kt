package com.example.kasir.model

import java.util.Date

data class Transaction(
    val id: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val paymentMethod: String,
    val date: Date = Date(),
    val cashierName: String = "Kasir",
    val openBalance: Double = 0.0
)
