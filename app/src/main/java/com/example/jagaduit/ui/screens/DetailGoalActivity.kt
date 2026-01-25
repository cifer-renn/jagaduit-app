package com.example.jagaduit.ui.screens

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.History
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.jagaduit.data.GoalEntity
import com.example.jagaduit.data.JagaDuitDatabase
import com.example.jagaduit.ui.theme.JagaDuitTheme
import com.example.jagaduit.utils.toRupiah
import com.example.jagaduit.viewmodel.GoalViewModel
import com.example.jagaduit.viewmodel.GoalViewModelFactory
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class DetailGoalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val goalId = intent.getIntExtra("GOAL_ID", -1)
        setContent {
            JagaDuitTheme {
                DetailGoalContent(goalId = goalId, onBack = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailGoalContent(goalId: Int, onBack: () -> Unit) {
    val context = LocalContext.current
    val db = JagaDuitDatabase.getDatabase(context)
    val viewModel: GoalViewModel = viewModel(factory = GoalViewModelFactory(db.goalDao()))

    var goal by remember { mutableStateOf<GoalEntity?>(null) }
    var inputAmount by remember { mutableStateOf("") }

    // Ambil Data History
    val historyList by viewModel.getHistory(goalId).collectAsState(initial = emptyList())

    LaunchedEffect(goalId) {
        goal = viewModel.getGoalById(goalId)
    }

    if (goal == null) return

    val currentGoal = goal!!
    val progress = if (currentGoal.targetAmount > 0) (currentGoal.currentAmount / currentGoal.targetAmount).toFloat() else 0f
    val remaining = currentGoal.targetAmount - currentGoal.currentAmount

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(currentGoal.name, color = Color.White) },
                navigationIcon = { IconButton(onClick = onBack) { Icon(Icons.Default.ArrowBack, null, tint = Color.White) } },
                actions = {
                    IconButton(onClick = {
                        viewModel.deleteGoal(currentGoal)
                        onBack()
                    }) { Icon(Icons.Default.Delete, null, tint = Color.Red) }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = Color.Black)
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.Black)
                .verticalScroll(rememberScrollState()) // Scrollable
        ) {
            // Gambar Header
            if (currentGoal.imagePath != null) {
                AsyncImage(
                    model = currentGoal.imagePath,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().height(200.dp),
                    contentScale = ContentScale.Fit
                )
            } else {
                Box(Modifier.fillMaxWidth().height(200.dp).background(Color.DarkGray), contentAlignment = Alignment.Center) {
                    Text("No Image", color = Color.White)
                }
            }

            Column(modifier = Modifier.padding(16.dp)) {
                // Info Status
                Card(
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Terkumpul", color = Color.Gray)
                        Text(currentGoal.currentAmount.toRupiah(), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        Spacer(modifier = Modifier.height(8.dp))
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(10.dp),
                            color = if (currentGoal.isAchieved) Color.Green else MaterialTheme.colorScheme.secondary,
                            trackColor = Color.DarkGray,
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Row(horizontalArrangement = Arrangement.SpaceBetween, modifier = Modifier.fillMaxWidth()) {
                            Text("Target: ${currentGoal.targetAmount.toRupiah()}", color = Color.Gray, fontSize = 12.sp)
                            Text("Kurang: ${if(remaining > 0) remaining.toRupiah() else "Lunas!"}", color = if(remaining>0) Color.Red else Color.Green, fontSize = 12.sp)
                        }
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // Input Nabung
                Text("Tambah Tabungan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(8.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = inputAmount,
                        onValueChange = { if (it.all { c -> c.isDigit() }) inputAmount = it },
                        label = { Text("Nominal (Rp)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.weight(1f),
                        colors = OutlinedTextFieldDefaults.colors(focusedTextColor = Color.White, unfocusedTextColor = Color.White)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            val amount = inputAmount.toDoubleOrNull()
                            if (amount != null && amount > 0) {
                                viewModel.saveDeposit(currentGoal, amount)
                                // Refresh Data Lokal
                                goal = currentGoal.copy(currentAmount = currentGoal.currentAmount + amount)
                                inputAmount = ""
                                Toast.makeText(context, "Berhasil ditabung!", Toast.LENGTH_SHORT).show()
                            }
                        },
                        shape = RoundedCornerShape(8.dp),
                        modifier = Modifier.height(56.dp),
                        colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
                    ) {
                        Text("TABUNG", color = Color.Black, fontWeight = FontWeight.Bold)
                    }
                }

                Spacer(modifier = Modifier.height(32.dp))

                // --- BAGIAN HISTORY ---
                Divider(color = Color.DarkGray)
                Spacer(modifier = Modifier.height(16.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.History, contentDescription = null, tint = Color.Gray)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Riwayat Uang Masuk", color = Color.Gray, fontWeight = FontWeight.Bold, fontSize = 16.sp)
                }
                Spacer(modifier = Modifier.height(16.dp))

                if (historyList.isEmpty()) {
                    Text("Belum ada riwayat menabung.", color = Color.DarkGray, fontSize = 14.sp)
                } else {
                    // Loop untuk menampilkan list history
                    historyList.forEach { history ->
                        val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale("id", "ID"))
                        val timeFormat = SimpleDateFormat("HH:mm", Locale("id", "ID"))
                        val dateString = dateFormat.format(Date(history.timestamp))
                        val timeString = timeFormat.format(Date(history.timestamp))

                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(vertical = 8.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(dateString, color = Color.White, fontWeight = FontWeight.Bold)
                                Text("Pukul $timeString", color = Color.Gray, fontSize = 12.sp)
                            }
                            Text(
                                "+ ${history.amount.toRupiah()}",
                                color = MaterialTheme.colorScheme.secondary,
                                fontWeight = FontWeight.Bold
                            )
                        }
                        Divider(color = Color(0xFF2C2C2C))
                    }
                }
                Spacer(modifier = Modifier.height(50.dp)) // Space bawah
            }
        }
    }
}