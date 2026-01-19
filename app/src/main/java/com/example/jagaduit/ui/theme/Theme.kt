package com.example.jagaduit.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

// ini buat darkmode
private val DarkColorScheme = darkColorScheme(
    primary = NeonPurple,
    onPrimary = PureWhite,
    secondary = NeonLime,
    onSecondary = NeonBlack,
    background = NeonBlack,
    onBackground = PureWhite,
    surface = DarkSurface,
    onSurface = PureWhite
)

@Composable
fun JagaDuitTheme(
    content: @Composable () -> Unit
) {
    val colorScheme = DarkColorScheme
    val view = LocalView.current

    if (!view.isInEditMode) {
        SideEffect {
            // buat status bar item
            val window = (view.context as android.app.Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}