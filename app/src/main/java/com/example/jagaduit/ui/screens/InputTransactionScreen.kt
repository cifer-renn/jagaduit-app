package com.example.jagaduit.ui.screens // <-- SAYA UBAH INI JADI ADA 's' AGAR KETEMU DI MAINACTIVITY

import android.app.Application
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jagaduit.data.JagaDuitDatabase
import com.example.jagaduit.viewmodel.AccountViewModel
import com.example.jagaduit.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

// SAYA TAMBAHKAN FACTORY DI SINI SUPAYA TIDAK ERROR "UNRESOLVED REFERENCE"
// (Ini hanya logika penyambung data, tidak mengubah tampilan UI)
class LocalAccountViewModelFactory(private val dao: com.example.jagaduit.data.AccountDao) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(AccountViewModel::class.java)) {
            return AccountViewModel(dao) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun InputTransactionScreen(
    navController: NavController,
    txnId: Int,
    scannedAmount: String = "",
    scannedCategory: String = "",
    scannedDate: String = "",
    viewModel: TransactionViewModel = viewModel() // Transaction VM
) {
    val context = LocalContext.current
    val application = context.applicationContext as Application

    // 1. SETUP ACCOUNT VIEWMODEL
    val db = JagaDuitDatabase.getDatabase(context)

    // Perbaikan: Menggunakan Factory Lokal yang saya buat di atas agar aman
    val accViewModel: AccountViewModel = viewModel(
        factory = LocalAccountViewModelFactory(db.accountDao())
    )

    // Ambil list akun untuk dropdown & logika update saldo
    val accountList by accViewModel.allAccounts.collectAsState(initial = emptyList())

    // 2. STATE VARIABELS UI
    var selectedTab by remember { mutableIntStateOf(0) } // 0=Income, 1=Expense, 2=Transfer
    val tabs = listOf("Income", "Expense", "Transfer")

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedAccountFrom by remember { mutableStateOf("") }
    var selectedAccountTo by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

    // Helper State
    var isEditing by remember { mutableStateOf(false) }

    // --- LOGIC 1: JIKA DATA DARI HASIL SCAN (AI) ---
    LaunchedEffect(key1 = scannedAmount) {
        if (scannedAmount.isNotEmpty() && txnId == -1) {
            // Bersihkan format (misal "Rp 50.000" jadi "50000")
            amount = scannedAmount.replace(Regex("[^0-9]"), "")

            // Default ke Tab Expense (Biasanya struk itu pengeluaran)
            selectedTab = 1

            // Isi Kategori jika ada
            if (scannedCategory.isNotEmpty()) {
                selectedCategory = scannedCategory
            }

            // Isi Tanggal jika ada
            if (scannedDate.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = sdf.parse(scannedDate)
                    if (date != null) {
                        selectedDate = date.time
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        }
    }

    // --- LOGIC 2: JIKA MODE EDIT (Manual) ---
    LaunchedEffect(key1 = txnId) {
        if (txnId != -1) {
            isEditing = true
            val txn = viewModel.getTransactionById(txnId)
            if (txn != null) {
                amount = txn.amount.toLong().toString()
                note = txn.note
                selectedDate = txn.date
                selectedAccountFrom = txn.accountFrom

                when (txn.type) {
                    "INCOME" -> {
                        selectedTab = 0
                        selectedCategory = txn.category
                    }
                    "EXPENSE" -> {
                        selectedTab = 1
                        selectedCategory = txn.category
                    }
                    "TRANSFER" -> {
                        selectedTab = 2
                        selectedAccountTo = txn.accountTo ?: ""
                    }
                }
            }
        }
    }

    // --- DATA DROPDOWN ---
    val categoryType = if (selectedTab == 0) "INCOME" else "EXPENSE"
    val categoryList by viewModel.getCategories(categoryType).collectAsState(initial = emptyList())

    var expandedCategory by remember { mutableStateOf(false) }
    var expandedAccountFrom by remember { mutableStateOf(false) }
    var expandedAccountTo by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (txnId != -1) "Edit Transaction" else "Add Transaction", fontWeight = FontWeight.Bold) },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = MaterialTheme.colorScheme.primary)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(MaterialTheme.colorScheme.background)
                .padding(16.dp)
        ) {
            // TABS
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.secondary,
                indicator = { tabPositions ->
                    TabRowDefaults.Indicator(
                        modifier = Modifier.tabIndicatorOffset(tabPositions[selectedTab]),
                        color = MaterialTheme.colorScheme.secondary
                    )
                }
            ) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = {
                            selectedTab = index
                            if (txnId == -1) selectedCategory = ""
                        },
                        text = { Text(title, fontWeight = if (selectedTab == index) FontWeight.Bold else FontWeight.Normal) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(24.dp))

            // DATE INPUT
            OutlinedTextField(
                value = SimpleDateFormat("dd MMMM yyyy", Locale.getDefault()).format(Date(selectedDate)),
                onValueChange = {},
                readOnly = true,
                label = { Text("Date") },
                trailingIcon = {
                    IconButton(onClick = { showDatePicker = true }) {
                        Icon(Icons.Default.CalendarToday, contentDescription = "Select Date")
                    }
                },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // AMOUNT INPUT
            OutlinedTextField(
                value = amount,
                onValueChange = { if (it.all { char -> char.isDigit() }) amount = it },
                label = { Text("Amount (Rp)") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            // DYNAMIC FORM FIELDS
            if (selectedTab != 2) {
                // INCOME & EXPENSE
                ExposedDropdownMenuBox(
                    expanded = expandedCategory,
                    onExpandedChange = { expandedCategory = !expandedCategory }
                ) {
                    OutlinedTextField(
                        value = selectedCategory,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Category") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedCategory) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedCategory,
                        onDismissRequest = { expandedCategory = false }
                    ) {
                        categoryList.forEach { cat ->
                            DropdownMenuItem(
                                text = { Text(cat.name) },
                                onClick = {
                                    selectedCategory = cat.name
                                    expandedCategory = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedAccountFrom,
                    onExpandedChange = { expandedAccountFrom = !expandedAccountFrom }
                ) {
                    OutlinedTextField(
                        value = selectedAccountFrom,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccountFrom) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAccountFrom,
                        onDismissRequest = { expandedAccountFrom = false }
                    ) {
                        accountList.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    selectedAccountFrom = acc.name
                                    expandedAccountFrom = false
                                }
                            )
                        }
                    }
                }

            } else {
                // TRANSFER
                ExposedDropdownMenuBox(
                    expanded = expandedAccountFrom,
                    onExpandedChange = { expandedAccountFrom = !expandedAccountFrom }
                ) {
                    OutlinedTextField(
                        value = selectedAccountFrom,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("From Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccountFrom) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAccountFrom,
                        onDismissRequest = { expandedAccountFrom = false }
                    ) {
                        accountList.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    selectedAccountFrom = acc.name
                                    expandedAccountFrom = false
                                }
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                ExposedDropdownMenuBox(
                    expanded = expandedAccountTo,
                    onExpandedChange = { expandedAccountTo = !expandedAccountTo }
                ) {
                    OutlinedTextField(
                        value = selectedAccountTo,
                        onValueChange = {},
                        readOnly = true,
                        label = { Text("To Account") },
                        trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expandedAccountTo) },
                        modifier = Modifier.menuAnchor().fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = expandedAccountTo,
                        onDismissRequest = { expandedAccountTo = false }
                    ) {
                        accountList.filter { it.name != selectedAccountFrom }.forEach { acc ->
                            DropdownMenuItem(
                                text = { Text(acc.name) },
                                onClick = {
                                    selectedAccountTo = acc.name
                                    expandedAccountTo = false
                                }
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // NOTE INPUT
            OutlinedTextField(
                value = note,
                onValueChange = { note = it },
                label = { Text("Note (Optional)") },
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(32.dp))

            // SAVE BUTTON
            Button(
                onClick = {
                    if (amount.isNotEmpty() && selectedAccountFrom.isNotEmpty()) {
                        val typeStr = when(selectedTab) {
                            0 -> "INCOME"
                            1 -> "EXPENSE"
                            else -> "TRANSFER"
                        }

                        val amountVal = amount.toDoubleOrNull() ?: 0.0

                        // --- LOGIC UPDATE SALDO DOMPET ---
                        // (Hanya jalan jika Transaksi Baru, bukan Edit)
                        if (txnId == -1) {
                            // Helper function local untuk update saldo di list
                            fun updateBalance(accountName: String, diff: Double) {
                                val acc = accountList.find { it.name == accountName }
                                if (acc != null) {
                                    val newBalance = acc.balance + diff
                                    accViewModel.updateAccount(acc.copy(balance = newBalance))
                                }
                            }

                            when (typeStr) {
                                "INCOME" -> {
                                    // Uang Masuk -> Tambah Saldo
                                    updateBalance(selectedAccountFrom, amountVal)
                                }
                                "EXPENSE" -> {
                                    // Uang Keluar -> Kurang Saldo
                                    updateBalance(selectedAccountFrom, -amountVal)
                                }
                                "TRANSFER" -> {
                                    if (selectedAccountTo.isNotEmpty()) {
                                        // Transfer -> Asal Berkurang, Tujuan Bertambah
                                        updateBalance(selectedAccountFrom, -amountVal)
                                        updateBalance(selectedAccountTo, amountVal)
                                    }
                                }
                            }
                        }

                        // SIMPAN TRANSAKSI
                        viewModel.saveTransaction(
                            id = if (txnId == -1) 0 else txnId,
                            date = selectedDate,
                            amount = amountVal,
                            type = typeStr,
                            category = if (selectedTab == 2) "Transfer" else selectedCategory,
                            accountFrom = selectedAccountFrom,
                            accountTo = if (selectedTab == 2) selectedAccountTo else null,
                            note = note
                        )

                        val msg = if (txnId == -1) "Transaction Saved!" else "Transaction Updated!"
                        Toast.makeText(context, msg, Toast.LENGTH_SHORT).show()
                        navController.popBackStack()
                    } else {
                        Toast.makeText(context, "Please fill required fields", Toast.LENGTH_SHORT).show()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text(if (txnId == -1) "Save Transaction" else "Update Transaction", fontSize = 18.sp, fontWeight = FontWeight.Bold)
            }
        }
    }

    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    dateState.selectedDateMillis?.let { selectedDate = it }
                    showDatePicker = false
                }) { Text("OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text("Cancel") }
            }
        ) {
            DatePicker(state = dateState)
        }
    }
}