package com.example.kasir.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PinLockDialog(
    correctPin: String,
    onPinCorrect: () -> Unit,
    modifier: Modifier = Modifier
) {
    var enteredPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf(false) }
    
    AlertDialog(
        onDismissRequest = { },
        title = {
            Text(
                text = "Masukkan PIN",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )
        },
        text = {
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedTextField(
                    value = enteredPin,
                    onValueChange = { 
                        if (it.length <= 4) {
                            enteredPin = it
                            error = false
                        }
                    },
                    label = { Text("PIN (4 digit)") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    isError = error
                )
                if (error) {
                    Text(
                        text = "PIN salah",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    if (enteredPin == correctPin) {
                        onPinCorrect()
                    } else {
                        error = true
                    }
                },
                enabled = enteredPin.length == 4
            ) {
                Text("Masuk")
            }
        }
    )
}
