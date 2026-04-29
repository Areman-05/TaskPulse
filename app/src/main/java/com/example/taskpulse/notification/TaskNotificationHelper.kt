package com.example.taskpulse.notification

import android.app.NotificationChannel
import android.app.NotificationManager
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
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle("Tarea pendiente")
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)
            .setAutoCancel(true)
            .build()
        manager.notify(taskId.toInt(), notification)
    }

    companion object {
        const val CHANNEL_ID = "task_reminders"
    }
}
