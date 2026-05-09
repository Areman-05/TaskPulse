package com.example.taskpulse.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.data.export.TaskSnapshotFileExporter
import com.example.taskpulse.domain.model.AutomationAction
import com.example.taskpulse.domain.model.AutomationRule
import com.example.taskpulse.domain.model.AutomationSweepRun
import com.example.taskpulse.domain.model.AutomationTrigger
import com.example.taskpulse.domain.model.DailyProductivityPoint
import com.example.taskpulse.domain.repository.AutomationSettingsRepository
import com.example.taskpulse.domain.usecase.DeleteAutomationRuleUseCase
import com.example.taskpulse.domain.usecase.GetAutomationRuleUseCase
import com.example.taskpulse.domain.usecase.GetAutomationSweepIntervalUseCase
import com.example.taskpulse.domain.usecase.GetEnabledAutomationRuleCountUseCase
import com.example.taskpulse.domain.usecase.LoadAutomationSweepHistoryUseCase
import com.example.taskpulse.domain.usecase.ObserveAutomationRulesUseCase
import com.example.taskpulse.domain.usecase.ObserveDailyProductivityUseCase
import com.example.taskpulse.domain.usecase.RescheduleAutomationSweepUseCase
import com.example.taskpulse.domain.usecase.SetAutomationRuleEnabledUseCase
import com.example.taskpulse.domain.usecase.SetAutomationSweepIntervalUseCase
import com.example.taskpulse.domain.usecase.TriggerAutomationSweepNowUseCase
import com.example.taskpulse.domain.usecase.UpdateAutomationRuleDefinitionUseCase
import com.example.taskpulse.domain.usecase.UpsertAutomationRuleUseCase
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

data class InsightsUiState(
    val productivityTrend: List<DailyProductivityPoint> = emptyList(),
    val automationRules: List<AutomationRule> = emptyList(),
    val enabledAutomationCount: Int = 0,
    val isSweepRunning: Boolean = false,
    val draftRuleId: Long? = null,
    val draftRuleName: String = "",
    val draftTrigger: AutomationTrigger = AutomationTrigger.TASK_NOT_COMPLETED,
    val draftAction: AutomationAction = AutomationAction.SEND_NOTIFICATION,
    val draftThresholdDays: String = "",
    val sweepIntervalHours: String = "1",
    val draftValidationError: String? = null,
    val saveIntervalMessage: String? = null,
    val sweepUnmeteredOnly: Boolean = false,
    val sweepRequiresCharging: Boolean = false,
    val recentSweepRuns: List<AutomationSweepRun> = emptyList(),
    val pendingExport: PendingTaskExport? = null,
    val rulePendingDeleteId: Long? = null
)

class InsightsViewModel(
    observeDailyProductivityUseCase: ObserveDailyProductivityUseCase,
    observeAutomationRulesUseCase: ObserveAutomationRulesUseCase,
    private val setAutomationRuleEnabledUseCase: SetAutomationRuleEnabledUseCase,
    private val triggerAutomationSweepNowUseCase: TriggerAutomationSweepNowUseCase,
    private val upsertAutomationRuleUseCase: UpsertAutomationRuleUseCase,
    private val updateAutomationRuleDefinitionUseCase: UpdateAutomationRuleDefinitionUseCase,
    private val deleteAutomationRuleUseCase: DeleteAutomationRuleUseCase,
    private val getAutomationRuleUseCase: GetAutomationRuleUseCase,
    private val getEnabledAutomationRuleCountUseCase: GetEnabledAutomationRuleCountUseCase,
    private val getAutomationSweepIntervalUseCase: GetAutomationSweepIntervalUseCase,
    private val setAutomationSweepIntervalUseCase: SetAutomationSweepIntervalUseCase,
    private val rescheduleAutomationSweepUseCase: RescheduleAutomationSweepUseCase,
    private val automationSettingsRepository: AutomationSettingsRepository,
    private val loadAutomationSweepHistoryUseCase: LoadAutomationSweepHistoryUseCase,
    private val taskSnapshotFileExporter: TaskSnapshotFileExporter,
    private val roomDatabaseFile: File
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(
                sweepIntervalHours = getAutomationSweepIntervalUseCase().toString(),
                sweepUnmeteredOnly = automationSettingsRepository.isSweepUnmeteredOnly(),
                sweepRequiresCharging = automationSettingsRepository.isSweepRequiresCharging()
            )
        }
        viewModelScope.launch {
            observeDailyProductivityUseCase(limit = 7).collect { points ->
                _uiState.update { it.copy(productivityTrend = points.asReversed()) }
            }
        }
        viewModelScope.launch {
            observeAutomationRulesUseCase().collect { rules ->
                val enabledCount = getEnabledAutomationRuleCountUseCase()
                _uiState.update {
                    it.copy(
                        automationRules = rules,
                        enabledAutomationCount = enabledCount
                    )
                }
            }
        }
        refreshSweepHistory()
    }

    fun refreshSweepHistory() {
        viewModelScope.launch {
            val runs = loadAutomationSweepHistoryUseCase()
            _uiState.update { it.copy(recentSweepRuns = runs) }
        }
    }

    fun toggleRule(ruleId: Long, currentlyEnabled: Boolean) {
        viewModelScope.launch {
            setAutomationRuleEnabledUseCase(ruleId, !currentlyEnabled)
        }
    }

    fun runSweepNow() {
        viewModelScope.launch {
            _uiState.update { it.copy(isSweepRunning = true) }
            triggerAutomationSweepNowUseCase()
            _uiState.update { it.copy(isSweepRunning = false) }
            refreshSweepHistory()
        }
    }

    fun onDraftNameChange(value: String) {
        _uiState.update { it.copy(draftRuleName = value) }
    }

    fun onDraftThresholdChange(value: String) {
        _uiState.update { it.copy(draftThresholdDays = value.filter { ch -> ch.isDigit() }) }
    }

    fun onDraftTriggerChange(trigger: AutomationTrigger) {
        _uiState.update { it.copy(draftTrigger = trigger) }
    }

    fun onDraftActionChange(action: AutomationAction) {
        _uiState.update { it.copy(draftAction = action) }
    }

    fun beginEdit(rule: AutomationRule) {
        viewModelScope.launch {
            val persisted = getAutomationRuleUseCase(rule.id) ?: return@launch
            _uiState.update {
                it.copy(
                    draftRuleId = persisted.id,
                    draftRuleName = persisted.name,
                    draftTrigger = persisted.trigger,
                    draftAction = persisted.action,
                    draftThresholdDays = persisted.thresholdDays?.toString().orEmpty(),
                    draftValidationError = null
                )
            }
        }
    }

    fun clearDraft() {
        _uiState.update {
            it.copy(
                draftRuleId = null,
                draftRuleName = "",
                draftTrigger = AutomationTrigger.TASK_NOT_COMPLETED,
                draftAction = AutomationAction.SEND_NOTIFICATION,
                draftThresholdDays = "",
                draftValidationError = null
            )
        }
    }

    fun saveDraftRule() {
        viewModelScope.launch {
            val state = _uiState.value
            val trimmedName = state.draftRuleName.trim()
            if (trimmedName.isBlank()) {
                _uiState.update { it.copy(draftValidationError = "insights_error_name_required") }
                return@launch
            }

            val threshold = state.draftThresholdDays.toIntOrNull()
            if (state.draftTrigger == AutomationTrigger.TASK_STALE_DAYS && threshold == null) {
                _uiState.update { it.copy(draftValidationError = "insights_error_stale_needs_threshold") }
                return@launch
            }
            if (state.draftRuleId == null) {
                upsertAutomationRuleUseCase(
                    AutomationRule(
                        id = 0,
                        name = trimmedName,
                        enabled = true,
                        trigger = state.draftTrigger,
                        action = state.draftAction,
                        thresholdDays = threshold
                    )
                )
            } else {
                updateAutomationRuleDefinitionUseCase(
                    AutomationRule(
                        id = state.draftRuleId,
                        name = trimmedName,
                        enabled = true,
                        trigger = state.draftTrigger,
                        action = state.draftAction,
                        thresholdDays = threshold
                    )
                )
            }
            clearDraft()
        }
    }

    fun requestDeleteRule(ruleId: Long) {
        _uiState.update { it.copy(rulePendingDeleteId = ruleId) }
    }

    fun cancelPendingDeleteRule() {
        _uiState.update { it.copy(rulePendingDeleteId = null) }
    }

    fun confirmPendingDeleteRule() {
        viewModelScope.launch {
            val id = _uiState.value.rulePendingDeleteId ?: return@launch
            deleteAutomationRuleUseCase(id)
            _uiState.update { it.copy(rulePendingDeleteId = null) }
        }
    }

    fun onSweepIntervalChange(value: String) {
        _uiState.update { it.copy(sweepIntervalHours = value.filter { ch -> ch.isDigit() }) }
    }

    fun saveSweepInterval() {
        val hours = _uiState.value.sweepIntervalHours.toLongOrNull()?.coerceAtLeast(1L) ?: run {
            _uiState.update { it.copy(saveIntervalMessage = "insights_interval_invalid") }
            return
        }
        setAutomationSweepIntervalUseCase(hours)
        rescheduleAutomationSweepUseCase(hours)
        _uiState.update {
            it.copy(
                sweepIntervalHours = hours.toString(),
                saveIntervalMessage = "insights_interval_saved"
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
        private val observeDailyProductivityUseCase: ObserveDailyProductivityUseCase,
        private val observeAutomationRulesUseCase: ObserveAutomationRulesUseCase,
        private val setAutomationRuleEnabledUseCase: SetAutomationRuleEnabledUseCase,
        private val triggerAutomationSweepNowUseCase: TriggerAutomationSweepNowUseCase,
        private val upsertAutomationRuleUseCase: UpsertAutomationRuleUseCase,
        private val updateAutomationRuleDefinitionUseCase: UpdateAutomationRuleDefinitionUseCase,
        private val deleteAutomationRuleUseCase: DeleteAutomationRuleUseCase,
        private val getAutomationRuleUseCase: GetAutomationRuleUseCase,
        private val getEnabledAutomationRuleCountUseCase: GetEnabledAutomationRuleCountUseCase,
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
            return InsightsViewModel(
                observeDailyProductivityUseCase,
                observeAutomationRulesUseCase,
                setAutomationRuleEnabledUseCase,
                triggerAutomationSweepNowUseCase,
                upsertAutomationRuleUseCase,
                updateAutomationRuleDefinitionUseCase,
                deleteAutomationRuleUseCase,
                getAutomationRuleUseCase,
                getEnabledAutomationRuleCountUseCase,
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
