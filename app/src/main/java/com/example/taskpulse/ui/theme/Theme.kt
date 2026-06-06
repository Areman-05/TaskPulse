package com.example.taskpulse.ui.theme

import android.app.Activity
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
    primary = TaskPulseColors.Primary,
    onPrimary = TaskPulseColors.White,
    primaryContainer = TaskPulseColors.PrimaryContainer,
    onPrimaryContainer = TaskPulseColors.OnPrimaryContainer,
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
    onSurfaceVariant = TaskPulseColors.OnSurfaceVariant,
    surfaceContainer = TaskPulseColors.SurfaceContainer,
    surfaceContainerHigh = TaskPulseColors.SurfaceContainerHigh,
    surfaceContainerLow = TaskPulseColors.SurfaceContainerLow,
    surfaceContainerLowest = TaskPulseColors.White,
    outline = TaskPulseColors.Outline,
    outlineVariant = TaskPulseColors.OutlineVariant,
    error = TaskPulseColors.Error,
    onError = TaskPulseColors.White
)

private val DarkColorScheme = darkColorScheme(
    primary = TaskPulseColors.BronzeLight,
    onPrimary = TaskPulseColors.Gray900,
    primaryContainer = TaskPulseColors.TertiaryContainer,
    onPrimaryContainer = TaskPulseColors.OnPrimaryContainer,
    secondary = TaskPulseColors.Gray500,
    onSecondary = TaskPulseColors.Gray900,
    secondaryContainer = TaskPulseColors.TertiaryContainer,
    onSecondaryContainer = TaskPulseColors.OnPrimaryContainer,
    tertiary = TaskPulseColors.BronzeLight,
    onTertiary = TaskPulseColors.Gray900,
    background = TaskPulseColors.Gray900,
    onBackground = TaskPulseColors.Gray100,
    surface = TaskPulseColors.GraySurfaceDark,
    onSurface = TaskPulseColors.Gray100,
    surfaceVariant = TaskPulseColors.Gray800,
    onSurfaceVariant = TaskPulseColors.Gray500,
    surfaceContainer = TaskPulseColors.Gray800,
    surfaceContainerHigh = TaskPulseColors.Gray700,
    surfaceContainerLow = Color(0xFF3C4043),
    surfaceContainerLowest = TaskPulseColors.GraySurfaceDark,
    outline = TaskPulseColors.Gray600,
    outlineVariant = TaskPulseColors.Gray700,
    error = Color(0xFFF28B82),
    onError = TaskPulseColors.Gray900
)

@Composable
fun TaskPulseTheme(
    darkTheme: Boolean = false,
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
