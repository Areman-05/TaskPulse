package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.repository.AutomationRuleRepository

class DeleteAutomationRuleUseCase(
    private val repository: AutomationRuleRepository
) {
    suspend operator fun invoke(ruleId: Long) {
        repository.deleteRule(ruleId)
    }
}
