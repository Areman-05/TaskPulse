package com.example.taskpulse.ui.theme

import android.app.Activity
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = TaskPulseColors.Black,
    onPrimary = TaskPulseColors.White,
    secondary = TaskPulseColors.Gray600,
    onSecondary = TaskPulseColors.White,
    tertiary = TaskPulseColors.Celestial,
    onTertiary = TaskPulseColors.Black,
    background = TaskPulseColors.White,
    onBackground = TaskPulseColors.Black,
    surface = TaskPulseColors.White,
    onSurface = TaskPulseColors.Black,
    surfaceVariant = TaskPulseColors.Gray100,
    onSurfaceVariant = TaskPulseColors.Gray600,
    outline = TaskPulseColors.Gray200,
    outlineVariant = TaskPulseColors.Gray200
)

private val DarkColorScheme = darkColorScheme(
    primary = TaskPulseColors.White,
    onPrimary = TaskPulseColors.Black,
    secondary = TaskPulseColors.Gray400,
    onSecondary = TaskPulseColors.Black,
    tertiary = TaskPulseColors.Celestial,
    onTertiary = TaskPulseColors.Black,
    background = TaskPulseColors.Black,
    onBackground = TaskPulseColors.White,
    surface = TaskPulseColors.Gray900,
    onSurface = TaskPulseColors.White,
    surfaceVariant = TaskPulseColors.Gray900,
    onSurfaceVariant = TaskPulseColors.Gray400,
    outline = TaskPulseColors.Gray600,
    outlineVariant = TaskPulseColors.Gray600
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
