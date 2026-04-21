package com.example.kasir.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun PaperSizeDialog(
    currentSize: String,
    onDismiss: () -> Unit,
    onPaperSizeSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    val paperSizes = listOf(
        "58mm" to "58mm (Thermal Printer)",
        "80mm" to "80mm (Thermal Printer)",
        "A6" to "A6 (105mm x 148mm)"
    )
    
    var selectedSize by remember { mutableStateOf(currentSize) }
    
    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text("Pilih Ukuran Kertas")
        },
        text = {
            Column {
                paperSizes.forEach { (size, label) ->
                    Row {
                        RadioButton(
                            selected = selectedSize == size,
                            onClick = { selectedSize = size }
                        )
                        Text(
                            text = label,
                            modifier = Modifier.padding(start = 8.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    onPaperSizeSelected(selectedSize)
                    onDismiss()
                }
            ) {
                Text("Simpan")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Batal")
            }
        }
    )
}
