package com.example.kasir.ui.screens

import android.app.DatePickerDialog
import android.content.Context
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Receipt
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.text.font.FontWeight
import com.example.kasir.model.CartItem
import com.example.kasir.model.Transaction
import java.text.SimpleDateFormat
import java.util.*
import java.util.Calendar

data class ItemSales(
    val productName: String,
    val quantity: Int,
    val totalRevenue: Double
)

data class DailyItemSales(
    val date: Date,
    val itemSales: List<ItemSales>,
    val totalTransactions: Int,
    val totalRevenue: Double
)

@Composable
fun SalesReportScreen(
    transactions: List<Transaction>,
    onDismiss: () -> Unit = {},
    onDownloadAccountingPdf: (List<Transaction>, Date?, Date?) -> Unit = { _, _, _ -> },
    modifier: Modifier = Modifier
) {
    val context = LocalContext.current
    var customStartDate by remember { mutableStateOf<Date?>(null) }
    var customEndDate by remember { mutableStateOf<Date?>(null) }
    var filteredTransactions by remember { mutableStateOf<List<Transaction>>(emptyList()) }
    var itemSalesList by remember { mutableStateOf<List<ItemSales>>(emptyList()) }
    var dailyItemSalesList by remember { mutableStateOf<List<DailyItemSales>>(emptyList()) }
    var hasFiltered by remember { mutableStateOf(false) }
    var isPickingStartDate by remember { mutableStateOf(true) }

    val displayDateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))

    val calendar = Calendar.getInstance()

    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, dayOfMonth ->
            calendar.set(Calendar.YEAR, year)
            calendar.set(Calendar.MONTH, month)
            calendar.set(Calendar.DAY_OF_MONTH, dayOfMonth)
            calendar.set(Calendar.HOUR_OF_DAY, 0)
            calendar.set(Calendar.MINUTE, 0)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            val selectedDate = calendar.time

            if (isPickingStartDate) {
                customStartDate = selectedDate
            } else {
                customEndDate = selectedDate
            }
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    fun showDatePickerDialog(pickingStartDate: Boolean) {
        isPickingStartDate = pickingStartDate
        datePickerDialog.show()
    }

    fun filterTransactions() {
        if (customStartDate != null && customEndDate != null) {
            val filtered = transactions.filter { transaction ->
                transaction.date >= customStartDate!! && transaction.date <= customEndDate!!
            }
            filteredTransactions = filtered.toList()
            
            // Group transactions by date
            val dailySalesMap = mutableMapOf<Date, MutableList<Transaction>>()
            filtered.forEach { transaction ->
                val dateKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).parse(
                    SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(transaction.date)
                ) ?: transaction.date
                if (!dailySalesMap.containsKey(dateKey)) {
                    dailySalesMap[dateKey] = mutableListOf()
                }
                dailySalesMap[dateKey]!!.add(transaction)
            }
            
            // Calculate item sales for each day
            val dailySalesList = mutableListOf<DailyItemSales>()
            dailySalesMap.forEach { (date, dayTransactions) ->
                val itemSalesMap = mutableMapOf<String, ItemSales>()
                dayTransactions.forEach { transaction ->
                    transaction.items.forEach { item ->
                        val existing = itemSalesMap[item.product.name]
                        if (existing != null) {
                            itemSalesMap[item.product.name] = ItemSales(
                                productName = item.product.name,
                                quantity = existing.quantity + item.quantity,
                                totalRevenue = existing.totalRevenue + item.totalPrice
                            )
                        } else {
                            itemSalesMap[item.product.name] = ItemSales(
                                productName = item.product.name,
                                quantity = item.quantity,
                                totalRevenue = item.totalPrice
                            )
                        }
                    }
                }
                
                val dayTotalRevenue = dayTransactions.sumOf { it.totalAmount }
                dailySalesList.add(
                    DailyItemSales(
                        date = date,
                        itemSales = itemSalesMap.values.toList(),
                        totalTransactions = dayTransactions.size,
                        totalRevenue = dayTotalRevenue
                    )
                )
            }
            
            dailyItemSalesList = dailySalesList.sortedBy { it.date }
            
            // Also keep the flat item sales list for compatibility
            val flatItemSalesMap = mutableMapOf<String, ItemSales>()
            filtered.forEach { transaction ->
                transaction.items.forEach { item ->
                    val existing = flatItemSalesMap[item.product.name]
                    if (existing != null) {
                        flatItemSalesMap[item.product.name] = ItemSales(
                            productName = item.product.name,
                            quantity = existing.quantity + item.quantity,
                            totalRevenue = existing.totalRevenue + item.totalPrice
                        )
                    } else {
                        flatItemSalesMap[item.product.name] = ItemSales(
                            productName = item.product.name,
                            quantity = item.quantity,
                            totalRevenue = item.totalPrice
                        )
                    }
                }
            }
            itemSalesList = flatItemSalesMap.values.toList()
            hasFiltered = true
        }
    }
    
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = androidx.compose.ui.Alignment.CenterVertically
        ) {
            Text(
                text = "Laporan Penjualan per Item",
                style = MaterialTheme.typography.headlineMedium,
                fontWeight = FontWeight.Bold
            )
            IconButton(
                onClick = {
                    if (hasFiltered && filteredTransactions.isNotEmpty()) {
                        onDownloadAccountingPdf(filteredTransactions, customStartDate, customEndDate)
                    } else {
                        onDownloadAccountingPdf(transactions, customStartDate, customEndDate)
                    }
                },
                modifier = Modifier.size(32.dp)
            ) {
                Icon(
                    Icons.Default.Receipt,
                    contentDescription = "Download PDF",
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        }
        
        // Date Range Selection
        Card(
            modifier = Modifier.fillMaxWidth(),
            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = "Pilih Rentang Tanggal",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                
                customStartDate?.let {
                    Text(
                        text = "Tanggal Mulai: ${displayDateFormat.format(it)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } ?: Text(
                    text = "Tanggal Mulai: Belum dipilih",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                customEndDate?.let {
                    Text(
                        text = "Tanggal Akhir: ${displayDateFormat.format(it)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                } ?: Text(
                    text = "Tanggal Akhir: Belum dipilih",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.secondary
                )
                
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Button(
                        onClick = {
                            showDatePickerDialog(true)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pilih Tanggal Mulai")
                    }
                    Button(
                        onClick = {
                            showDatePickerDialog(false)
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Pilih Tanggal Akhir")
                    }
                }
                
                if (customStartDate != null && customEndDate != null) {
                    Button(
                        onClick = { filterTransactions() },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text("Tampilkan Laporan")
                    }
                }
            }
        }
        
        // Summary
        if (hasFiltered && filteredTransactions.isNotEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "Ringkasan",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    
                    Text(
                        text = "Total Transaksi: ${filteredTransactions.size}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                    
                    val totalRevenue = filteredTransactions.sumOf { it.totalAmount }
                    Text(
                        text = "Total Pendapatan: Rp${"%,.2f".format(totalRevenue)}",
                        style = MaterialTheme.typography.bodyMedium
                    )
                }
            }
        }
        
        // Show message when no transactions found
        if (hasFiltered && filteredTransactions.isEmpty()) {
            Card(
                modifier = Modifier.fillMaxWidth(),
                elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
            ) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally
                ) {
                    Text(
                        text = "Tidak ada transaksi pada rentang tanggal ini",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        }
        
        // Item Sales List grouped by date
        if (hasFiltered && dailyItemSalesList.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(dailyItemSalesList) { dailySales ->
                    Card(
                        modifier = Modifier.fillMaxWidth(),
                        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                    ) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            // Date header
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween
                            ) {
                                Text(
                                    text = displayDateFormat.format(dailySales.date),
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    text = "Rp${"%,.2f".format(dailySales.totalRevenue)}",
                                    style = MaterialTheme.typography.titleMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            
                            Text(
                                text = "${dailySales.totalTransactions} transaksi",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.secondary
                            )
                            
                            Divider()
                            
                            // Item sales for this day
                            dailySales.itemSales.forEach { itemSales ->
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = itemSales.productName,
                                        style = MaterialTheme.typography.bodyMedium
                                    )
                                    
                                    Column(
                                        horizontalAlignment = androidx.compose.ui.Alignment.End
                                    ) {
                                        Text(
                                            text = "x${itemSales.quantity}",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.secondary
                                        )
                                        Text(
                                            text = "Rp${"%,.2f".format(itemSales.totalRevenue)}",
                                            style = MaterialTheme.typography.bodyMedium,
                                            fontWeight = FontWeight.Bold
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
