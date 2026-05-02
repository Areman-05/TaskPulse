package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.AutomationRule
import com.example.taskpulse.domain.repository.AutomationRuleRepository
import kotlinx.coroutines.flow.Flow

class ObserveAutomationRulesUseCase(
    private val repository: AutomationRuleRepository
) {
    operator fun invoke(): Flow<List<AutomationRule>> = repository.observeRules()
}
