package com.example.kasir.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun OpenBalanceDialog(
    isCloseBalance: Boolean = false,
    onDismiss: () -> Unit,
    onConfirm: (Double) -> Unit,
    modifier: Modifier = Modifier
) {
    var balanceInput by remember { mutableStateOf("") }
    
    val titleText = if (isCloseBalance) "Set Saldo Akhir" else "Set Saldo Awal"
    val descriptionText = if (isCloseBalance) "Masukkan saldo akhir untuk hari ini" else "Masukkan saldo awal untuk hari ini"
    val labelText = if (isCloseBalance) "Saldo Akhir" else "Saldo Awal"
    val buttonText = if (isCloseBalance) "Tutup" else "Mulai"
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = titleText,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    text = descriptionText,
                    style = MaterialTheme.typography.bodyMedium
                )
                OutlinedTextField(
                    value = balanceInput,
                    onValueChange = { balanceInput = it },
                    label = { Text(labelText) },
                    placeholder = { Text("0") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val balance = balanceInput.toDoubleOrNull() ?: 0.0
                    onConfirm(balance)
                },
                enabled = balanceInput.isNotEmpty()
            ) {
                Text(buttonText)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
