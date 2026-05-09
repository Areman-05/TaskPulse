package com.example.taskpulse.data.repository

import android.content.Context
import com.example.taskpulse.domain.model.AppThemeMode
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedPreferencesThemeRepository(
    context: Context
) {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _mode = MutableStateFlow(readMode())

    val mode: StateFlow<AppThemeMode> = _mode.asStateFlow()

    fun setMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
        _mode.value = mode
    }

    fun cyclePreferredMode() {
        val next = when (readMode()) {
            AppThemeMode.SYSTEM -> AppThemeMode.LIGHT
            AppThemeMode.LIGHT -> AppThemeMode.DARK
            AppThemeMode.DARK -> AppThemeMode.SYSTEM
        }
        setMode(next)
    }

    private fun readMode(): AppThemeMode {
        val raw = prefs.getString(KEY_MODE, AppThemeMode.SYSTEM.name).orEmpty()
        return runCatching { AppThemeMode.valueOf(raw) }.getOrElse { AppThemeMode.SYSTEM }
    }

    private companion object {
        const val PREFS_NAME = "taskpulse_appearance"
        const val KEY_MODE = "theme_mode"
    }
}
