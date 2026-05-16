package com.example.taskpulse.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.taskpulse.data.repository.SharedPreferencesThemeRepository
import com.example.taskpulse.domain.model.AppThemeMode

class SettingsViewModel(
    private val themeRepository: SharedPreferencesThemeRepository
) : ViewModel() {

    val themeMode = themeRepository.mode

    fun setLightMode() {
        themeRepository.setMode(AppThemeMode.LIGHT)
    }

    fun setDarkMode() {
        themeRepository.setMode(AppThemeMode.DARK)
    }

    class Factory(
        private val themeRepository: SharedPreferencesThemeRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(themeRepository) as T
        }
    }
}
