package com.example.jagaduit.ui.screens

import android.view.animation.OvershootInterpolator
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.platform.LocalContext // Import ini
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import com.example.jagaduit.R
import com.example.jagaduit.utils.SessionManager // Import SessionManager
import kotlinx.coroutines.delay

@Composable
fun SplashScreen(navController: NavController) {
    val scale = remember { Animatable(0f) }
    val context = LocalContext.current

    LaunchedEffect(key1 = true) {
        scale.animateTo(
            targetValue = 1f,
            animationSpec = tween(
                durationMillis = 1000,
                easing = { OvershootInterpolator(2f).getInterpolation(it) }
            )
        )

        delay(3000L)

        // CEK SESI 10 MENIT
        val sessionManager = SessionManager(context)

        if (sessionManager.isSessionValid()) {
            // Masih < 10 Menit Langsung ke Main Menu
            navController.navigate("main") {
                popUpTo("splash") { inclusive = true }
            }
        } else {
            // Sudah > 10 Menit atau Belum Login Masuk Ke Login Screen
            navController.navigate("login") {
                popUpTo("splash") { inclusive = true }
            }
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Image(
            painter = painterResource(id = R.drawable.logo_jagaduit_white),
            contentDescription = "Logo",
            modifier = Modifier
                .size(150.dp)
                .scale(scale.value)
        )
    }
}