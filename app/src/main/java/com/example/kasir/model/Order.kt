package com.example.kasir.model

import java.util.Date

data class Order(
    val id: String,
    val items: List<CartItem>,
    val totalAmount: Double,
    val customerName: String? = null,
    val tableName: String? = null,
    val status: OrderStatus = OrderStatus.OPEN,
    val createdAt: Date = Date(),
    val closedAt: Date? = null
)

enum class OrderStatus {
    OPEN,
    CLOSED,
    CANCELLED
}
