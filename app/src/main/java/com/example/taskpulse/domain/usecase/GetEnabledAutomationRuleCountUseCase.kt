package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.repository.AutomationRuleRepository

class GetEnabledAutomationRuleCountUseCase(
    private val repository: AutomationRuleRepository
) {
    suspend operator fun invoke(): Int = repository.countEnabledRules()
}
