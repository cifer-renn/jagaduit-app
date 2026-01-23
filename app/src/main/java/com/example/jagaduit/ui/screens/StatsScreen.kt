package com.example.jagaduit.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBackIosNew
import androidx.compose.material.icons.filled.ArrowForwardIos
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.example.jagaduit.ui.components.MonthYearPickerDialog
import com.example.jagaduit.ui.theme.ChartColors
import com.example.jagaduit.utils.toRupiah
import com.example.jagaduit.viewmodel.TransactionViewModel
import java.text.SimpleDateFormat
import java.util.*

@Composable
fun StatsScreen(
    navController: NavController,
    viewModel: TransactionViewModel = viewModel()
) {
    val allTransactions by viewModel.transactionList.collectAsState(initial = emptyList())

    var currentMonthOffset by remember { mutableIntStateOf(0) }
    val calendar = Calendar.getInstance()
    calendar.add(Calendar.MONTH, currentMonthOffset)

    val currentMonthName = SimpleDateFormat("MMM yyyy", Locale.US).format(calendar.time)

    var showMonthPicker by remember { mutableStateOf(false) }

    var isExpense by remember { mutableStateOf(true) }
    val targetType = if (isExpense) "EXPENSE" else "INCOME"

    val filteredTransactions = allTransactions.filter { txn ->
        val txnCal = Calendar.getInstance()
        txnCal.timeInMillis = txn.date
        txnCal.get(Calendar.MONTH) == calendar.get(Calendar.MONTH) &&
                txnCal.get(Calendar.YEAR) == calendar.get(Calendar.YEAR) &&
                txn.type == targetType
    }

    val groupedData = filteredTransactions
        .groupBy { it.category }
        .mapValues { entry -> entry.value.sumOf { it.amount } }
        .toList()
        .sortedByDescending { it.second }

    val totalAmount = groupedData.sumOf { it.second }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp)
    ) {
        // --- HEADER: < Bulan > (Kiri) & Edit (Kanan) ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 2.dp), // Padding vertikal agar sejajar
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            // BLOCK KIRI: < Bulan > Kompak
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = { currentMonthOffset-- }) {
                    Icon(Icons.Default.ArrowBackIosNew, contentDescription = "Prev", tint = Color.White, modifier = Modifier.size(20.dp))
                }

                Text(
                    text = currentMonthName,
                    style = MaterialTheme.typography.titleMedium, // Ukuran kecil
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.secondary,
                    modifier = Modifier
                        .clickable { showMonthPicker = true }
                        .padding(horizontal = 8.dp)
                )

                IconButton(onClick = { currentMonthOffset++ }) {
                    Icon(Icons.Default.ArrowForwardIos, contentDescription = "Next", tint = Color.White, modifier = Modifier.size(20.dp))
                }
            }

            // BLOCK KANAN: Edit Pencil
            IconButton(onClick = { navController.navigate("manage_category") }) {
                Icon(Icons.Default.Edit, contentDescription = "Manage", tint = Color.White)
            }
        }

        Spacer(modifier = Modifier.height(2.dp))

        // --- TOGGLE BUTTON ---
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(45.dp)
                .background(Color(0xFF1E1E1E), RoundedCornerShape(12.dp))
                .padding(4.dp)
        ) {
            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (isExpense) MaterialTheme.colorScheme.secondary else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { isExpense = true },
                contentAlignment = Alignment.Center
            ) { Text("Expense", fontWeight = FontWeight.Bold, color = if (isExpense) Color.Black else Color.Gray) }

            Box(
                modifier = Modifier
                    .weight(1f)
                    .fillMaxHeight()
                    .background(if (!isExpense) MaterialTheme.colorScheme.secondary else Color.Transparent, RoundedCornerShape(8.dp))
                    .clickable { isExpense = false },
                contentAlignment = Alignment.Center
            ) { Text("Income", fontWeight = FontWeight.Bold, color = if (!isExpense) Color.Black else Color.Gray) }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // --- CHART ---
        if (totalAmount > 0) {
            Box(
                modifier = Modifier.fillMaxWidth(),
                contentAlignment = Alignment.Center
            ) {
                DonutChart(groupedData, totalAmount)
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("Total", color = Color.Gray, fontSize = 12.sp)
                    Text(text = totalAmount.toRupiah(), fontWeight = FontWeight.Bold, color = Color.White, fontSize = 16.sp)
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            LazyColumn {
                itemsIndexed(groupedData) { index, item ->
                    val color = ChartColors[index % ChartColors.size]
                    val percentage = (item.second / totalAmount * 100).toInt()
                    CategoryStatItem(name = item.first, amount = item.second, percentage = percentage, color = color)
                }
            }
        } else {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                Text("No Data for this month", color = Color.Gray)
            }
        }
    }

    // --- DIALOG PICKER ---
    MonthYearPickerDialog(
        visible = showMonthPicker,
        currentMonth = calendar.get(Calendar.MONTH),
        currentYear = calendar.get(Calendar.YEAR),
        onDismiss = { showMonthPicker = false },
        onDateSelected = { month, year ->
            val today = Calendar.getInstance()
            val target = Calendar.getInstance()
            target.set(Calendar.YEAR, year)
            target.set(Calendar.MONTH, month)
            val diffYear = target.get(Calendar.YEAR) - today.get(Calendar.YEAR)
            val diffMonth = target.get(Calendar.MONTH) - today.get(Calendar.MONTH)
            currentMonthOffset = (diffYear * 12) + diffMonth
        }
    )
}

@Composable
fun DonutChart(data: List<Pair<String, Double>>, total: Double) {
    Canvas(modifier = Modifier.size(200.dp)) {
        val strokeWidth = 30.dp.toPx()
        var startAngle = -90f
        data.forEachIndexed { index, entry ->
            val sweepAngle = (entry.second / total * 360).toFloat()
            val color = ChartColors[index % ChartColors.size]
            drawArc(
                color = color,
                startAngle = startAngle,
                sweepAngle = sweepAngle,
                useCenter = false,
                topLeft = Offset(strokeWidth / 2, strokeWidth / 2),
                size = Size(size.width - strokeWidth, size.height - strokeWidth),
                style = Stroke(width = strokeWidth)
            )
            startAngle += sweepAngle
        }
    }
}

@Composable
fun CategoryStatItem(name: String, amount: Double, percentage: Int, color: Color) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        Box(modifier = Modifier.size(12.dp).clip(CircleShape).background(color))
        Spacer(modifier = Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(text = name, color = Color.White, fontWeight = FontWeight.Medium)
            Text(text = "$percentage%", color = Color.Gray, fontSize = 12.sp)
        }
        Text(text = amount.toRupiah(), color = Color.White, fontWeight = FontWeight.Bold)
    }
}