package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.repository.AutomationRuleRepository

class EnsureStarterAutomationRulesUseCase(
    private val repository: AutomationRuleRepository
) {
    suspend operator fun invoke() {
        repository.ensureStarterRules()
    }
}
