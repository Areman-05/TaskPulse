package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.automation.RuleEngine
import com.example.taskpulse.domain.automation.RuleMatch
import com.example.taskpulse.domain.model.AutomationRule
import com.example.taskpulse.domain.model.Task

class EvaluateAutomationRulesUseCase(
    private val ruleEngine: RuleEngine
) {
    operator fun invoke(
        rules: List<AutomationRule>,
        tasks: List<Task>,
        nowMillis: Long
    ): List<RuleMatch> = ruleEngine.evaluate(rules, tasks, nowMillis)
}
