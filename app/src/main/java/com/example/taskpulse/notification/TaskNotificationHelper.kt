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
    private val context: Context,
    private val cooldownStore: NotificationCooldownStore = NotificationCooldownStore(context)
) {
    private val manager: NotificationManager =
        context.getSystemService(NotificationManager::class.java)

    fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            CHANNEL_ID,
            context.getString(R.string.notification_channel_reminders_title),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_reminders_description)
            setShowBadge(true)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }

    fun ensureAutomationChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            AUTOMATION_CHANNEL_ID,
            context.getString(R.string.notification_channel_automation_title),
            NotificationManager.IMPORTANCE_LOW
        ).apply {
            description = context.getString(R.string.notification_channel_automation_description)
            setShowBadge(false)
            enableVibration(false)
        }
        manager.createNotificationChannel(channel)
    }

    /**
     * @return true si se mostró; false si se omitió por cooldown.
     */
    fun showReminderIfAllowed(taskId: Long, title: String, nowMillis: Long = System.currentTimeMillis()): Boolean {
        val key = NotificationCooldownStore.reminderKey(taskId)
        if (!cooldownStore.shouldShow(key, NotificationCooldownStore.REMINDER_COOLDOWN_MS, nowMillis)) {
            return false
        }
        showReminder(taskId, title)
        cooldownStore.markShown(key, nowMillis)
        return true
    }

    fun showReminder(taskId: Long, title: String) {
        ensureChannel()
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
            .setContentTitle(context.getString(R.string.notification_reminder_title))
            .setContentText(title)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setGroup(GROUP_REMINDERS)
            .addAction(0, context.getString(R.string.notification_action_complete), completeIntent)
            .addAction(0, context.getString(R.string.notification_action_snooze), snoozeIntent)
            .setContentIntent(openIntent)
            .build()
        manager.notify(taskId.toInt(), notification)
    }

    /**
     * @return true si se mostró; false si se omitió por cooldown.
     */
    fun showAutomationAlertIfAllowed(
        ruleName: String,
        taskTitle: String,
        taskId: Long,
        ruleId: Long,
        notificationId: Int,
        nowMillis: Long = System.currentTimeMillis()
    ): Boolean {
        val key = NotificationCooldownStore.automationKey(taskId, ruleId)
        if (!cooldownStore.shouldShow(key, NotificationCooldownStore.AUTOMATION_COOLDOWN_MS, nowMillis)) {
            return false
        }
        showAutomationAlert(ruleName, taskTitle, notificationId)
        cooldownStore.markShown(key, nowMillis)
        return true
    }

    fun showAutomationAlert(ruleName: String, taskTitle: String, notificationId: Int) {
        ensureAutomationChannel()
        val notification = NotificationCompat.Builder(context, AUTOMATION_CHANNEL_ID)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(context.getString(R.string.notification_automation_title))
            .setContentText(context.getString(R.string.notification_automation_body, ruleName, taskTitle))
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setOnlyAlertOnce(true)
            .setAutoCancel(true)
            .setGroup(GROUP_AUTOMATION)
            .build()
        manager.notify(notificationId, notification)
    }

    fun cancelReminderNotification(taskId: Long) {
        manager.cancel(taskId.toInt())
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
            putExtra(TaskNotificationActions.EXTRA_SNOOZE_MINUTES, DEFAULT_SNOOZE_MINUTES)
        }
        return PendingIntent.getBroadcast(
            context,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    companion object {
        /** v2: importancia baja; el canal v1 quedaba con sonido por defecto del sistema. */
        const val CHANNEL_ID = "task_reminders_v2"
        const val AUTOMATION_CHANNEL_ID = "task_automation_v2"
        private const val GROUP_REMINDERS = "taskpulse-reminders"
        private const val GROUP_AUTOMATION = "taskpulse-automation"
        const val DEFAULT_SNOOZE_MINUTES = 60
    }
}
