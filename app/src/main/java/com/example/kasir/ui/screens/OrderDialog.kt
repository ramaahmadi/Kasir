package com.example.kasir.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.kasir.model.Order
import com.example.kasir.model.OrderStatus

@Composable
fun OrderDialog(
    openOrders: List<Order>,
    onDismiss: () -> Unit,
    onLoadOrder: (Order) -> Unit,
    onCloseOrder: (Order) -> Unit,
    onSaveCurrentOrder: (customerName: String, tableName: String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showSaveDialog by remember { mutableStateOf(false) }
    var customerName by remember { mutableStateOf("") }
    var tableName by remember { mutableStateOf("") }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(onClick = onDismiss) {
                Text("Tutup")
            }
        },
        title = {
            Text(
                text = "Kelola Pesanan",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Button(
                    onClick = { showSaveDialog = true },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Simpan Pesanan Saat Ini")
                }
                
                if (openOrders.isEmpty()) {
                    Text(
                        text = "Tidak ada pesanan terbuka",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.secondary
                    )
                } else {
                    openOrders.forEach { order ->
                        Card(
                            modifier = Modifier.fillMaxWidth(),
                            elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
                        ) {
                            Column(
                                modifier = Modifier.padding(12.dp)
                            ) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween
                                ) {
                                    Text(
                                        text = "ID: ${order.id}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                    Text(
                                        text = when (order.status) {
                                            OrderStatus.OPEN -> "Terbuka"
                                            OrderStatus.CLOSED -> "Ditutup"
                                            OrderStatus.CANCELLED -> "Dibatalkan"
                                        },
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (order.customerName != null) {
                                    Text(
                                        text = "Pelanggan: ${order.customerName}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                if (order.tableName != null) {
                                    Text(
                                        text = "Meja: ${order.tableName}",
                                        style = MaterialTheme.typography.bodySmall
                                    )
                                }
                                Text(
                                    text = "Total: Rp ${String.format("%,.0f", order.totalAmount)}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    if (order.status == OrderStatus.OPEN) {
                                        Button(
                                            onClick = { onLoadOrder(order) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Buka")
                                        }
                                        Button(
                                            onClick = { onCloseOrder(order) },
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            Text("Tutup")
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    )
    
    if (showSaveDialog) {
        AlertDialog(
            onDismissRequest = { showSaveDialog = false },
            title = {
                Text("Simpan Pesanan")
            },
            text = {
                Column(
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    OutlinedTextField(
                        value = customerName,
                        onValueChange = { customerName = it },
                        label = { Text("Nama Pelanggan (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = tableName,
                        onValueChange = { tableName = it },
                        label = { Text("Nomor Meja (opsional)") },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSaveCurrentOrder(customerName, tableName)
                        showSaveDialog = false
                        customerName = ""
                        tableName = ""
                    }
                ) {
                    Text("Simpan")
                }
            },
            dismissButton = {
                TextButton(onClick = { showSaveDialog = false }) {
                    Text("Batal")
                }
            }
        )
    }
}
