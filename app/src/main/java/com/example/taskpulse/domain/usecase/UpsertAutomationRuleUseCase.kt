package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.AutomationRule
import com.example.taskpulse.domain.repository.AutomationRuleRepository

class UpsertAutomationRuleUseCase(
    private val repository: AutomationRuleRepository
) {
    suspend operator fun invoke(rule: AutomationRule): Long {
        return repository.upsertRule(rule)
    }
}
