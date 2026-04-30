package com.example.taskpulse.domain.automation

import com.example.taskpulse.domain.model.AutomationRule
import com.example.taskpulse.domain.model.Task

interface RuleEngine {
    fun evaluate(rules: List<AutomationRule>, tasks: List<Task>, nowMillis: Long): List<RuleMatch>
}

data class RuleMatch(
    val ruleId: Long,
    val taskId: Long
)
