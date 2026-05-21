package com.example.taskpulse.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskpulse.core.requireAppContainer
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.notification.TaskNotificationHelper

class TaskReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(WorkerKeys.TASK_ID, 0L)
        val taskTitle = inputData.getString(WorkerKeys.TASK_TITLE).orEmpty()
        if (taskId == 0L || taskTitle.isBlank()) return Result.failure()

        val container = applicationContext.requireAppContainer()
        val task = container.getTaskUseCase(taskId) ?: return Result.success()

        when (task.status) {
            TaskStatus.COMPLETED,
            TaskStatus.FAILED -> {
                container.cancelTaskReminderUseCase(taskId)
                TaskNotificationHelper(applicationContext).cancelReminderNotification(taskId)
                return Result.success()
            }
            else -> Unit
        }

        val blockerId = task.blockedByTaskId
        if (blockerId != null) {
            val blocker = container.getTaskUseCase(blockerId)
            if (blocker == null || blocker.status != TaskStatus.COMPLETED) {
                return Result.retry()
            }
        }

        val notifier = TaskNotificationHelper(applicationContext)
        notifier.showReminderIfAllowed(taskId, task.title.ifBlank { taskTitle })
        return Result.success()
    }
}
