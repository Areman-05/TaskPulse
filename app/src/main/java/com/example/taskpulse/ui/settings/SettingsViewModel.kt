package com.example.taskpulse.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.data.export.TaskSnapshotFileExporter
import com.example.taskpulse.data.repository.SharedPreferencesThemeRepository
import com.example.taskpulse.domain.model.AppThemeMode
import com.example.taskpulse.domain.model.AutomationSweepRun
import com.example.taskpulse.domain.repository.AutomationSettingsRepository
import com.example.taskpulse.domain.usecase.GetAutomationSweepIntervalUseCase
import com.example.taskpulse.domain.usecase.LoadAutomationSweepHistoryUseCase
import com.example.taskpulse.domain.usecase.RescheduleAutomationSweepUseCase
import com.example.taskpulse.domain.usecase.RunEntryLifecycleMaintenanceUseCase
import com.example.taskpulse.domain.usecase.SetAutomationSweepIntervalUseCase
import com.example.taskpulse.domain.usecase.TriggerAutomationSweepNowUseCase
import java.io.File
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class PendingTaskExport(
    val absolutePath: String,
    val mimeType: String
)

data class SettingsUiState(
    val isMaintenanceRunning: Boolean = false,
    val sweepIntervalHours: String = "1",
    val saveIntervalMessage: String? = null,
    val sweepUnmeteredOnly: Boolean = false,
    val sweepRequiresCharging: Boolean = false,
    val recentSweepRuns: List<AutomationSweepRun> = emptyList(),
    val pendingExport: PendingTaskExport? = null
)

class SettingsViewModel(
    private val themeRepository: SharedPreferencesThemeRepository,
    private val triggerAutomationSweepNowUseCase: TriggerAutomationSweepNowUseCase,
    private val runEntryLifecycleMaintenanceUseCase: RunEntryLifecycleMaintenanceUseCase,
    private val getAutomationSweepIntervalUseCase: GetAutomationSweepIntervalUseCase,
    private val setAutomationSweepIntervalUseCase: SetAutomationSweepIntervalUseCase,
    private val rescheduleAutomationSweepUseCase: RescheduleAutomationSweepUseCase,
    private val automationSettingsRepository: AutomationSettingsRepository,
    private val loadAutomationSweepHistoryUseCase: LoadAutomationSweepHistoryUseCase,
    private val taskSnapshotFileExporter: TaskSnapshotFileExporter,
    private val roomDatabaseFile: File
) : ViewModel() {

    val themeMode = themeRepository.mode

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                sweepIntervalHours = getAutomationSweepIntervalUseCase().toString(),
                sweepUnmeteredOnly = automationSettingsRepository.isSweepUnmeteredOnly(),
                sweepRequiresCharging = automationSettingsRepository.isSweepRequiresCharging()
            )
        }
        refreshSweepHistory()
    }

    fun setLightMode() {
        themeRepository.setMode(AppThemeMode.LIGHT)
    }

    fun setDarkMode() {
        themeRepository.setMode(AppThemeMode.DARK)
    }

    fun refreshSweepHistory() {
        viewModelScope.launch {
            val runs = loadAutomationSweepHistoryUseCase()
            _uiState.update { it.copy(recentSweepRuns = runs) }
        }
    }

    fun runMaintenanceNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isMaintenanceRunning = true) }
            runEntryLifecycleMaintenanceUseCase()
            triggerAutomationSweepNowUseCase()
            _uiState.update { it.copy(isMaintenanceRunning = false) }
            refreshSweepHistory()
        }
    }

    fun onSweepIntervalChange(value: String) {
        _uiState.update { it.copy(sweepIntervalHours = value.filter { ch -> ch.isDigit() }) }
    }

    fun saveSweepInterval() {
        val hours = _uiState.value.sweepIntervalHours.toLongOrNull()?.coerceAtLeast(1L) ?: run {
            _uiState.update { it.copy(saveIntervalMessage = "settings_interval_invalid") }
            return
        }
        setAutomationSweepIntervalUseCase(hours)
        rescheduleAutomationSweepUseCase(hours)
        _uiState.update {
            it.copy(
                sweepIntervalHours = hours.toString(),
                saveIntervalMessage = "settings_interval_saved"
            )
        }
    }

    fun onSweepUnmeteredChange(enabled: Boolean) {
        automationSettingsRepository.setSweepUnmeteredOnly(enabled)
        _uiState.update { it.copy(sweepUnmeteredOnly = enabled) }
        rescheduleAutomationSweepUseCase(getAutomationSweepIntervalUseCase())
    }

    fun onSweepRequiresChargingChange(enabled: Boolean) {
        automationSettingsRepository.setSweepRequiresCharging(enabled)
        _uiState.update { it.copy(sweepRequiresCharging = enabled) }
        rescheduleAutomationSweepUseCase(getAutomationSweepIntervalUseCase())
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
                it.copy(pendingExport = PendingTaskExport(file.absolutePath, "application/octet-stream"))
            }
        }
    }

    fun consumePendingExport() {
        _uiState.update { it.copy(pendingExport = null) }
    }

    class Factory(
        private val themeRepository: SharedPreferencesThemeRepository,
        private val triggerAutomationSweepNowUseCase: TriggerAutomationSweepNowUseCase,
        private val runEntryLifecycleMaintenanceUseCase: RunEntryLifecycleMaintenanceUseCase,
        private val getAutomationSweepIntervalUseCase: GetAutomationSweepIntervalUseCase,
        private val setAutomationSweepIntervalUseCase: SetAutomationSweepIntervalUseCase,
        private val rescheduleAutomationSweepUseCase: RescheduleAutomationSweepUseCase,
        private val automationSettingsRepository: AutomationSettingsRepository,
        private val loadAutomationSweepHistoryUseCase: LoadAutomationSweepHistoryUseCase,
        private val taskSnapshotFileExporter: TaskSnapshotFileExporter,
        private val roomDatabaseFile: File
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SettingsViewModel(
                themeRepository,
                triggerAutomationSweepNowUseCase,
                runEntryLifecycleMaintenanceUseCase,
                getAutomationSweepIntervalUseCase,
                setAutomationSweepIntervalUseCase,
                rescheduleAutomationSweepUseCase,
                automationSettingsRepository,
                loadAutomationSweepHistoryUseCase,
                taskSnapshotFileExporter,
                roomDatabaseFile
            ) as T
        }
    }
}
