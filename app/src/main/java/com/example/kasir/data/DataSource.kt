package com.example.kasir.data

import com.example.kasir.model.Product

object DataSource {
    private val _products = mutableListOf(
        Product(
            id = "1",
            name = "Nasi Goreng Spesial",
            price = 25000.0,
            category = "Makanan",
            description = "Nasi goreng dengan telur, ayam, dan sayuran"
        ),
        Product(
            id = "2",
            name = "Mie Ayam Bakso",
            price = 20000.0,
            category = "Makanan",
            description = "Mie ayam dengan bakso sapi"
        ),
        Product(
            id = "3",
            name = "Ayam Bakar Madu",
            price = 35000.0,
            category = "Makanan",
            description = "Ayam bakar dengan saus madu"
        ),
        Product(
            id = "4",
            name = "Es Teh Manis",
            price = 5000.0,
            category = "Minuman",
            description = "Es teh manis segar"
        ),
        Product(
            id = "5",
            name = "Jus Jeruk",
            price = 12000.0,
            category = "Minuman",
            description = "Jus jeruk segar"
        ),
        Product(
            id = "6",
            name = "Kopi Susu",
            price = 15000.0,
            category = "Minuman",
            description = "Kopi susu gula aren"
        ),
        Product(
            id = "7",
            name = "Kentang Goreng",
            price = 15000.0,
            category = "Snack",
            description = "Kentang goreng renyah"
        ),
        Product(
            id = "8",
            name = "Roti Bakar",
            price = 18000.0,
            category = "Snack",
            description = "Roti bakar dengan selai dan coklat"
        ),
        Product(
            id = "9",
            name = "Sate Ayam",
            price = 30000.0,
            category = "Makanan",
            description = "Sate ayam dengan bumbu kacang"
        ),
        Product(
            id = "10",
            name = "Es Campur",
            price = 20000.0,
            category = "Minuman",
            description = "Es campur dengan buah segar"
        )
    )

    val products: List<Product> get() = _products

    fun getProductById(id: String): Product? {
        return _products.find { it.id == id }
    }

    fun searchProducts(query: String): List<Product> {
        return _products.filter { 
            it.name.contains(query, ignoreCase = true) || 
            it.category.contains(query, ignoreCase = true)
        }
    }

    fun getProductsByCategory(category: String): List<Product> {
        return _products.filter { it.category == category }
    }

    fun getCategories(): List<String> {
        return _products.map { it.category }.distinct()
    }

    fun addProduct(product: Product) {
        _products.add(product)
    }

    fun updateProduct(product: Product) {
        val index = _products.indexOfFirst { it.id == product.id }
        if (index != -1) {
            _products[index] = product
        }
    }

    fun deleteProduct(id: String) {
        _products.removeIf { it.id == id }
    }

    fun generateId(): String {
        return (System.currentTimeMillis()).toString()
    }
}
