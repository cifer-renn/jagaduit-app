package com.example.jagaduit.ui.screens

// --- BAGIAN INI SANGAT PENTING (IMPORT) ---
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.example.jagaduit.data.GoalEntity
import com.example.jagaduit.data.JagaDuitDatabase
import com.example.jagaduit.utils.toRupiah
import com.example.jagaduit.viewmodel.GoalViewModel
import com.example.jagaduit.viewmodel.GoalViewModelFactory

@Composable
fun GoalScreen() {
    val context = LocalContext.current
    val db = JagaDuitDatabase.getDatabase(context)
    val viewModel: GoalViewModel = viewModel(factory = GoalViewModelFactory(db.goalDao()))

    val goals by viewModel.allGoals.collectAsState(initial = emptyList())
    val sortedGoals = goals.sortedByDescending { it.isFavorite }

    Scaffold(
        floatingActionButton = {
            FloatingActionButton(
                onClick = {
                    val intent = Intent(context, NewGoalActivity::class.java)
                    context.startActivity(intent)
                },
                containerColor = MaterialTheme.colorScheme.secondary
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Goal", tint = Color.Black)
            }
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .padding(padding)
                .fillMaxSize()
                .background(Color.Black)
                .padding(horizontal = 16.dp)
        ) {
            // ITEM 1: JUDUL
            item {
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    "Saving Goals",
                    color = Color.White,
                    fontSize = 24.sp,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(16.dp))
            }

            // ITEM 2: LOGIKA LIST / KOSONG
            if (sortedGoals.isEmpty()) {
                item {
                    Box(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(300.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Belum ada goal. Ayo buat mimpi!", color = Color.Gray)
                    }
                }
            } else {
                // LIST GOAL
                items(sortedGoals) { goal ->
                    GoalItem(
                        goal = goal,
                        onClick = {
                            val intent = Intent(context, DetailGoalActivity::class.java)
                            intent.putExtra("GOAL_ID", goal.id)
                            context.startActivity(intent)
                        },
                        onDelete = { viewModel.deleteGoal(goal) },
                        onToggleFavorite = {
                            // Update favorite
                            val updatedGoal = goal.copy(isFavorite = !goal.isFavorite)
                            viewModel.updateGoal(updatedGoal)
                        }
                    )
                }

                // ITEM 3: SPACER BAWAH
                item {
                    Spacer(modifier = Modifier.height(80.dp))
                }
            }
        }
    }
}

@Composable
fun GoalItem(
    goal: GoalEntity,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onToggleFavorite: () -> Unit
) {
    val progress = if (goal.targetAmount > 0) (goal.currentAmount / goal.targetAmount).toFloat() else 0f
    // HITUNG PERSENTASE
    val percentage = if (goal.targetAmount > 0) ((goal.currentAmount / goal.targetAmount) * 100).toInt() else 0

    var showMenu by remember { mutableStateOf(false) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E))
    ) {
        Row(modifier = Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
            // Gambar
            if (goal.imagePath != null) {
                AsyncImage(
                    model = goal.imagePath,
                    contentDescription = null,
                    modifier = Modifier.size(60.dp).background(Color.Gray, RoundedCornerShape(8.dp)),
                    contentScale = ContentScale.Crop
                )
            } else {
                Box(
                    modifier = Modifier.size(60.dp).background(Color.DarkGray, RoundedCornerShape(8.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Text("📷", fontSize = 24.sp)
                }
            }
            Spacer(modifier = Modifier.width(16.dp))

            // Info Goal
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (goal.isFavorite) {
                        Icon(Icons.Default.Favorite, contentDescription = null, tint = Color.Red, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                    }
                    Text(goal.name, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp)
                }

                // Baris Saldo & Persentase
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        "${goal.currentAmount.toRupiah()} / ${goal.targetAmount.toRupiah()}",
                        color = Color.Gray,
                        fontSize = 10.sp
                    )
                    Text(
                        "$percentage%",
                        color = MaterialTheme.colorScheme.secondary,
                        fontWeight = FontWeight.Bold,
                        fontSize = 12.sp
                    )
                }

                Spacer(modifier = Modifier.height(4.dp))
                LinearProgressIndicator(
                    progress = { progress },
                    modifier = Modifier.fillMaxWidth().height(8.dp),
                    color = if (goal.isAchieved) Color.Green else MaterialTheme.colorScheme.secondary,
                    trackColor = Color.DarkGray,
                )
            }

            // Menu Titik Tiga
            Box {
                IconButton(onClick = { showMenu = true }) {
                    Icon(Icons.Default.MoreVert, contentDescription = "Menu", tint = Color.Gray)
                }
                DropdownMenu(expanded = showMenu, onDismissRequest = { showMenu = false }) {
                    DropdownMenuItem(
                        text = { Text(if (goal.isFavorite) "Hapus Favorit" else "Jadikan Favorit") },
                        leadingIcon = {
                            Icon(
                                imageVector = if (goal.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                contentDescription = null,
                                tint = if (goal.isFavorite) Color.Red else Color.Black
                            )
                        },
                        onClick = {
                            onToggleFavorite()
                            showMenu = false
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Hapus Goal", color = Color.Red) },
                        leadingIcon = { Icon(Icons.Default.Delete, contentDescription = null, tint = Color.Red) },
                        onClick = {
                            onDelete()
                            showMenu = false
                        }
                    )
                }
            }
        }
    }
}