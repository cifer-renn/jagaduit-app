package com.example.jagaduit.ui.screens

import android.app.Application
import android.graphics.BitmapFactory
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jagaduit.data.JagaDuitDatabase
import com.example.jagaduit.utils.saveBitmapToStorage
import com.example.jagaduit.utils.saveUriToStorage
import com.example.jagaduit.viewmodel.AccountViewModel
import com.example.jagaduit.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

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
    scannedImagePath: String = "",
    viewModel: TransactionViewModel = viewModel()
) {
    val context = LocalContext.current

    // 1. SETUP ACCOUNT VIEWMODEL
    val db = JagaDuitDatabase.getDatabase(context)
    val accViewModel: AccountViewModel = viewModel(
        factory = LocalAccountViewModelFactory(db.accountDao())
    )
    val accountList by accViewModel.allAccounts.collectAsState(initial = emptyList())

    // 2. STATE VARIABELS UI
    var selectedTab by remember { mutableIntStateOf(1) }
    val tabs = listOf("Income", "Expense", "Transfer")

    var amount by remember { mutableStateOf("") }
    var note by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf("") }
    var selectedAccountFrom by remember { mutableStateOf("") }
    var selectedAccountTo by remember { mutableStateOf("") }
    var selectedDate by remember { mutableLongStateOf(System.currentTimeMillis()) }

    // STATE GAMBAR
    var currentImagePath by remember { mutableStateOf<String?>(null) }
    var showImageSourceDialog by remember { mutableStateOf(false) }
    // UPDATE: State untuk Zoom Gambar
    var showFullImageDialog by remember { mutableStateOf(false) }

    var showDatePicker by remember { mutableStateOf(false) }
    val dateState = rememberDatePickerState(initialSelectedDateMillis = selectedDate)

    // Helper State
    var isEditing by remember { mutableStateOf(false) }

    // --- LAUNCHERS ---
    val galleryLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
        uri?.let { currentImagePath = saveUriToStorage(context, it) }
    }
    val cameraLauncher = rememberLauncherForActivityResult(ActivityResultContracts.TakePicturePreview()) { bitmap ->
        bitmap?.let { currentImagePath = saveBitmapToStorage(context, it) }
    }

    // --- LOGIC 1: DATA DARI SCANNER ---
    LaunchedEffect(key1 = scannedAmount) {
        if (scannedAmount.isNotEmpty() && txnId == -1) {
            amount = scannedAmount.replace(Regex("[^0-9]"), "")
            selectedTab = 1
            if (scannedCategory.isNotEmpty()) selectedCategory = scannedCategory

            if (scannedDate.isNotEmpty()) {
                try {
                    val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                    val date = sdf.parse(scannedDate)
                    if (date != null) selectedDate = date.time
                } catch (e: Exception) { e.printStackTrace() }
            }

            if (scannedImagePath.isNotEmpty()) {
                currentImagePath = scannedImagePath
            }
        }
    }

    // --- LOGIC 2: MODE EDIT ---
    LaunchedEffect(key1 = txnId) {
        if (txnId != -1) {
            isEditing = true
            val txn = viewModel.getTransactionById(txnId)
            if (txn != null) {
                amount = txn.amount.toLong().toString()
                note = txn.note
                selectedDate = txn.date
                selectedAccountFrom = txn.accountFrom
                currentImagePath = txn.imagePath

                when (txn.type) {
                    "INCOME" -> { selectedTab = 0; selectedCategory = txn.category }
                    "EXPENSE" -> { selectedTab = 1; selectedCategory = txn.category }
                    "TRANSFER" -> { selectedTab = 2; selectedAccountTo = txn.accountTo ?: "" }
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
                            DropdownMenuItem(text = { Text(cat.name) }, onClick = { selectedCategory = cat.name; expandedCategory = false })
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
                            DropdownMenuItem(text = { Text(acc.name) }, onClick = { selectedAccountFrom = acc.name; expandedAccountFrom = false })
                        }
                    }
                }

            } else {
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
                            DropdownMenuItem(text = { Text(acc.name) }, onClick = { selectedAccountFrom = acc.name; expandedAccountFrom = false })
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
                            DropdownMenuItem(text = { Text(acc.name) }, onClick = { selectedAccountTo = acc.name; expandedAccountTo = false })
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
                modifier = Modifier.fillMaxWidth(),
                trailingIcon = {
                    IconButton(onClick = { showImageSourceDialog = true }) {
                        Icon(Icons.Default.CameraAlt, contentDescription = "Attach Image", tint = MaterialTheme.colorScheme.primary)
                    }
                }
            )

            // --- IMAGE PREVIEW THUMBNAIL ---
            if (currentImagePath != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Box(
                    modifier = Modifier
                        .size(100.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .border(1.dp, Color.Gray, RoundedCornerShape(8.dp))
                        // UPDATE: Agar bisa diklik untuk Zoom
                        .clickable { showFullImageDialog = true }
                ) {
                    val bitmap = BitmapFactory.decodeFile(currentImagePath)
                    if (bitmap != null) {
                        Image(
                            bitmap = bitmap.asImageBitmap(),
                            contentDescription = "Attachment",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    }
                    // Tombol Hapus (Z-Index paling atas)
                    Box(
                        modifier = Modifier
                            .align(Alignment.TopEnd)
                            .padding(4.dp)
                            .size(24.dp)
                            .background(Color.Red, CircleShape)
                            .clickable { currentImagePath = null },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Default.Close, contentDescription = "Remove", tint = Color.White, modifier = Modifier.size(16.dp))
                    }
                }
            }

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

                        // Logic Update Saldo
                        if (txnId == -1) {
                            fun updateBalance(accountName: String, diff: Double) {
                                val acc = accountList.find { it.name == accountName }
                                if (acc != null) {
                                    val newBalance = acc.balance + diff
                                    accViewModel.updateAccount(acc.copy(balance = newBalance))
                                }
                            }

                            when (typeStr) {
                                "INCOME" -> updateBalance(selectedAccountFrom, amountVal)
                                "EXPENSE" -> updateBalance(selectedAccountFrom, -amountVal)
                                "TRANSFER" -> {
                                    if (selectedAccountTo.isNotEmpty()) {
                                        updateBalance(selectedAccountFrom, -amountVal)
                                        updateBalance(selectedAccountTo, amountVal)
                                    }
                                }
                            }
                        }

                        // Simpan
                        viewModel.saveTransaction(
                            id = if (txnId == -1) 0 else txnId,
                            date = selectedDate,
                            amount = amountVal,
                            type = typeStr,
                            category = if (selectedTab == 2) "Transfer" else selectedCategory,
                            accountFrom = selectedAccountFrom,
                            accountTo = if (selectedTab == 2) selectedAccountTo else null,
                            note = note,
                            imagePath = currentImagePath
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

    // DIALOG SUMBER GAMBAR (Kamera/Galeri)
    if (showImageSourceDialog) {
        AlertDialog(
            onDismissRequest = { showImageSourceDialog = false },
            title = { Text("Add Photo") },
            text = { Text("Choose source") },
            confirmButton = {
                TextButton(onClick = { showImageSourceDialog = false; cameraLauncher.launch() }) { Text("Camera") }
            },
            dismissButton = {
                TextButton(onClick = { showImageSourceDialog = false; galleryLauncher.launch("image/*") }) { Text("Gallery") }
            }
        )
    }

    // UPDATE: DIALOG FULL SCREEN IMAGE
    if (showFullImageDialog && currentImagePath != null) {
        Dialog(onDismissRequest = { showFullImageDialog = false }) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .wrapContentHeight() // Menyesuaikan tinggi gambar
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.Black)
            ) {
                val bitmap = BitmapFactory.decodeFile(currentImagePath)
                if (bitmap != null) {
                    Image(
                        bitmap = bitmap.asImageBitmap(),
                        contentDescription = "Full Image",
                        modifier = Modifier.fillMaxWidth(),
                        contentScale = ContentScale.Fit // Agar seluruh gambar terlihat
                    )
                }

                // Tombol Close di pojok kanan atas dialog
                IconButton(
                    onClick = { showFullImageDialog = false },
                    modifier = Modifier.align(Alignment.TopEnd)
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close",
                        tint = Color.White
                    )
                }
            }
        }
    }

    // DATE PICKER DIALOG
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = { dateState.selectedDateMillis?.let { selectedDate = it }; showDatePicker = false }) { Text("OK") }
            },
            dismissButton = { TextButton(onClick = { showDatePicker = false }) { Text("Cancel") } }
        ) { DatePicker(state = dateState) }
    }
}