package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.AutomationRule
import com.example.taskpulse.domain.repository.AutomationRuleRepository

class GetAutomationRuleUseCase(
    private val repository: AutomationRuleRepository
) {
    suspend operator fun invoke(ruleId: Long): AutomationRule? = repository.getRule(ruleId)
}
