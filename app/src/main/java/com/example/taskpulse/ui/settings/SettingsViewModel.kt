package com.example.taskpulse.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.data.export.TaskSnapshotFileExporter
import com.example.taskpulse.domain.model.AppThemeMode
import com.example.taskpulse.domain.repository.ThemeRepository
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val themeRepository: ThemeRepository,
    private val taskSnapshotFileExporter: TaskSnapshotFileExporter,
    private val roomDatabaseFile: File
) : ViewModel() {

    val themeMode = themeRepository.mode

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun setDarkModeEnabled(enabled: Boolean) {
        themeRepository.setMode(if (enabled) AppThemeMode.DARK else AppThemeMode.LIGHT)
    }

    fun exportJsonSnapshot() {
        viewModelScope.launch {
            val file = taskSnapshotFileExporter.writeJson()
            _uiState.update {
                it.copy(pendingExport = PendingTaskExport(file.absolutePath, "application/json"))
            }
        }
    }

    fun exportCsvSnapshot() {
        viewModelScope.launch {
            val file = taskSnapshotFileExporter.writeCsv()
            _uiState.update {
                it.copy(pendingExport = PendingTaskExport(file.absolutePath, "text/csv"))
            }
        }
    }

    fun exportDatabaseBackup() {
        viewModelScope.launch {
            val file = taskSnapshotFileExporter.writeDatabaseBackup(roomDatabaseFile)
            _uiState.update {
                it.copy(
                    pendingExport = PendingTaskExport(file.absolutePath, "application/octet-stream")
                )
            }
        }
    }

    fun consumePendingExport() {
        _uiState.update { it.copy(pendingExport = null) }
    }

    class Factory(
        private val themeRepository: ThemeRepository,
        private val taskSnapshotFileExporter: TaskSnapshotFileExporter,
        private val roomDatabaseFile: File
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                themeRepository,
                taskSnapshotFileExporter,
                roomDatabaseFile
            ) as T
        }
    }
}
