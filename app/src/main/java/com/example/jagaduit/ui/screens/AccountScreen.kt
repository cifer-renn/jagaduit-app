package com.example.jagaduit.ui.screens

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.jagaduit.data.AccountEntity
import com.example.jagaduit.utils.toRupiah
import com.example.jagaduit.viewmodel.AccountViewModel
import com.example.jagaduit.viewmodel.TransactionViewModel
import kotlin.math.abs

// Factory untuk TransactionViewModel
class TransactionViewModelFactory(private val application: Application) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(TransactionViewModel::class.java)) {
            return TransactionViewModel(application) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@Composable
fun AccountScreen(
    viewModel: AccountViewModel
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    val txnViewModel: TransactionViewModel = viewModel(
        factory = TransactionViewModelFactory(application)
    )

    val accountList by viewModel.allAccounts.collectAsState(initial = emptyList())
    val totalBalance = if (accountList.isNotEmpty()) accountList.sumOf { it.balance } else 0.0

    // --- STATE DIALOG ---
    var showAddDialog by remember { mutableStateOf(false) }
    var showEditDialog by remember { mutableStateOf(false) }
    var showConfirmDifferenceDialog by remember { mutableStateOf(false) }

    // --- 1. STATE BARU UNTUK HAPUS ---
    var showDeleteDialog by remember { mutableStateOf(false) }
    var accountToDelete by remember { mutableStateOf<AccountEntity?>(null) } // Simpan akun yg mau dihapus

    // Data sementara untuk Edit
    var accountToEdit by remember { mutableStateOf<AccountEntity?>(null) }
    var inputName by remember { mutableStateOf("") }
    var inputBalance by remember { mutableStateOf("") }

    // Data selisih
    var detectedDifference by remember { mutableDoubleStateOf(0.0) }

    // Fungsi Reset
    fun resetInput() {
        inputName = ""
        inputBalance = ""
        accountToEdit = null
        accountToDelete = null // Reset juga yg mau dihapus
        detectedDifference = 0.0
    }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    resetInput()
                    showAddDialog = true
                },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Account", tint = Color.Black)
            }
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // Header Total Saldo
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface)
            ) {
                Column(
                    modifier = Modifier.padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text("Total Saldo (All Wallets)", color = Color.Gray)
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = totalBalance.toRupiah(),
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Daftar Akun", color = Color.Gray, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))

            // List Akun
            LazyColumn {
                items(accountList) { account ->
                    AccountItem(
                        account = account,
                        onEditClick = {
                            accountToEdit = account
                            inputName = account.name
                            val balanceLong = account.balance.toLong()
                            inputBalance = if (account.balance == balanceLong.toDouble()) {
                                balanceLong.toString()
                            } else {
                                account.balance.toString()
                            }
                            showEditDialog = true
                        },
                        onDeleteClick = {
                            // --- 2. UBAH LOGIKA TOMBOL DELETE ---
                            // Jangan langsung hapus, tapi simpan dulu datanya & buka dialog
                            accountToDelete = account
                            showDeleteDialog = true
                        }
                    )
                }
            }
        }
    }

    // --- DIALOG TAMBAH AKUN ---
    if (showAddDialog) {
        AlertDialog(
            onDismissRequest = { showAddDialog = false },
            title = { Text("Tambah Akun Baru") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Nama Akun (cth: OVO)") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputBalance,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) inputBalance = it },
                        label = { Text("Saldo Awal") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    if (inputName.isNotEmpty() && inputBalance.isNotEmpty()) {
                        viewModel.addAccount(inputName, inputBalance.toDoubleOrNull() ?: 0.0)
                        showAddDialog = false
                    }
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showAddDialog = false }) { Text("Batal") }
            }
        )
    }

    // --- DIALOG EDIT AKUN ---
    if (showEditDialog && accountToEdit != null) {
        AlertDialog(
            onDismissRequest = { showEditDialog = false },
            title = { Text("Edit Akun") },
            text = {
                Column {
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        label = { Text("Nama Akun") }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = inputBalance,
                        onValueChange = { if (it.all { c -> c.isDigit() || c == '.' }) inputBalance = it },
                        label = { Text("Saldo Saat Ini") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(onClick = {
                    val newBalance = inputBalance.toDoubleOrNull() ?: 0.0
                    val oldBalance = accountToEdit!!.balance
                    val diff = newBalance - oldBalance

                    if (abs(diff) > 0.0) {
                        detectedDifference = diff
                        showEditDialog = false
                        showConfirmDifferenceDialog = true
                    } else {
                        val updated = accountToEdit!!.copy(name = inputName, balance = newBalance)
                        viewModel.updateAccount(updated)
                        showEditDialog = false
                    }
                }) { Text("Simpan") }
            },
            dismissButton = {
                TextButton(onClick = { showEditDialog = false }) { Text("Batal") }
            }
        )
    }

    // --- DIALOG WARNING SELISIH ---
    if (showConfirmDifferenceDialog && accountToEdit != null) {
        val isProfit = detectedDifference > 0
        val diffText = abs(detectedDifference).toRupiah()

        AlertDialog(
            onDismissRequest = { showConfirmDifferenceDialog = false },
            containerColor = Color(0xFF1E1E1E),
            titleContentColor = Color.White,
            textContentColor = Color.LightGray,
            title = { Text("Penyesuaian Saldo") },
            text = {
                Column {
                    Text("Selisih saldo terdeteksi sebesar:")
                    Text(
                        text = diffText,
                        fontSize = 18.sp,
                        fontWeight = FontWeight.Bold,
                        color = if(isProfit) MaterialTheme.colorScheme.secondary else Color(0xFFFF6B6B)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("Selisih ini akan mengubah saldo akun Anda. Apakah Anda ingin mencatat selisih ini sebagai Transaksi (Difference) agar pembukuan rapi?")
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val finalBalance = inputBalance.toDoubleOrNull() ?: 0.0
                        val updatedAccount = accountToEdit!!.copy(name = inputName, balance = finalBalance)
                        viewModel.updateAccount(updatedAccount)

                        txnViewModel.saveTransaction(
                            id = 0,
                            date = System.currentTimeMillis(),
                            amount = abs(detectedDifference),
                            type = if (isProfit) "INCOME" else "EXPENSE",
                            category = "Difference",
                            accountFrom = updatedAccount.name,
                            accountTo = null,
                            note = "Penyesuaian Saldo Manual"
                        )

                        Toast.makeText(context, "Saldo diupdate & Transaksi tercatat!", Toast.LENGTH_SHORT).show()
                        showConfirmDifferenceDialog = false
                        resetInput()
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                ) {
                    Text("YA, CATAT", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        val finalBalance = inputBalance.toDoubleOrNull() ?: 0.0
                        val updatedAccount = accountToEdit!!.copy(name = inputName, balance = finalBalance)
                        viewModel.updateAccount(updatedAccount)

                        Toast.makeText(context, "Hanya saldo yang diupdate.", Toast.LENGTH_SHORT).show()
                        showConfirmDifferenceDialog = false
                        resetInput()
                    }
                ) {
                    Text("TIDAK", color = Color.White)
                }
            }
        )
    }

    // --- 3. DIALOG KONFIRMASI HAPUS (BARU) ---
    if (showDeleteDialog && accountToDelete != null) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Hapus Akun") },
            text = {
                Text("Apakah Anda yakin ingin menghapus akun \"${accountToDelete?.name}\"?")
            },
            confirmButton = {
                Button(
                    onClick = {
                        // EKSEKUSI HAPUS
                        accountToDelete?.let {
                            viewModel.deleteAccount(it)
                        }
                        showDeleteDialog = false
                        accountToDelete = null // Bersihkan state
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color.Red) // Merah biar warning
                ) {
                    Text("YA", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false // Tutup dialog aja
                        accountToDelete = null
                    }
                ) {
                    Text("TIDAK")
                }
            }
        )
    }
}

@Composable
fun AccountItem(
    account: AccountEntity,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        shape = RoundedCornerShape(12.dp)
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column {
                Text(account.name, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                Text(account.balance.toRupiah(), color = MaterialTheme.colorScheme.secondary)
            }
            Row {
                IconButton(onClick = onEditClick) {
                    Icon(Icons.Default.Edit, contentDescription = "Edit", tint = Color.Gray)
                }
                // Saat tombol ini diklik, dia akan memanggil onDeleteClick yg ada di LazyColumn
                IconButton(onClick = onDeleteClick) {
                    Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.Red)
                }
            }
        }
    }
}