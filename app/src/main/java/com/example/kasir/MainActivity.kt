package com.example.kasir

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import android.print.PrintAttributes
import android.print.PrintDocumentAdapter
import android.print.PrintDocumentInfo
import android.print.PrintManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import kotlinx.coroutines.launch
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.tooling.preview.PreviewScreenSizes
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.kasir.data.BalanceDataSource
import com.example.kasir.data.DataSource
import com.example.kasir.data.TransactionDataSource
import com.example.kasir.model.CartItem
import com.example.kasir.model.Order
import com.example.kasir.model.OrderStatus
import com.example.kasir.model.Product
import com.example.kasir.model.Transaction
import com.example.kasir.ui.screens.CartScreen
import com.example.kasir.ui.screens.CheckoutScreen
import com.example.kasir.ui.screens.CloseBalanceDialog
import com.example.kasir.ui.screens.FavoritesScreen
import com.example.kasir.ui.screens.HomeScreen
import com.example.kasir.ui.screens.OpenBalanceDialog
import com.example.kasir.ui.screens.OrderDialog
import com.example.kasir.ui.screens.PaperSizeDialog
import com.example.kasir.ui.screens.ProductFormDialog
import com.example.kasir.ui.screens.ProductManagementScreen
import com.example.kasir.ui.screens.ReceiptScreen
import com.example.kasir.ui.screens.SalesReportScreen
import com.example.kasir.ui.screens.SettingsScreen
import com.example.kasir.ui.screens.TransactionHistoryScreen
import com.example.kasir.ui.theme.KasirTheme
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import java.util.UUID

class ReceiptPrintAdapter(
    private val context: Context,
    private val receiptText: String
) : android.print.PrintDocumentAdapter() {
    override fun onLayout(
        oldAttributes: android.print.PrintAttributes?,
        newAttributes: android.print.PrintAttributes,
        cancellationSignal: android.os.CancellationSignal?,
        callback: android.print.PrintDocumentAdapter.LayoutResultCallback,
        extras: android.os.Bundle?
    ) {
        if (cancellationSignal?.isCanceled == true) {
            callback.onLayoutCancelled()
            return
        }

        val builder = android.print.PrintDocumentInfo.Builder("struk.txt")
        builder.setContentType(android.print.PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
        builder.setPageCount(1)

        callback.onLayoutFinished(builder.build(), oldAttributes != newAttributes)
    }

    override fun onWrite(
        pages: Array<android.print.PageRange>,
        destination: android.os.ParcelFileDescriptor,
        cancellationSignal: android.os.CancellationSignal?,
        callback: android.print.PrintDocumentAdapter.WriteResultCallback
    ) {
        try {
            val fileDescriptor = destination.fileDescriptor
            FileOutputStream(fileDescriptor).use { it.write(receiptText.toByteArray()) }
            callback.onWriteFinished(arrayOf(android.print.PageRange(0, 0)))
        } catch (e: Exception) {
            callback.onWriteFailed(e.toString())
        }
    }
}

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            KasirTheme {
                KasirApp(
                    context = this,
                    onPrintReceipt = { transaction -> printReceipt(transaction) },
                    onShareReceipt = { transaction -> shareReceipt(transaction) },
                    onDownloadAccountingPdf = { transactions, startDate, endDate -> downloadAccountingPdf(transactions, startDate, endDate) },
                    onExit = { finish() },
                    onGetTransactions = { emptyList() }
                )
            }
        }
    }

    private fun printReceipt(transaction: Transaction) {
        val printManager = getSystemService(Context.PRINT_SERVICE) as PrintManager
        val jobName = "Struk_${transaction.id}"
        
        val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val storeName = sharedPreferences.getString("store_name", "") ?: ""
        val storeLocation = sharedPreferences.getString("store_location", "") ?: ""
        
        val printAdapter = object : PrintDocumentAdapter() {
            override fun onLayout(
                oldAttributes: PrintAttributes?,
                newAttributes: PrintAttributes,
                cancellationSignal: android.os.CancellationSignal?,
                callback: LayoutResultCallback,
                extras: Bundle?
            ) {
                if (cancellationSignal?.isCanceled == true) {
                    callback.onLayoutCancelled()
                    return
                }
                
                val builder = PrintDocumentInfo.Builder(jobName)
                    .setContentType(PrintDocumentInfo.CONTENT_TYPE_DOCUMENT)
                    .setPageCount(1)
                
                callback.onLayoutFinished(builder.build(), true)
            }
            
            override fun onWrite(
                pages: Array<android.print.PageRange>,
                destination: android.os.ParcelFileDescriptor,
                cancellationSignal: android.os.CancellationSignal?,
                callback: WriteResultCallback
            ) {
                try {
                    val pdfDocument = PdfDocument()
                    val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
                    val page = pdfDocument.startPage(pageInfo)
                    val canvas = page.canvas
                    val paint = Paint()
                    
                    // Set text color and center alignment
                    paint.color = Color.BLACK
                    paint.textAlign = Paint.Align.CENTER
                    
                    var yPosition = 30f
                    
                    // Draw store name
                    if (storeName.isNotEmpty()) {
                        paint.textSize = 18f
                        canvas.drawText(storeName, 150f, yPosition, paint)
                        yPosition += 20f
                    }
                    
                    // Draw store location
                    if (storeLocation.isNotEmpty()) {
                        paint.textSize = 12f
                        canvas.drawText(storeLocation, 150f, yPosition, paint)
                        yPosition += 20f
                    }
                    
                    // Draw separator
                    paint.textSize = 12f
                        canvas.drawText("================================", 150f, yPosition, paint)
                    yPosition += 20f
                    
                    // Draw title
                    paint.textSize = 20f
                    canvas.drawText("STRUK PEMBELIAN", 150f, yPosition, paint)
                    yPosition += 30f
                    
                    // Draw separator
                    paint.textSize = 12f
                    canvas.drawText("================================", 150f, yPosition, paint)
                    yPosition += 20f
                    
                    // Draw date
                    val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
                    paint.textSize = 14f
                    canvas.drawText("Tanggal: ${dateFormat.format(transaction.date)}", 150f, yPosition, paint)
                    yPosition += 25f
                    
                    // Draw separator
                    canvas.drawText("================================", 150f, yPosition, paint)
                    yPosition += 20f
                    
                    // Draw items
                    paint.textSize = 14f
                    transaction.items.forEach { item ->
                        canvas.drawText("${item.product.name} x${item.quantity}", 150f, yPosition, paint)
                        yPosition += 20f
                        canvas.drawText("Rp ${String.format("%,.0f", item.product.price * item.quantity)}", 150f, yPosition, paint)
                        yPosition += 30f
                    }
                    
                    // Draw separator
                    paint.textSize = 12f
                    canvas.drawText("================================", 150f, yPosition, paint)
                    yPosition += 20f
                    
                    // Draw total
                    paint.textSize = 16f
                    canvas.drawText("TOTAL: Rp ${String.format("%,.0f", transaction.totalAmount)}", 150f, yPosition, paint)
                    yPosition += 30f
                    
                    // Draw payment method
                    paint.textSize = 14f
                    canvas.drawText("Metode: ${transaction.paymentMethod}", 150f, yPosition, paint)
                    yPosition += 30f
                    
                    // Draw separator
                    paint.textSize = 12f
                    canvas.drawText("================================", 150f, yPosition, paint)
                    yPosition += 20f
                    
                    // Draw cashier
                    canvas.drawText("Kasir: ${transaction.cashierName}", 150f, yPosition, paint)
                    yPosition += 30f
                    
                    // Draw thank you message
                    paint.textSize = 14f
                    canvas.drawText("Terima Kasih!", 150f, yPosition, paint)
                    
                    pdfDocument.finishPage(page)
                    pdfDocument.writeTo(FileOutputStream(destination.fileDescriptor))
                    pdfDocument.close()
                    
                    callback.onWriteFinished(arrayOf(android.print.PageRange(0, 0)))
                } catch (e: Exception) {
                    callback.onWriteFailed(e.toString())
                }
            }
        }
        
        // Get paper size preference
        val paperSize = sharedPreferences.getString("paper_size", "58mm") ?: "58mm"
        
        // Use standard ISO media sizes
        val mediaSize = when (paperSize) {
            "58mm" -> PrintAttributes.MediaSize.ISO_A5
            "80mm" -> PrintAttributes.MediaSize.ISO_A4
            else -> PrintAttributes.MediaSize.ISO_A6
        }
        
        printManager.print(jobName, printAdapter, PrintAttributes.Builder()
            .setMediaSize(mediaSize)
            .setResolution(PrintAttributes.Resolution("dpi", "dpi", 203, 203))
            .setMinMargins(PrintAttributes.Margins.NO_MARGINS)
            .build()
        )
    }

    private fun shareReceipt(transaction: Transaction) {
        val fileName = "struk_${transaction.id}.pdf"
        val file = File(cacheDir, fileName)
        val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val storeName = sharedPreferences.getString("store_name", "") ?: ""
        val storeLocation = sharedPreferences.getString("store_location", "") ?: ""

        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(300, 600, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            val canvas = page.canvas
            val paint = Paint()

            // Set text color and center alignment
            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.CENTER

            var yPosition = 30f

            // Draw store name
            if (storeName.isNotEmpty()) {
                paint.textSize = 18f
                canvas.drawText(storeName, 150f, yPosition, paint)
                yPosition += 20f
            }

            // Draw store location
            if (storeLocation.isNotEmpty()) {
                paint.textSize = 12f
                canvas.drawText(storeLocation, 150f, yPosition, paint)
                yPosition += 20f
            }

            // Draw separator
            paint.textSize = 12f
            canvas.drawText("================================", 150f, yPosition, paint)
            yPosition += 20f

            // Draw title
            paint.textSize = 20f
            canvas.drawText("STRUK PEMBELIAN", 150f, yPosition, paint)
            yPosition += 30f

            // Draw separator
            paint.textSize = 12f
            canvas.drawText("================================", 150f, yPosition, paint)
            yPosition += 20f

            // Draw date
            val dateFormat = SimpleDateFormat("dd MMM yyyy, HH:mm", Locale("id", "ID"))
            paint.textSize = 14f
            canvas.drawText("Tanggal: ${dateFormat.format(transaction.date)}", 150f, yPosition, paint)
            yPosition += 25f

            // Draw separator
            canvas.drawText("================================", 150f, yPosition, paint)
            yPosition += 20f

            // Draw items
            paint.textSize = 14f
            transaction.items.forEach { item ->
                canvas.drawText("${item.product.name} x${item.quantity}", 150f, yPosition, paint)
                yPosition += 20f
                canvas.drawText("Rp ${String.format("%,.0f", item.product.price * item.quantity)}", 150f, yPosition, paint)
                yPosition += 30f
            }

            // Draw separator
            paint.textSize = 12f
            canvas.drawText("================================", 150f, yPosition, paint)
            yPosition += 20f

            // Draw total
            paint.textSize = 16f
            canvas.drawText("TOTAL: Rp ${String.format("%,.0f", transaction.totalAmount)}", 150f, yPosition, paint)
            yPosition += 30f

            // Draw payment method
            paint.textSize = 14f
            canvas.drawText("Metode: ${transaction.paymentMethod}", 150f, yPosition, paint)
            yPosition += 30f

            // Draw separator
            paint.textSize = 12f
            canvas.drawText("================================", 150f, yPosition, paint)
            yPosition += 20f

            // Draw cashier
            paint.textSize = 14f
            canvas.drawText("Kasir: ${transaction.cashierName}", 150f, yPosition, paint)
            yPosition += 30f

            // Draw thank you message
            paint.textSize = 14f
            canvas.drawText("Terima Kasih!", 150f, yPosition, paint)

            pdfDocument.finishPage(page)
            pdfDocument.writeTo(FileOutputStream(file))
            pdfDocument.close()

            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                file
            )

            val intent = Intent(Intent.ACTION_SEND).apply {
                type = "application/pdf"
                putExtra(Intent.EXTRA_STREAM, uri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(intent, "Bagikan Struk"))
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun downloadAccountingPdf(transactions: List<Transaction>, startDate: Date?, endDate: Date?) {
        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
        val dateRangeText = if (startDate != null && endDate != null) {
            "${dateFormat.format(startDate)} - ${dateFormat.format(endDate)}"
        } else {
            dateFormat.format(Date())
        }
        val fileName = "laporan_pembukuan_${dateRangeText.replace(" ", "_").replace("-", "_")}.pdf"
        val cacheFile = File(cacheDir, fileName)
        val sharedPreferences = getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val storeName = sharedPreferences.getString("store_name", "") ?: ""
        val storeLocation = sharedPreferences.getString("store_location", "") ?: ""

        try {
            val pdfDocument = PdfDocument()
            val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
            val page = pdfDocument.startPage(pageInfo)
            var canvas = page.canvas
            val paint = Paint()

            paint.color = Color.BLACK
            paint.textAlign = Paint.Align.CENTER
            var yPosition = 50f

            // Draw store name
            if (storeName.isNotEmpty()) {
                paint.textSize = 24f
                canvas.drawText(storeName, 297.5f, yPosition, paint)
                yPosition += 30f
            }

            // Draw store location
            if (storeLocation.isNotEmpty()) {
                paint.textSize = 14f
                canvas.drawText(storeLocation, 297.5f, yPosition, paint)
                yPosition += 30f
            }

            // Draw separator
            paint.textSize = 12f
            canvas.drawText("============================================", 297.5f, yPosition, paint)
            yPosition += 30f

            // Draw title
            paint.textSize = 22f
            canvas.drawText("LAPORAN PEMBUKUAN", 297.5f, yPosition, paint)
            yPosition += 30f

            // Draw date
            paint.textSize = 16f
            canvas.drawText("Periode: $dateRangeText", 297.5f, yPosition, paint)
            yPosition += 40f

            // Draw separator
            paint.textSize = 12f
            canvas.drawText("============================================", 297.5f, yPosition, paint)
            yPosition += 30f

            // Draw summary
            paint.textSize = 18f
            canvas.drawText("RINGKASAN", 297.5f, yPosition, paint)
            yPosition += 30f

            paint.textSize = 14f
            canvas.drawText("Total Transaksi: ${transactions.size}", 297.5f, yPosition, paint)
            yPosition += 25f

            val totalRevenue = transactions.sumOf { it.totalAmount }
            canvas.drawText("Total Penjualan: Rp ${String.format("%,.0f", totalRevenue)}", 297.5f, yPosition, paint)
            yPosition += 40f

            // Draw separator
            paint.textSize = 12f
            canvas.drawText("============================================", 297.5f, yPosition, paint)
            yPosition += 30f

            // Draw transaction details
            paint.textSize = 18f
            canvas.drawText("DETAIL TRANSAKSI", 297.5f, yPosition, paint)
            yPosition += 30f

            paint.textSize = 12f
            transactions.forEachIndexed { index, transaction ->
                if (yPosition > 750f) {
                    pdfDocument.finishPage(page)
                    val newPageInfo = PdfDocument.PageInfo.Builder(595, 842, pdfDocument.pages.size + 1).create()
                    val newPage = pdfDocument.startPage(newPageInfo)
                    canvas = newPage.canvas
                    yPosition = 50f
                }

                canvas.drawText("Transaksi #${index + 1}", 297.5f, yPosition, paint)
                yPosition += 20f

                val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))
                canvas.drawText("Waktu: ${timeFormat.format(transaction.date)}", 297.5f, yPosition, paint)
                yPosition += 20f

                canvas.drawText("Total: Rp ${String.format("%,.0f", transaction.totalAmount)}", 297.5f, yPosition, paint)
                yPosition += 20f

                canvas.drawText("Metode: ${transaction.paymentMethod}", 297.5f, yPosition, paint)
                yPosition += 20f

                // Draw items
                paint.textSize = 10f
                transaction.items.forEach { item ->
                    canvas.drawText("- ${item.product.name} x${item.quantity}", 297.5f, yPosition, paint)
                    yPosition += 15f
                }
                yPosition += 15f
            }

            pdfDocument.finishPage(page)
            pdfDocument.writeTo(FileOutputStream(cacheFile))
            pdfDocument.close()

            // Show PDF preview
            val uri = FileProvider.getUriForFile(
                this,
                "${packageName}.fileprovider",
                cacheFile
            )

            val intent = Intent(Intent.ACTION_VIEW).apply {
                type = "application/pdf"
                setDataAndType(uri, "application/pdf")
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
            }

            startActivity(intent)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}

enum class AppDestinations(
    val label: String,
    val icon: Int,
) {
    SETTINGS("Pengaturan", R.drawable.ic_account_box),
    PRODUCT_MANAGEMENT("Produk", R.drawable.ic_inventory),
    HOME("Order", R.drawable.ic_order),
    CART("Keranjang", R.drawable.ic_shopping_cart),
    PROFILE("Riwayat", R.drawable.ic_history),
    SALES_REPORT("Pembukuan", R.drawable.ic_receipt),
}

@PreviewScreenSizes
@Composable
fun KasirApp(
    context: Context? = null,
    onPrintReceipt: (Transaction) -> Unit = {},
    onShareReceipt: (Transaction) -> Unit = {},
    onDownloadAccountingPdf: (List<Transaction>, Date?, Date?) -> Unit = { _, _, _ -> },
    onExit: () -> Unit = {},
    onGetTransactions: () -> List<Transaction> = { emptyList() }
) {
    var currentDestination by rememberSaveable { mutableStateOf<AppDestinations>(AppDestinations.SETTINGS) }
    var showCheckout by rememberSaveable { mutableStateOf(false) }
    var showProductForm by rememberSaveable { mutableStateOf(false) }
    var showReceipt by rememberSaveable { mutableStateOf(false) }
    var showOpenBalance by rememberSaveable { mutableStateOf(false) }
    var isCloseBalance by rememberSaveable { mutableStateOf(false) }
    var showPaperSizeDialog by rememberSaveable { mutableStateOf(false) }
    var showOrderDialog by rememberSaveable { mutableStateOf(false) }
    var showExitDialog by rememberSaveable { mutableStateOf(false) }
    var selectedProduct by rememberSaveable { mutableStateOf<Product?>(null) }
    var editingProduct by rememberSaveable { mutableStateOf<Product?>(null) }
    var lastTransaction by rememberSaveable { mutableStateOf<Transaction?>(null) }
    
    val cartItems = rememberSaveable { mutableStateListOf<CartItem>() }
    val favoriteProducts = rememberSaveable { mutableStateListOf<Product>() }
    val transactions = rememberSaveable { mutableStateListOf<Transaction>() }
    val orders = rememberSaveable { mutableStateListOf<Order>() }
    val coroutineScope = rememberCoroutineScope()
    
    // Load transactions from SharedPreferences
    LaunchedEffect(Unit) {
        if (context != null) {
            val loadedTransactions = TransactionDataSource.loadTransactions(context)
            transactions.clear()
            transactions.addAll(loadedTransactions)
        }
    }
    
    // Save transactions whenever they change using derived state key
    LaunchedEffect(transactions.toList()) {
        if (context != null) {
            TransactionDataSource.saveTransactions(context, transactions.toList())
        }
    }
    
    // Check if today's balance is set
    LaunchedEffect(Unit) {
        if (context != null && !BalanceDataSource.isTodayBalanceSet(context)) {
            showOpenBalance = true
        }
    }

    BackHandler {
        showExitDialog = true
    }
    
    NavigationSuiteScaffold(
            navigationSuiteItems = {
                AppDestinations.entries.forEach {
                    item(
                        icon = {
                            Icon(
                                painterResource(it.icon),
                                contentDescription = it.label,
                                modifier = Modifier.size(24.dp)
                            )
                        },
                        label = { Text(it.label, textAlign = androidx.compose.ui.text.style.TextAlign.Center) },
                        selected = it == currentDestination,
                        onClick = { 
                            currentDestination = it
                            showCheckout = false
                        }
                    )
                }
            }
        ) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            bottomBar = {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    contentAlignment = androidx.compose.ui.Alignment.Center
                ) {
                    Text(
                        text = "© 2026 Rama",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }
        ) { innerPadding ->
            when {
                showCheckout -> {
                    CheckoutScreen(
                        cartItems = cartItems,
                        onCompletePayment = { paymentMethod ->
                            val openBalance = context?.let { BalanceDataSource.getTodayBalance(it)?.openingBalance } ?: 0.0
                            val transaction = Transaction(
                                id = UUID.randomUUID().toString(),
                                items = cartItems.toList(),
                                totalAmount = cartItems.sumOf { it.totalPrice },
                                paymentMethod = paymentMethod,
                                date = Date(),
                                openBalance = openBalance
                            )
                            transactions.add(transaction)
                            lastTransaction = transaction
                            cartItems.clear()
                            showCheckout = false
                            showReceipt = true
                            
                            // Save transactions to SharedPreferences immediately
                            if (context != null) {
                                TransactionDataSource.saveTransactions(context, transactions.toList())
                            }
                        },
                        onCancel = {
                            showCheckout = false
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                currentDestination == AppDestinations.HOME -> {
                    HomeScreen(
                        onProductClick = { product ->
                            val existingItem = cartItems.find { it.product.id == product.id }
                            if (existingItem != null) {
                                val index = cartItems.indexOf(existingItem)
                                cartItems[index] = existingItem.copy(quantity = existingItem.quantity + 1)
                            } else {
                                cartItems.add(CartItem(product, 1))
                            }
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                currentDestination == AppDestinations.CART -> {
                    CartScreen(
                        cartItems = cartItems,
                        onQuantityChange = { productId, quantity ->
                            val item = cartItems.find { it.product.id == productId }
                            if (item != null) {
                                val index = cartItems.indexOf(item)
                                cartItems[index] = item.copy(quantity = quantity)
                            }
                        },
                        onRemoveItem = { productId ->
                            cartItems.removeIf { it.product.id == productId }
                        },
                        onCheckout = {
                            showCheckout = true
                        },
                        onManageOrders = {
                            showOrderDialog = true
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                currentDestination == AppDestinations.PROFILE -> {
                    TransactionHistoryScreen(
                        transactions = transactions,
                        onTransactionClick = { transaction ->
                            lastTransaction = transaction
                            showReceipt = true
                        },
                        onDeleteTransaction = { transaction ->
                            transactions.remove(transaction)
                            if (context != null) {
                                TransactionDataSource.saveTransactions(context, transactions.toList())
                            }
                        },
                        onDownloadAccountingPdf = {
                            onDownloadAccountingPdf(transactions.toList(), null, null)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                currentDestination == AppDestinations.PRODUCT_MANAGEMENT -> {
                    ProductManagementScreen(
                        products = DataSource.products,
                        onAddProduct = {
                            editingProduct = null
                            showProductForm = true
                        },
                        onEditProduct = { product ->
                            editingProduct = product
                            showProductForm = true
                        },
                        onDeleteProduct = { productId ->
                            DataSource.deleteProduct(productId)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                currentDestination == AppDestinations.SETTINGS -> {
                    SettingsScreen(
                        onOpenBalance = { 
                            isCloseBalance = false
                            showOpenBalance = true 
                        },
                        onCloseBalance = { 
                            isCloseBalance = true
                            showOpenBalance = true 
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
                currentDestination == AppDestinations.SALES_REPORT -> {
                    SalesReportScreen(
                        transactions = transactions.toList(),
                        onDismiss = {
                            currentDestination = AppDestinations.SETTINGS
                        },
                        onDownloadAccountingPdf = { filteredTx, startDate, endDate ->
                            onDownloadAccountingPdf(filteredTx, startDate, endDate)
                        },
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }
    }

    if (showExitDialog) {
        AlertDialog(
            onDismissRequest = { showExitDialog = false },
            title = {
                Text("Keluar Aplikasi")
            },
            text = {
                Text("Apakah Anda yakin ingin keluar dari aplikasi?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (context != null) {
                            // Save transactions before exiting
                            TransactionDataSource.saveTransactions(context, transactions.toList())
                        }
                        onExit()
                    }
                ) {
                    Text("Ya")
                }
            },
            dismissButton = {
                TextButton(onClick = { showExitDialog = false }) {
                    Text("Tidak")
                }
            }
        )
    }

    if (showProductForm) {
        ProductFormDialog(
            product = editingProduct,
            onDismiss = {
                showProductForm = false
                editingProduct = null
            },
            onSave = { id, name, price, category, description, stock ->
                val product = Product(
                    id = id,
                    name = name,
                    price = price,
                    category = category,
                    description = description,
                    stock = stock
                )
                if (editingProduct != null) {
                    DataSource.updateProduct(product)
                } else {
                    DataSource.addProduct(product)
                }
                showProductForm = false
                editingProduct = null
            }
        )
    }

    if (showOpenBalance) {
        if (isCloseBalance) {
            val openBalance = context?.let { BalanceDataSource.getTodayBalance(it)?.openingBalance } ?: 0.0
            val totalSales = transactions.filter { transaction ->
                val today = java.util.Calendar.getInstance().apply {
                    set(java.util.Calendar.HOUR_OF_DAY, 0)
                    set(java.util.Calendar.MINUTE, 0)
                    set(java.util.Calendar.SECOND, 0)
                    set(java.util.Calendar.MILLISECOND, 0)
                }.time
                transaction.date >= today
            }.sumOf { it.totalAmount }
            
            CloseBalanceDialog(
                openBalance = openBalance,
                totalSales = totalSales,
                onDismiss = { showOpenBalance = false },
                onConfirm = {
                    if (context != null) {
                        BalanceDataSource.setTodayBalance(context, openBalance + totalSales)
                    }
                    showOpenBalance = false
                }
            )
        } else {
            OpenBalanceDialog(
                isCloseBalance = false,
                onDismiss = { showOpenBalance = false },
                onConfirm = { balance ->
                    if (context != null) {
                        BalanceDataSource.setTodayBalance(context, balance)
                    }
                    showOpenBalance = false
                }
            )
        }
    }

    if (showPaperSizeDialog && context != null) {
        val sharedPreferences = context.getSharedPreferences("app_settings", Context.MODE_PRIVATE)
        val currentSize = sharedPreferences.getString("paper_size", "58mm") ?: "58mm"
        
        PaperSizeDialog(
            currentSize = currentSize,
            onDismiss = {
                showPaperSizeDialog = false
            },
            onPaperSizeSelected = { size ->
                sharedPreferences.edit().putString("paper_size", size).apply()
                showPaperSizeDialog = false
            }
        )
    }

    if (showOrderDialog) {
        OrderDialog(
            openOrders = orders.filter { it.status == OrderStatus.OPEN },
            onDismiss = {
                showOrderDialog = false
            },
            onLoadOrder = { order ->
                cartItems.clear()
                cartItems.addAll(order.items)
                showOrderDialog = false
            },
            onCloseOrder = { order ->
                val updatedOrder = order.copy(
                    status = OrderStatus.CLOSED,
                    closedAt = Date()
                )
                val index = orders.indexOf(order)
                if (index != -1) {
                    orders[index] = updatedOrder
                }
            },
            onSaveCurrentOrder = { customerName, tableName ->
                if (cartItems.isNotEmpty()) {
                    val order = Order(
                        id = UUID.randomUUID().toString(),
                        items = cartItems.toList(),
                        totalAmount = cartItems.sumOf { it.totalPrice },
                        customerName = customerName.ifBlank { null },
                        tableName = tableName.ifBlank { null },
                        status = OrderStatus.OPEN
                    )
                    orders.add(order)
                }
            }
        )
    }

    if (showReceipt && lastTransaction != null) {
        ReceiptScreen(
            transaction = lastTransaction!!,
            onDismiss = {
                showReceipt = false
                currentDestination = AppDestinations.PROFILE
            },
            onPrint = {
                onPrintReceipt(lastTransaction!!)
            },
            onShare = {
                onShareReceipt(lastTransaction!!)
            }
        )
    }
}