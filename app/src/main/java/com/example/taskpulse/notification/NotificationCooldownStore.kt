package com.example.taskpulse.notification

import android.content.Context

/**
 * Evita repetir la misma notificación en intervalos cortos (p. ej. barridos horarios).
 */
class NotificationCooldownStore(context: Context) {
    private val prefs =
        context.applicationContext.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun shouldShow(key: String, cooldownMillis: Long, nowMillis: Long = System.currentTimeMillis()): Boolean {
        val last = prefs.getLong(key, 0L)
        return last == 0L || nowMillis - last >= cooldownMillis
    }

    fun markShown(key: String, nowMillis: Long = System.currentTimeMillis()) {
        prefs.edit().putLong(key, nowMillis).apply()
    }

    fun clear(key: String) {
        prefs.edit().remove(key).apply()
    }

    companion object {
        private const val PREFS_NAME = "taskpulse_notification_cooldown"

        /** Mínimo entre dos recordatorios de la misma tarea. */
        const val REMINDER_COOLDOWN_MS = 4L * 60L * 60L * 1000L // 4 h

        /** Mínimo entre avisos de automatización para la misma regla+tarea. */
        const val AUTOMATION_COOLDOWN_MS = 12L * 60L * 60L * 1000L // 12 h

        fun reminderKey(taskId: Long): String = "reminder:$taskId"

        fun automationKey(taskId: Long, ruleId: Long): String = "automation:$taskId:$ruleId"
    }
}
