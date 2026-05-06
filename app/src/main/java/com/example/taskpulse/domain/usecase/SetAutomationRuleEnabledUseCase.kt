package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.repository.AutomationRuleRepository

class SetAutomationRuleEnabledUseCase(
    private val repository: AutomationRuleRepository
) {
    suspend operator fun invoke(ruleId: Long, enabled: Boolean) {
        repository.setRuleEnabled(ruleId, enabled)
    }
}
