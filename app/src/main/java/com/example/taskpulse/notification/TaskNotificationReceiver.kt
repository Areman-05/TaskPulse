package com.example.taskpulse.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.taskpulse.core.requireAppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val app = context.requireAppContainer()
        val notifier = TaskNotificationHelper(context.applicationContext)
        val taskId = intent.getLongExtra(TaskNotificationActions.EXTRA_TASK_ID, 0L)
        val title = intent.getStringExtra(TaskNotificationActions.EXTRA_TASK_TITLE).orEmpty()
        val snoozeMinutes = intent.getIntExtra(
            TaskNotificationActions.EXTRA_SNOOZE_MINUTES,
            TaskNotificationHelper.DEFAULT_SNOOZE_MINUTES
        )
        if (taskId == 0L) {
            pendingResult.finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val now = System.currentTimeMillis()
            val message = when (intent.action) {
                TaskNotificationActions.ACTION_COMPLETE -> {
                    app.completeTaskAndStopRemindersUseCase(taskId, now)
                    notifier.cancelReminderNotification(taskId)
                    context.getString(com.example.taskpulse.R.string.notification_toast_completed, title)
                }
                TaskNotificationActions.ACTION_SNOOZE -> {
                    app.snoozeTaskAndReminderUseCase(taskId, snoozeMinutes * 60_000L, now)
                    notifier.cancelReminderNotification(taskId)
                    context.getString(com.example.taskpulse.R.string.notification_toast_snoozed, title, snoozeMinutes)
                }
                TaskNotificationActions.ACTION_OPEN -> {
                    app.markTaskInProgressUseCase(taskId, now)
                    notifier.cancelReminderNotification(taskId)
                    context.getString(com.example.taskpulse.R.string.notification_toast_opened, title)
                }
                else -> {
                    pendingResult.finish()
                    return@launch
                }
            }
            CoroutineScope(Dispatchers.Main).launch {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
                pendingResult.finish()
            }
        }
    }
}
