package com.example.taskpulse.ui.theme

import android.app.Activity
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat

private val LightColorScheme = lightColorScheme(
    primary = TaskPulseColors.Primary,
    onPrimary = TaskPulseColors.White,
    primaryContainer = TaskPulseColors.PrimaryContainer,
    onPrimaryContainer = TaskPulseColors.White,
    secondary = Color(0xFF675C54),
    onSecondary = TaskPulseColors.White,
    secondaryContainer = TaskPulseColors.SecondaryContainer,
    onSecondaryContainer = TaskPulseColors.OnSecondaryContainer,
    tertiary = TaskPulseColors.Bronze,
    onTertiary = TaskPulseColors.White,
    background = TaskPulseColors.Gray50,
    onBackground = Color(0xFF191C1D),
    surface = TaskPulseColors.Gray50,
    onSurface = Color(0xFF191C1D),
    surfaceVariant = TaskPulseColors.Gray100,
    onSurfaceVariant = Color(0xFF54433B),
    outline = Color(0xFF867369),
    outlineVariant = TaskPulseColors.OutlineVariant,
    error = Color(0xFFBA1A1A),
    onError = TaskPulseColors.White
)

@Composable
fun TaskPulseTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    val colorScheme = LightColorScheme
    val view = LocalView.current
    if (!view.isInEditMode) {
        SideEffect {
            val window = (view.context as Activity).window
            window.statusBarColor = colorScheme.background.toArgb()
            window.navigationBarColor = colorScheme.background.toArgb()
            WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = true
            WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = true
        }
    }

    MaterialTheme(
        colorScheme = colorScheme,
        typography = Typography,
        content = content
    )
}
