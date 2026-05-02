package com.example.taskpulse.ui.insights

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.domain.model.AutomationRule
import com.example.taskpulse.domain.model.DailyProductivityPoint
import com.example.taskpulse.domain.usecase.ObserveAutomationRulesUseCase
import com.example.taskpulse.domain.usecase.ObserveDailyProductivityUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class InsightsUiState(
    val productivityTrend: List<DailyProductivityPoint> = emptyList(),
    val automationRules: List<AutomationRule> = emptyList(),
    val enabledAutomationCount: Int = 0
)

class InsightsViewModel(
    observeDailyProductivityUseCase: ObserveDailyProductivityUseCase,
    observeAutomationRulesUseCase: ObserveAutomationRulesUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(InsightsUiState())
    val uiState: StateFlow<InsightsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeDailyProductivityUseCase(limit = 7).collect { points ->
                _uiState.update { it.copy(productivityTrend = points.asReversed()) }
            }
        }
        viewModelScope.launch {
            observeAutomationRulesUseCase().collect { rules ->
                _uiState.update {
                    it.copy(
                        automationRules = rules,
                        enabledAutomationCount = rules.count { rule -> rule.enabled }
                    )
                }
            }
        }
    }

    class Factory(
        private val observeDailyProductivityUseCase: ObserveDailyProductivityUseCase,
        private val observeAutomationRulesUseCase: ObserveAutomationRulesUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return InsightsViewModel(
                observeDailyProductivityUseCase,
                observeAutomationRulesUseCase
            ) as T
        }
    }
}
