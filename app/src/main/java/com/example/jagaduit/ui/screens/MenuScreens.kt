package com.example.jagaduit.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.jagaduit.utils.toRupiah
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun TransactionScreen(
    navController: NavController,
    viewModel: com.example.jagaduit.viewmodel.TransactionViewModel = androidx.lifecycle.viewmodel.compose.viewModel()
) {
    // Ambil data dari database
    val allTransactions by viewModel.transactionList.collectAsState(initial = emptyList())

    // State buat filter berdasarkan bulan
    var currentMonthOffset by remember { androidx.compose.runtime.mutableIntStateOf(0) }
    val calendar = java.util.Calendar.getInstance()
    calendar.add(java.util.Calendar.MONTH, currentMonthOffset)

    val currentMonthName = java.text.SimpleDateFormat("MMMM yyyy", java.util.Locale.getDefault()).format(calendar.time)

    val filteredTransactions = allTransactions.filter { txn ->
        val txnCal = java.util.Calendar.getInstance()
        txnCal.timeInMillis = txn.date
        txnCal.get(java.util.Calendar.MONTH) == calendar.get(java.util.Calendar.MONTH) &&
                txnCal.get(java.util.Calendar.YEAR) == calendar.get(java.util.Calendar.YEAR)
    }

    // Hitung total balance bulan ini
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
                Icon(androidx.compose.material.icons.Icons.Default.Add, contentDescription = "Add")
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
            // selector bulan
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { currentMonthOffset-- }) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Prev", tint = Color.White)
                }

                Text(
                    text = currentMonthName,
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary // Lime
                )

                IconButton(onClick = { currentMonthOffset++ }) {
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next", tint = Color.White)
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Summary card
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
                        color = if (balance >= 0) MaterialTheme.colorScheme.secondary else Color.White
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                        Text("In: ${totalIncome.toRupiah()}", color = MaterialTheme.colorScheme.secondary, fontSize = 12.sp)
                        Text("Out: ${totalExpense.toRupiah()}", color = Color.White, fontSize = 12.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // List transaksi
            if (filteredTransactions.isEmpty()) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Text("Belum ada transaksi bulan ini", color = Color.Gray)
                }
            } else {
                androidx.compose.foundation.lazy.LazyColumn {
                    items(filteredTransactions) { txn ->
                        TransactionItem(
                            transaction = txn,
                            onDelete = {viewModel.deleteTransaction(txn) },
                            onEdit = {navController.navigate("input_transaction?txnId=${txn.id}") }
                        )
                    }
                }
            }
        }
    }
}

// Placeholder menu account
@Composable
fun AccountScreen() {
    ScreenPlaceholder("Account Screen\n(Wallet List)")
}

// Placeholder menu goal
@Composable
fun GoalScreen() {
    ScreenPlaceholder("Goal Saving Screen")
}

// Helper sederhana untuk tampilan sementara
@Composable
fun ScreenPlaceholder(title: String) {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text(text = title, color = MaterialTheme.colorScheme.primary)
    }
}

@Composable
fun TransactionItem(
    transaction: com.example.jagaduit.data.TransactionEntity,
    onDelete: () -> Unit, // Callback delete
    onEdit: () -> Unit    // Callback edit
) {
    // State buat dialog
    var showDialog by remember { mutableStateOf(false) }

    val amountColor = when (transaction.type) {
        "INCOME" -> MaterialTheme.colorScheme.secondary
        "EXPENSE" -> Color.White
        else -> MaterialTheme.colorScheme.primary
    }
    val sign = if (transaction.type == "EXPENSE") "-" else "+"

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { showDialog = true },
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = androidx.compose.foundation.shape.RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (transaction.type == "TRANSFER") "Transfer ke ${transaction.accountTo}" else transaction.category,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = SimpleDateFormat("dd MMM yyyy", java.util.Locale.getDefault()).format(java.util.Date(transaction.date)),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.Gray
                )
                if (transaction.note.isNotEmpty()) {
                    Text(
                        text = transaction.note,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.DarkGray,
                        maxLines = 1
                    )
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

    // Dialog konfirmasi
    if (showDialog) {
        AlertDialog(
            onDismissRequest = { showDialog = false },
            title = { Text(text = "Action") },
            text = { Text("What do you want to do with this transaction?") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onEdit() // Panggil Edit
                    }
                ) { Text("Edit", color = MaterialTheme.colorScheme.secondary) }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDialog = false
                        onDelete() // Panggil Delete
                    }
                ) { Text("Delete", color = Color.Red) }
            },
            containerColor = MaterialTheme.colorScheme.surface,
            titleContentColor = Color.White,
            textContentColor = Color.Gray
        )
    }
}