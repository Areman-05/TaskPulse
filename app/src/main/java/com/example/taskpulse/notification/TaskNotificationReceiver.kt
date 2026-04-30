package com.example.taskpulse.notification

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.widget.Toast

class TaskNotificationReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val taskId = intent.getLongExtra(TaskNotificationActions.EXTRA_TASK_ID, 0L)
        val title = intent.getStringExtra(TaskNotificationActions.EXTRA_TASK_TITLE).orEmpty()
        if (taskId == 0L) return

        val message = when (intent.action) {
            TaskNotificationActions.ACTION_COMPLETE -> "Tarea completada: $title"
            TaskNotificationActions.ACTION_SNOOZE -> "Tarea pospuesta: $title"
            TaskNotificationActions.ACTION_OPEN -> "Abrir tarea: $title"
            else -> return
        }
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
    }
}
