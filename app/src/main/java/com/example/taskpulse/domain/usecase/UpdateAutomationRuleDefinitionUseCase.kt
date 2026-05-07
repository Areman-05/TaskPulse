package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.AutomationRule
import com.example.taskpulse.domain.repository.AutomationRuleRepository

class UpdateAutomationRuleDefinitionUseCase(
    private val repository: AutomationRuleRepository
) {
    suspend operator fun invoke(rule: AutomationRule) {
        repository.updateRuleDefinition(rule)
    }
}
