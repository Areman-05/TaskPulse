package com.example.taskpulse.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskpulse.notification.TaskNotificationHelper

class TaskReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        val taskId = inputData.getLong(WorkerKeys.TASK_ID, 0L)
        val taskTitle = inputData.getString(WorkerKeys.TASK_TITLE).orEmpty()
        if (taskId == 0L || taskTitle.isBlank()) return Result.failure()

        val notifier = TaskNotificationHelper(applicationContext)
        notifier.ensureChannel()
        notifier.showReminder(taskId, taskTitle)
        return Result.success()
    }
}
