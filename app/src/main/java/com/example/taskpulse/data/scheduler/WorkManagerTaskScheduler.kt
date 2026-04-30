package com.example.taskpulse.data.scheduler

import android.content.Context
import androidx.work.Constraints
import androidx.work.Data
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.scheduler.TaskScheduler
import com.example.taskpulse.worker.TaskReminderWorker
import com.example.taskpulse.worker.WorkerKeys
import java.util.concurrent.TimeUnit

class WorkManagerTaskScheduler(
    context: Context
) : TaskScheduler {
    private val workManager: WorkManager = WorkManager.getInstance(context)

    override fun scheduleReminder(task: Task) {
        val delay = task.dueAtMillis?.minus(System.currentTimeMillis())?.coerceAtLeast(0L) ?: 0L
        val request = OneTimeWorkRequestBuilder<TaskReminderWorker>()
            .setInitialDelay(delay, TimeUnit.MILLISECONDS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                Data.Builder()
                    .putLong(WorkerKeys.TASK_ID, task.id)
                    .putString(WorkerKeys.TASK_TITLE, task.title)
                    .build()
            )
            .addTag("task-reminders")
            .addTag(taskTag(task.id))
            .build()

        workManager.enqueueUniqueWork(
            uniqueName(task.id),
            ExistingWorkPolicy.REPLACE,
            request
        )
    }

    override fun scheduleRecurring(task: Task) {
        val recurrence = task.recurrence ?: return
        val repeatUnit = when (recurrence.unit.name) {
            "DAY" -> TimeUnit.DAYS
            "WEEK" -> TimeUnit.DAYS
            else -> TimeUnit.DAYS
        }
        val repeatValue = if (recurrence.unit.name == "WEEK") {
            recurrence.interval.toLong() * 7L
        } else {
            recurrence.interval.toLong().coerceAtLeast(1L)
        }

        val request = PeriodicWorkRequestBuilder<TaskReminderWorker>(repeatValue, repeatUnit)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.CONNECTED)
                    .build()
            )
            .setInputData(
                Data.Builder()
                    .putLong(WorkerKeys.TASK_ID, task.id)
                    .putString(WorkerKeys.TASK_TITLE, task.title)
                    .putString(WorkerKeys.ACTION_TYPE, "recurring")
                    .build()
            )
            .addTag("task-reminders")
            .addTag(recurringTag(task.id))
            .build()

        workManager.enqueueUniquePeriodicWork(
            recurringName(task.id),
            ExistingPeriodicWorkPolicy.UPDATE,
            request
        )
    }

    override fun cancelReminder(taskId: Long) {
        workManager.cancelUniqueWork(uniqueName(taskId))
        workManager.cancelUniqueWork(recurringName(taskId))
    }

    override fun cancelAll() {
        workManager.cancelAllWorkByTag("task-reminders")
    }

    private fun uniqueName(taskId: Long): String = "task-reminder-$taskId"
    private fun recurringName(taskId: Long): String = "task-recurring-$taskId"
    private fun taskTag(taskId: Long): String = "task:$taskId"
    private fun recurringTag(taskId: Long): String = "task:recurring:$taskId"
}
