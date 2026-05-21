package com.example.taskpulse.domain.repository

import com.example.taskpulse.domain.model.AppThemeMode
import kotlinx.coroutines.flow.StateFlow

interface ThemeRepository {
    val mode: StateFlow<AppThemeMode>
    fun setMode(mode: AppThemeMode)
}
