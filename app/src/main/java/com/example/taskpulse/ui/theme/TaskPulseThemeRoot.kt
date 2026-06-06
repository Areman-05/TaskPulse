package com.example.taskpulse.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.core.AppContainer
import com.example.taskpulse.domain.model.AppThemeMode

@Composable
fun TaskPulseThemeRoot(
    container: AppContainer,
    content: @Composable () -> Unit
) {
    val mode by container.themeRepository.mode.collectAsStateWithLifecycle(
        initialValue = AppThemeMode.LIGHT
    )
    val darkTheme = when (mode) {
        AppThemeMode.DARK -> true
        AppThemeMode.LIGHT -> false
        AppThemeMode.SYSTEM -> false
    }
    TaskPulseTheme(darkTheme = darkTheme, content = content)
}
