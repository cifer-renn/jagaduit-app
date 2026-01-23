package com.example.jagaduit.ui.screens

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.AttachFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jagaduit.ui.components.MonthYearPickerDialog
import com.example.jagaduit.utils.toRupiah
import java.text.SimpleDateFormat
import java.util.Locale

val ExpenseRed = Color(0xFFEF5350)

@Composable
fun TransactionScreen(
    navController: NavController,
    viewModel: com.example.jagaduit.viewmodel.TransactionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    val allTransactions by viewModel.transactionList.collectAsState(initial = emptyList())

    // State Filter Bulan
    var currentMonthOffset by remember { mutableIntStateOf(0) }
    val calendar = java.util.Calendar.getInstance()
    calendar.add(java.util.Calendar.MONTH, currentMonthOffset)

    // Format: MMM yyyy (Jan 2026)
    val currentMonthName = SimpleDateFormat("MMM yyyy", Locale.US).format(calendar.time)

    // State Dialog Picker
    var showMonthPicker by remember { mutableStateOf(false) }

    val filteredTransactions = allTransactions.filter { txn ->
        val txnCal = java.util.Calendar.getInstance()
        txnCal.timeInMillis = txn.date
        txnCal.get(java.util.Calendar.MONTH) == calendar.get(java.util.Calendar.MONTH) &&
                txnCal.get(java.util.Calendar.YEAR) == calendar.get(java.util.Calendar.YEAR)
    }

    val totalIncome = filteredTransactions.filter { it.type == "INCOME" }.sumOf { it.amount }
    val totalExpense = filteredTransactions.filter { it.type == "EXPENSE" }.sumOf { it.amount }
    val balance = totalIncome - totalExpense

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = { navController.navigate("input_transaction") },
                containerColor = MaterialTheme.colorScheme.secondary,
                contentColor = MaterialTheme.colorScheme.onSecondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add")
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // --- HEADER KOMPAK POJOK KIRI ATAS ---
            Row(
                modifier = Modifier
                    .wrapContentWidth() // Agar tidak melebar ke samping
                    .padding(vertical = 2.dp), // Padding atas bawah agar sejajar
                horizontalArrangement = Arrangement.Start, // Rata Kiri
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Panah Kiri
                IconButton(onClick = { currentMonthOffset-- }) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                // Teks Tengah (Klik untuk Buka Picker)
                Text(
                    text = currentMonthName,
                    style = MaterialTheme.typography.titleMedium, // Ukuran lebih kecil
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary, // Lime
                    modifier = Modifier
                        .clickable { showMonthPicker = true }
                        .padding(horizontal = 8.dp) // Jarak dengan panah
                )

                // Panah Kanan
                IconButton(onClick = { currentMonthOffset++ }) {
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            Spacer(modifier = Modifier.height(2.dp))

            // --- SUMMARY CARD ---
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text("Cash Flow (Bulan Ini)", color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = if (balance >= 0) "+ ${balance.toRupiah()}" else "- ${(kotlin.math.abs(balance).toRupiah())}",
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("In: ${totalIncome.toRupiah()}", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        Text("Out: ${totalExpense.toRupiah()}", color = ExpenseRed, fontSize = 12.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))
            Text("Riwayat Transaksi", color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // --- LIST TRANSAKSI ---
            if (filteredTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada transaksi bulan ini", color = Color.Gray)
                }
            } else {
                LazyColumn {
                    items(filteredTransactions) { txn ->
                        TransactionItem(
                            transaction = txn,
                            onDelete = { viewModel.deleteTransaction(txn) },
                            onEdit = { navController.navigate("input_transaction?txnId=${txn.id}") }
                        )
                    }
                }
            }
        }
    }

    // --- DIALOG MONTH PICKER ---
    MonthYearPickerDialog(
        visible = showMonthPicker,
        currentMonth = calendar.get(java.util.Calendar.MONTH),
        currentYear = calendar.get(java.util.Calendar.YEAR),
        onDismiss = { showMonthPicker = false },
        onDateSelected = { month, year ->
            val today = java.util.Calendar.getInstance()
            val target = java.util.Calendar.getInstance()
            target.set(java.util.Calendar.YEAR, year)
            target.set(java.util.Calendar.MONTH, month)

            val diffYear = target.get(java.util.Calendar.YEAR) - today.get(java.util.Calendar.YEAR)
            val diffMonth = target.get(java.util.Calendar.MONTH) - today.get(java.util.Calendar.MONTH)

            currentMonthOffset = (diffYear * 12) + diffMonth
        }
    )
}

@Composable
fun TransactionItem(
    transaction: com.example.jagaduit.data.TransactionEntity,
    onDelete: () -> Unit,
    onEdit: () -> Unit
) {
    var showDialog by remember { mutableStateOf(false) }

    val amountColor = when (transaction.type) {
        "INCOME" -> MaterialTheme.colorScheme.secondary
        "EXPENSE" -> ExpenseRed
        else -> MaterialTheme.colorScheme.primary
    }
    val sign = if (transaction.type == "EXPENSE") "-" else "+"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { showDialog = true },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = if (transaction.type == "TRANSFER") "Transfer ke ${transaction.accountTo}" else transaction.category,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.onSurface
                    )

                    if (!transaction.imagePath.isNullOrEmpty()) {
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(
                            imageVector = Icons.Default.AttachFile,
                            contentDescription = "Attachment",
                            tint = Color.Gray,
                            modifier = Modifier.size(16.dp)
                        )
                    }
                }

                Text(
                    text = SimpleDateFormat("dd MMM yyyy", Locale.getDefault()).format(java.util.Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                if (transaction.note.isNotEmpty()) {
                    Text(text = transaction.note, style = MaterialTheme.typography.bodySmall, color = Color.DarkGray, maxLines = 1)
                }
            }

            Text(
                text = "$sign ${(transaction.amount).toRupiah()}",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = amountColor
            )
        }
    }

    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Details") },
            text = {
                Column {
                    if (!transaction.imagePath.isNullOrEmpty()) {
                        val bitmap = BitmapFactory.decodeFile(transaction.imagePath)
                        if (bitmap != null) {
                            Image(
                                bitmap = bitmap.asImageBitmap(),
                                contentDescription = "Proof",
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(200.dp)
                                    .clip(RoundedCornerShape(8.dp))
                                    .background(Color.Black),
                                contentScale = ContentScale.Crop
                            )
                            Spacer(modifier = Modifier.height(16.dp))
                        }
                    }
                    Text("What do you want to do?")
                }
            },
            confirmButton = { TextButton(onClick = { showDialog = false; onEdit() }) { Text("Edit", color = MaterialTheme.colorScheme.secondary) } },
            dismissButton = { TextButton(onClick = { showDialog = false; onDelete() }) { Text("Delete", color = Color.Red) } },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = Color.White,
            textContentColor = Color.Gray
        )
    }
}
@Composable
fun GoalScreen() { Box(Modifier.fillMaxSize(), Alignment.Center) { Text("Goal Screen", color = Color.White) } }