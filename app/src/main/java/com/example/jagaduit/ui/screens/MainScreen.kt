package com.example.jagaduit.ui.screens

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AccountBalanceWallet
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Flag
import androidx.compose.material.icons.filled.List
import androidx.compose.material.icons.filled.PieChart
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.navigation.NavController // Pastikan import ini ada
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController

data class BottomNavItem(
    val route: String,
    val icon: ImageVector,
    val label: String
)

@Composable
// terima paramater rootNavController dari main activity
fun MainScreen(rootNavController: NavController) {

    // Ini navController buat yang bawah
    val bottomNavController = rememberNavController()

    val items = listOf(
        BottomNavItem("transaction", Icons.Default.List, "Trans"),
        BottomNavItem("stats", Icons.Default.PieChart, "Stats"),
        BottomNavItem("scan", Icons.Default.CameraAlt, "Scan"),
        BottomNavItem("account", Icons.Default.AccountBalanceWallet, "Acc"),
        BottomNavItem("goal", Icons.Default.Flag, "Goal")
    )

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.Black) {
                val navBackStackEntry by bottomNavController.currentBackStackEntryAsState()
                val currentRoute = navBackStackEntry?.destination?.route

                items.forEach { item ->
                    val isSelected = currentRoute == item.route
                    NavigationBarItem(
                        icon = {
                            Icon(
                                imageVector = item.icon,
                                contentDescription = item.label,
                                tint = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Gray
                            )
                        },
                        label = {
                            Text(
                                text = item.label,
                                color = if (isSelected) MaterialTheme.colorScheme.secondary else Color.Gray
                            )
                        },
                        selected = isSelected,
                        onClick = {
                            bottomNavController.navigate(item.route) {
                                popUpTo(bottomNavController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        },
                        colors = NavigationBarItemDefaults.colors(indicatorColor = Color.Transparent)
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = bottomNavController,
            startDestination = "transaction",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("transaction") { TransactionScreen(rootNavController) }

            composable("stats") { StatsScreen(rootNavController) }
            composable("scan") { ScanScreen(rootNavController) }
            composable("account") { AccountScreen() }
            composable("goal") { GoalScreen() }
        }
    }
}