package com.example.jagaduit.ui.screen

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.example.jagaduit.data.AccountEntity
import com.example.jagaduit.ui.theme.DarkSurface
import com.example.jagaduit.ui.theme.GrayText
import com.example.jagaduit.ui.theme.NeonBlack
import com.example.jagaduit.ui.theme.NeonLime
import com.example.jagaduit.ui.theme.PureWhite
import com.example.jagaduit.viewmodel.AccountViewModel
import java.text.NumberFormat
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AccountScreen(
    viewModel: AccountViewModel
) {
    val accounts by viewModel.allAccounts.collectAsState()
    val totalBalance by viewModel.totalBalance.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = NeonBlack, // Background Layar Hitam
        topBar = {
            CenterAlignedTopAppBar(
                title = {
                    Text(
                        "Dompet Saya",
                        fontWeight = FontWeight.Bold,
                        fontSize = 20.sp,
                        color = NeonLime // Judul Lime
                    )
                },
                colors = TopAppBarDefaults.centerAlignedTopAppBarColors(
                    containerColor = NeonBlack
                )
            )
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showDialog = true },
                containerColor = NeonLime, // Tombol Lime
                contentColor = NeonBlack,   // Icon Hitam
                shape = RoundedCornerShape(16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Tambah Akun")
            }
        }
    ) { paddingValues ->

        Column(
            modifier = Modifier
                .padding(paddingValues)
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            // Kartu Total (Dark + Lime Text)
            TotalBalanceCard(totalBalance ?: 0.0)

            Spacer(modifier = Modifier.height(24.dp))

            Text(
                text = "Daftar Akun",
                style = MaterialTheme.typography.labelLarge,
                color = GrayText,
                modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
            )

            // List Akun
            LazyColumn(
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(accounts) { account ->
                    AccountItem(
                        account = account,
                        onDelete = { viewModel.deleteAccount(account) }
                    )
                }
            }
        }

        if (showDialog) {
            AddAccountDialog(
                onDismiss = { showDialog = false },
                onConfirm = { name, balance ->
                    viewModel.addAccount(name, balance)
                    showDialog = false
                }
            )
        }
    }
}

@Composable
fun TotalBalanceCard(total: Double) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface), // Background Gelap
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                "Total Kekayaan",
                color = GrayText,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = formatRupiah(total),
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = NeonLime // Angka Lime Menyala
            )
        }
    }
}

@Composable
fun AccountItem(account: AccountEntity, onDelete: () -> Unit) {
    Card(
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = DarkSurface), // Background Gelap
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = account.name,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold,
                    color = PureWhite
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = formatRupiah(account.balance),
                    style = MaterialTheme.typography.bodyMedium,
                    color = NeonLime.copy(alpha = 0.9f),
                    fontWeight = FontWeight.Medium
                )
            }

            IconButton(onClick = onDelete) {
                Icon(
                    Icons.Default.Delete,
                    contentDescription = "Hapus",
                    tint = GrayText.copy(alpha = 0.6f)
                )
            }
        }
    }
}

@Composable
fun AddAccountDialog(
    onDismiss: () -> Unit,
    onConfirm: (String, Double) -> Unit
) {
    var name by remember { mutableStateOf("") }
    var balanceStr by remember { mutableStateOf("") }

    Dialog(onDismissRequest = onDismiss) {
        Card(
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = DarkSurface),
            modifier = Modifier.padding(16.dp)
        ) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = "Tambah Akun",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = PureWhite
                )

                Spacer(modifier = Modifier.height(16.dp))

                OutlinedTextField(
                    value = name,
                    onValueChange = { name = it },
                    label = { Text("Nama Akun", color = GrayText) },
                    singleLine = true,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = GrayText,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        cursorColor = NeonLime
                    )
                )

                Spacer(modifier = Modifier.height(12.dp))

                OutlinedTextField(
                    value = balanceStr,
                    onValueChange = { if (it.all { char -> char.isDigit() }) balanceStr = it },
                    label = { Text("Saldo Awal", color = GrayText) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                    singleLine = true,
                    prefix = { Text("Rp ", color = NeonLime) },
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = NeonLime,
                        unfocusedBorderColor = GrayText,
                        focusedTextColor = PureWhite,
                        unfocusedTextColor = PureWhite,
                        cursorColor = NeonLime
                    )
                )

                Spacer(modifier = Modifier.height(24.dp))

                Button(
                    onClick = {
                        val balance = balanceStr.toDoubleOrNull() ?: 0.0
                        if (name.isNotEmpty()) {
                            onConfirm(name, balance)
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = NeonLime,
                        contentColor = NeonBlack
                    )
                ) {
                    Text("Simpan", fontWeight = FontWeight.Bold)
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Batal", color = GrayText)
                }
            }
        }
    }
}

// Fungsi Helper Format Rupiah (Penyebab error tadi sudah ada di sini)
fun formatRupiah(amount: Double): String {
    val format = NumberFormat.getCurrencyInstance(Locale("id", "ID"))
    return format.format(amount).replace("Rp", "Rp ").substringBeforeLast(",")
}