package com.example.taskpulse.domain.automation

import com.example.taskpulse.domain.model.AutomationRule
import com.example.taskpulse.domain.model.AutomationTrigger
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskStatus

class SimpleRuleEngine : RuleEngine {
    override fun evaluate(rules: List<AutomationRule>, tasks: List<Task>, nowMillis: Long): List<RuleMatch> {
        val output = mutableListOf<RuleMatch>()
        rules.filter { it.enabled }.forEach { rule ->
            tasks.forEach { task ->
                if (matches(rule, task, nowMillis)) {
                    output += RuleMatch(ruleId = rule.id, taskId = task.id)
                }
            }
        }
        return output
    }

    private fun matches(rule: AutomationRule, task: Task, nowMillis: Long): Boolean = when (rule.trigger) {
        AutomationTrigger.TASK_NOT_COMPLETED ->
            task.status != TaskStatus.COMPLETED && task.dueAtMillis != null && task.dueAtMillis < nowMillis

        AutomationTrigger.TASK_STALE_DAYS -> {
            val threshold = rule.thresholdDays ?: return false
            task.status != TaskStatus.COMPLETED && task.updatedAtMillis < nowMillis - (threshold * DAY_MS)
        }

        AutomationTrigger.TASK_FAILED -> task.status == TaskStatus.FAILED
    }

    private companion object {
        const val DAY_MS = 24L * 60L * 60L * 1000L
    }
}
