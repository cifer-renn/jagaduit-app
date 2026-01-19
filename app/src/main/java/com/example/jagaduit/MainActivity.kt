package com.example.jagaduit

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.jagaduit.ui.screens.ForgotPasswordScreen
import com.example.jagaduit.ui.screens.InputTransactionScreen
import com.example.jagaduit.ui.screens.LoginScreen
import com.example.jagaduit.ui.screens.MainScreen
import com.example.jagaduit.ui.screens.ManageCategoryScreen
import com.example.jagaduit.ui.screens.SplashScreen
import com.example.jagaduit.ui.theme.JagaDuitTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            JagaDuitTheme {
                // Navigasi Utama
                val rootNavController = rememberNavController()

                NavHost(navController = rootNavController, startDestination = "splash") {

                    composable("splash") {
                        SplashScreen(rootNavController)
                    }
                    composable("login") {
                        LoginScreen(rootNavController)
                    }
                    composable("forgot_password") {
                        ForgotPasswordScreen(rootNavController)
                    }

                    composable("main") {
                        MainScreen(rootNavController)
                    }

                    composable(
                        // Rute menerima banyak parameter opsional
                        route = "input_transaction?txnId={txnId}&amount={amount}&category={category}&dateStr={dateStr}&imagePath={imagePath}",
                        arguments = listOf(
                            navArgument("txnId") {
                                type = NavType.IntType
                                defaultValue = -1
                            },
                            navArgument("amount") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                            navArgument("category") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                            navArgument("dateStr") {
                                type = NavType.StringType
                                defaultValue = ""
                            },
                            navArgument("imagePath") {
                                type = androidx.navigation.NavType.StringType
                                defaultValue = ""
                            }
                        )
                    ) { backStackEntry ->
                        val txnId = backStackEntry.arguments?.getInt("txnId") ?: -1
                        val amount = backStackEntry.arguments?.getString("amount") ?: ""
                        val category = backStackEntry.arguments?.getString("category") ?: ""
                        val dateStr = backStackEntry.arguments?.getString("dateStr") ?: ""
                        val imagePath = backStackEntry.arguments?.getString("imagePath") ?: ""

                        InputTransactionScreen(
                            navController = rootNavController,
                            txnId = txnId,
                            scannedAmount = amount,
                            scannedCategory = category,
                            scannedDate = dateStr,
                            scannedImagePath = imagePath
                        )
                    }
                    composable("manage_category") {
                        ManageCategoryScreen(rootNavController)
                    }
                }
            }
        }
    }
}