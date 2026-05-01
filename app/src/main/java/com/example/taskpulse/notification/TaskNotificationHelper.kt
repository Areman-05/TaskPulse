package com.example.taskpulse.notification

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.taskpulse.R

class TaskNotificationHelper(
    private val context: Context
) {
    private val manager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            "Recordatorios de tareas",
            NotificationManager.IMPORTANCE_DEFAULT
        ).apply {
            description = "Avisos para tareas pendientes y próximas a vencer"
        }
        manager.createNotificationChannel(channel)
    }

    fun showReminder(taskId: Long, title: String) {
        val completeIntent = actionIntent(
            action = TaskNotificationActions.ACTION_COMPLETE,
            taskId = taskId,
            title = title,
            requestCode = taskId.toInt() * 10 + 1
        )
        val snoozeIntent = actionIntent(
            action = TaskNotificationActions.ACTION_SNOOZE,
            taskId = taskId,
            title = title,
            requestCode = taskId.toInt() * 10 + 2
        )
        val openIntent = actionIntent(
            action = TaskNotificationActions.ACTION_OPEN,
            taskId = taskId,
            title = title,
            requestCode = taskId.toInt() * 10 + 3
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Tarea pendiente")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .setGroup("taskpulse-reminders")
            .addAction(0, "Completar", completeIntent)
            .addAction(0, "Posponer", snoozeIntent)
            .setContentIntent(openIntent)
            .build()
        manager.notify(taskId.toInt(), notification)
    }

    private fun actionIntent(
        action: String,
        taskId: Long,
        title: String,
        requestCode: Int
    ): PendingIntent {
        val intent = Intent(context, TaskNotificationReceiver::class.java).apply {
            this.action = action
            putExtra(TaskNotificationActions.EXTRA_TASK_ID, taskId)
            putExtra(TaskNotificationActions.EXTRA_TASK_TITLE, title)
            putExtra(TaskNotificationActions.EXTRA_SNOOZE_MINUTES, 15)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        const val CHANNEL_ID = "task_reminders"
    }
}
