package com.example.taskpulse.ui.theme

import androidx.compose.runtime.Composable
import com.example.taskpulse.core.AppContainer

@Composable
fun TaskPulseThemeRoot(
    container: AppContainer,
    content: @Composable () -> Unit
) {
    // Stitch: siempre tema claro; sin modo oscuro ni seguir al sistema.
    TaskPulseTheme(darkTheme = false, content = content)
}
