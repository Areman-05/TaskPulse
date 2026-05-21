package com.example.taskpulse.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskpulse.core.AppContainer
import com.example.taskpulse.domain.model.AutomationAction
import com.example.taskpulse.notification.TaskNotificationHelper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AutomationSweepWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val container = AppContainer(applicationContext)
        val now = System.currentTimeMillis()

        val lifecycle = container.runEntryLifecycleMaintenanceUseCase(now)
        var ruleMatchCount = 0

        val tasks = container.loadTaskSnapshot()
        val rules = container.loadAutomationRulesSnapshot().filter { it.enabled }
        if (rules.isNotEmpty()) {
            val matches = container.evaluateAutomationRulesUseCase(rules, tasks, now)
                .distinctBy { "${it.ruleId}:${it.taskId}" }
            ruleMatchCount = matches.size

            if (matches.isNotEmpty()) {
                val notifier = TaskNotificationHelper(applicationContext)
                matches.forEach { match ->
                    val rule = rules.find { it.id == match.ruleId } ?: return@forEach
                    val task = tasks.find { it.id == match.taskId } ?: return@forEach
                    when (rule.action) {
                        AutomationAction.SEND_NOTIFICATION -> {
                            if (task.status == com.example.taskpulse.domain.model.TaskStatus.COMPLETED) {
                                return@forEach
                            }
                            val notificationId = generateAutomationNotificationId(task.id, rule.id)
                            notifier.showAutomationAlertIfAllowed(
                                ruleName = rule.name,
                                taskTitle = task.title,
                                taskId = task.id,
                                ruleId = rule.id,
                                notificationId = notificationId,
                                nowMillis = now
                            )
                        }
                        AutomationAction.MARK_AS_IN_PROGRESS -> {
                            container.markTaskInProgressUseCase(task.id, now)
                        }
                        AutomationAction.MARK_AS_FAILED -> {
                            container.markTaskFailedUseCase(task.id, now, "automation:${rule.id}")
                        }
                    }
                }
            }
        }

        val loggedActions = lifecycle.autoCompleted + lifecycle.archived + ruleMatchCount
        container.appendAutomationSweepRunUseCase(loggedActions, now)

        return@withContext Result.success()
    }

    private fun generateAutomationNotificationId(taskId: Long, ruleId: Long): Int {
        val raw = (taskId shl 16) xor ruleId
        val mixed = raw xor (raw ushr 32)
        return if (mixed.toInt() == 0) 1 else mixed.toInt()
    }
}
