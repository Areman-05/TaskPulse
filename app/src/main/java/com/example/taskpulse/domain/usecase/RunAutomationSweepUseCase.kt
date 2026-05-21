package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.AutomationAction
import com.example.taskpulse.domain.model.AutomationRule
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.repository.AutomationRuleRepository
import com.example.taskpulse.domain.repository.TaskRepository
import com.example.taskpulse.notification.TaskNotificationHelper

/**
 * Barrido en segundo plano: ciclo de vida de entradas + evaluación de reglas de automatización.
 */
class RunAutomationSweepUseCase(
    private val taskRepository: TaskRepository,
    private val automationRuleRepository: AutomationRuleRepository,
    private val runEntryLifecycleMaintenanceUseCase: RunEntryLifecycleMaintenanceUseCase,
    private val evaluateAutomationRulesUseCase: EvaluateAutomationRulesUseCase,
    private val markTaskInProgressUseCase: MarkTaskInProgressUseCase,
    private val markTaskFailedUseCase: MarkTaskFailedUseCase,
    private val appendAutomationSweepRunUseCase: AppendAutomationSweepRunUseCase,
    private val notificationHelper: TaskNotificationHelper
) {
    suspend operator fun invoke(nowMillis: Long = System.currentTimeMillis()): Int {
        val lifecycle = runEntryLifecycleMaintenanceUseCase(nowMillis)
        val tasks = taskRepository.listAllTasks()
        val rules = automationRuleRepository.listRules().filter { it.enabled }
        var ruleActions = 0

        if (rules.isNotEmpty()) {
            val matches = evaluateAutomationRulesUseCase(rules, tasks, nowMillis)
                .distinctBy { "${it.ruleId}:${it.taskId}" }
            ruleActions = matches.size

            matches.forEach { match ->
                applyRuleMatch(rules, tasks, match.ruleId, match.taskId, nowMillis)
            }
        }

        val loggedActions = lifecycle.autoCompleted + lifecycle.archived + ruleActions
        appendAutomationSweepRunUseCase(loggedActions, nowMillis)
        return loggedActions
    }

    private suspend fun applyRuleMatch(
        rules: List<AutomationRule>,
        tasks: List<Task>,
        ruleId: Long,
        taskId: Long,
        nowMillis: Long
    ) {
        val rule = rules.find { it.id == ruleId } ?: return
        val task = tasks.find { it.id == taskId } ?: return
        when (rule.action) {
            AutomationAction.SEND_NOTIFICATION -> {
                if (task.status == TaskStatus.COMPLETED) return
                val notificationId = automationNotificationId(task.id, rule.id)
                notificationHelper.showAutomationAlertIfAllowed(
                    ruleName = rule.name,
                    taskTitle = task.title,
                    taskId = task.id,
                    ruleId = rule.id,
                    notificationId = notificationId,
                    nowMillis = nowMillis
                )
            }
            AutomationAction.MARK_AS_IN_PROGRESS -> markTaskInProgressUseCase(task.id, nowMillis)
            AutomationAction.MARK_AS_FAILED ->
                markTaskFailedUseCase(task.id, nowMillis, "automation:${rule.id}")
        }
    }

    private fun automationNotificationId(taskId: Long, ruleId: Long): Int {
        val raw = (taskId shl 16) xor ruleId
        val mixed = raw xor (raw ushr 32)
        return if (mixed.toInt() == 0) 1 else mixed.toInt()
    }
}
