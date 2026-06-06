package com.example.taskpulse.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = TaskPulseColors.Bronze,
    onPrimary = TaskPulseColors.White,
    primaryContainer = TaskPulseColors.BronzeMuted,
    onPrimaryContainer = TaskPulseColors.Gray800,
    secondary = TaskPulseColors.Gray700,
    onSecondary = TaskPulseColors.White,
    tertiary = TaskPulseColors.Bronze,
    onTertiary = TaskPulseColors.White,
    background = TaskPulseColors.White,
    onBackground = TaskPulseColors.Gray900,
    surface = TaskPulseColors.Gray50,
    onSurface = TaskPulseColors.Gray900,
    surfaceVariant = TaskPulseColors.Gray100,
    onSurfaceVariant = TaskPulseColors.Gray700,
    outline = TaskPulseColors.Gray300,
    outlineVariant = TaskPulseColors.Gray200,
    error = Color(0xFFD93025),
    onError = TaskPulseColors.White
)

private val DarkColorScheme = darkColorScheme(
    primary = TaskPulseColors.BronzeLight,
    onPrimary = TaskPulseColors.Gray900,
    primaryContainer = TaskPulseColors.BronzeDark,
    onPrimaryContainer = TaskPulseColors.BronzeMuted,
    secondary = TaskPulseColors.Gray500,
    onSecondary = TaskPulseColors.Gray900,
    tertiary = TaskPulseColors.BronzeLight,
    onTertiary = TaskPulseColors.Gray900,
    background = TaskPulseColors.Gray900,
    onBackground = TaskPulseColors.Gray100,
    surface = TaskPulseColors.GraySurfaceDark,
    onSurface = TaskPulseColors.Gray100,
    surfaceVariant = TaskPulseColors.Gray800,
    onSurfaceVariant = TaskPulseColors.Gray500,
    outline = TaskPulseColors.Gray600,
    outlineVariant = TaskPulseColors.Gray700,
    error = Color(0xFFF28B82),
    onError = TaskPulseColors.Gray900
)

@Composable
fun TaskPulseTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = !darkTheme
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = !darkTheme
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
