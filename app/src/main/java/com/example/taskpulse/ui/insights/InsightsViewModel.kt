package com.example.taskpulse.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.domain.model.AutomationAction
import com.example.taskpulse.domain.model.AutomationRule
import com.example.taskpulse.domain.model.AutomationTrigger
import com.example.taskpulse.domain.model.DailyProductivityPoint
import com.example.taskpulse.domain.usecase.DeleteAutomationRuleUseCase
import com.example.taskpulse.domain.usecase.GetAutomationSweepIntervalUseCase
import com.example.taskpulse.domain.usecase.ObserveAutomationRulesUseCase
import com.example.taskpulse.domain.usecase.ObserveDailyProductivityUseCase
import com.example.taskpulse.domain.usecase.SetAutomationRuleEnabledUseCase
import com.example.taskpulse.domain.usecase.SetAutomationSweepIntervalUseCase
import com.example.taskpulse.domain.usecase.TriggerAutomationSweepNowUseCase
import com.example.taskpulse.domain.usecase.UpdateAutomationRuleDefinitionUseCase
import com.example.taskpulse.domain.usecase.UpsertAutomationRuleUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

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
    val saveIntervalMessage: String? = null
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
    private val setAutomationSweepIntervalUseCase: SetAutomationSweepIntervalUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        _uiState.update {
            it.copy(sweepIntervalHours = getAutomationSweepIntervalUseCase().toString())
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

    fun cycleTrigger() {
        val next = when (_uiState.value.draftTrigger) {
            AutomationTrigger.TASK_NOT_COMPLETED -> AutomationTrigger.TASK_STALE_DAYS
            AutomationTrigger.TASK_STALE_DAYS -> AutomationTrigger.TASK_FAILED
            AutomationTrigger.TASK_FAILED -> AutomationTrigger.TASK_NOT_COMPLETED
        }
        _uiState.update { it.copy(draftTrigger = next) }
    }

    fun cycleAction() {
        val next = when (_uiState.value.draftAction) {
            AutomationAction.SEND_NOTIFICATION -> AutomationAction.MARK_AS_IN_PROGRESS
            AutomationAction.MARK_AS_IN_PROGRESS -> AutomationAction.MARK_AS_FAILED
            AutomationAction.MARK_AS_FAILED -> AutomationAction.SEND_NOTIFICATION
        }
        _uiState.update { it.copy(draftAction = next) }
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

    fun deleteRule(ruleId: Long) {
        viewModelScope.launch {
            deleteAutomationRuleUseCase(ruleId)
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
        _uiState.update {
            it.copy(
                sweepIntervalHours = hours.toString(),
                saveIntervalMessage = "insights_interval_saved"
            )
        }
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
        private val setAutomationSweepIntervalUseCase: SetAutomationSweepIntervalUseCase
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
                setAutomationSweepIntervalUseCase
            ) as T
        }
    }
}
