package com.example.jagaduit.ui.screens

import android.app.DatePickerDialog
import android.net.Uri
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CalendarToday
import androidx.compose.material.icons.filled.Image
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
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.jagaduit.data.JagaDuitDatabase
import com.example.jagaduit.ui.theme.JagaDuitTheme
import com.example.jagaduit.viewmodel.GoalViewModel
import com.example.jagaduit.viewmodel.GoalViewModelFactory
import java.util.Calendar

class NewGoalActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JagaDuitTheme {
                NewGoalContent(onFinish = { finish() })
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NewGoalContent(onFinish: () -> Unit) {
    val context = LocalContext.current
    val db = JagaDuitDatabase.getDatabase(context)
    val viewModel: GoalViewModel = viewModel(factory = GoalViewModelFactory(db.goalDao()))

    var name by remember { mutableStateOf("") }
    var target by remember { mutableStateOf("") }
    var dateInMillis by remember { mutableStateOf(System.currentTimeMillis()) }
    var imageUri by remember { mutableStateOf<Uri?>(null) }

    val launcher = rememberLauncherForActivityResult(contract = ActivityResultContracts.GetContent()) { uri ->
        imageUri = uri
    }

    val calendar = Calendar.getInstance()
    val datePickerDialog = DatePickerDialog(
        context,
        { _, year, month, day ->
            calendar.set(year, month, day)
            dateInMillis = calendar.timeInMillis
        },
        calendar.get(Calendar.YEAR),
        calendar.get(Calendar.MONTH),
        calendar.get(Calendar.DAY_OF_MONTH)
    )

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Buat Tujuan Baru", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = { onFinish() }) {
                        Icon(
                            imageVector = Icons.Default.ArrowBack,
                            contentDescription = "Kembali",
                            tint = Color.White
                        )
                    }
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
                .padding(16.dp)
        ) {
            // 1. Gambar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                    .clickable { launcher.launch("image/*") },
                contentAlignment = Alignment.Center
            ) {
                if (imageUri != null) {
                    AsyncImage(
                        model = imageUri,
                        contentDescription = null,
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Fit
                    )
                } else {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(Icons.Default.Image, contentDescription = null, tint = Color.Gray, modifier = Modifier.size(40.dp))
                        Text("Pilih Gambar ", color = Color.Gray)
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 2. Input Nama
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Nama") },
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 3. Input Target
            OutlinedTextField(
                value = target,
                onValueChange = { if (it.all { c -> c.isDigit() }) target = it },
                label = { Text("Target ") },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.White, unfocusedTextColor = Color.White
                )
            )

            Spacer(modifier = Modifier.height(8.dp))

            // 4. Input Tanggal
            OutlinedTextField(
                value = android.text.format.DateFormat.getDateFormat(context).format(dateInMillis),
                onValueChange = {},
                readOnly = true,
                label = { Text("Target Tercapai") },
                trailingIcon = { Icon(Icons.Default.CalendarToday, null, tint = Color.White) },
                modifier = Modifier.fillMaxWidth().clickable { datePickerDialog.show() },
                enabled = false,
                colors = OutlinedTextFieldDefaults.colors(
                    disabledTextColor = Color.White, disabledLabelColor = Color.Gray
                )
            )

            Spacer(modifier = Modifier.weight(1f))

            // 5. Tombol Simpan
            Button(
                onClick = {
                    if (name.isNotEmpty() && target.isNotEmpty()) {
                        viewModel.addGoal(
                            name = name,
                            target = target.toDouble(),
                            deadline = dateInMillis,
                            imagePath = imageUri?.toString()
                        )
                        Toast.makeText(context, "Goal Disimpan!", Toast.LENGTH_SHORT).show()
                        onFinish()
                    }
                },
                modifier = Modifier.fillMaxWidth().height(50.dp),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.secondary)
            ) {
                // --- PERUBAHAN DISINI (color = Color.Black) ---
                Text("MULAI MENABUNG", fontWeight = FontWeight.Bold, color = Color.Black)
            }
        }
    }
}