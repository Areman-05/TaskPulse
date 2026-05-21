package com.example.taskpulse.data.repository

import android.content.Context
import com.example.taskpulse.domain.model.AppThemeMode
import com.example.taskpulse.domain.repository.ThemeRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class SharedPreferencesThemeRepository(
    context: Context
) : ThemeRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    private val _mode = MutableStateFlow(readMode())

    override val mode: StateFlow<AppThemeMode> = _mode.asStateFlow()

    override fun setMode(mode: AppThemeMode) {
        prefs.edit().putString(KEY_MODE, mode.name).apply()
        _mode.value = mode
    }

    /**
     * Alterna según cómo se ve la app ahora (no el modo guardado abstracto).
     * Evita el paso SYSTEM→LIGHT cuando ya estás en claro y parece que "no hace nada".
     */
    fun toggleLightDark(isEffectivelyDark: Boolean) {
        setMode(if (isEffectivelyDark) AppThemeMode.LIGHT else AppThemeMode.DARK)
    }

    private fun readMode(): AppThemeMode {
        val raw = prefs.getString(KEY_MODE, AppThemeMode.LIGHT.name).orEmpty()
        return runCatching { AppThemeMode.valueOf(raw) }.getOrElse { AppThemeMode.LIGHT }
    }

    private companion object {
        const val PREFS_NAME = "taskpulse_appearance"
        const val KEY_MODE = "theme_mode"
    }
}
