package com.example.kasir.model

data class Product(
    val id: String,
    val name: String,
    val price: Double,
    val category: String,
    val imageRes: Int? = null,
    val description: String = "",
    val stock: Int = 0
)
