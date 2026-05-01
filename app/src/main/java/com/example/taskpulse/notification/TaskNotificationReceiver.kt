package com.example.taskpulse.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast
import com.example.taskpulse.core.AppContainer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class TaskNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val pendingResult = goAsync()
        val app = AppContainer(context.applicationContext)
        val taskId = intent.getLongExtra(TaskNotificationActions.EXTRA_TASK_ID, 0L)
        val title = intent.getStringExtra(TaskNotificationActions.EXTRA_TASK_TITLE).orEmpty()
        val snoozeMinutes = intent.getIntExtra(TaskNotificationActions.EXTRA_SNOOZE_MINUTES, 15)
        if (taskId == 0L) {
            pendingResult.finish()
            return
        }

        CoroutineScope(Dispatchers.IO).launch {
            val now = System.currentTimeMillis()
            val message = when (intent.action) {
                TaskNotificationActions.ACTION_COMPLETE -> {
                    app.markTaskCompletedUseCase(taskId, now)
                    "Tarea completada: $title"
                }
                TaskNotificationActions.ACTION_SNOOZE -> {
                    app.snoozeTaskUseCase(taskId, snoozeMinutes * 60_000L, now)
                    "Tarea pospuesta: $title"
                }
                TaskNotificationActions.ACTION_OPEN -> {
                    app.markTaskInProgressUseCase(taskId, now)
                    "Abrir tarea: $title"
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
