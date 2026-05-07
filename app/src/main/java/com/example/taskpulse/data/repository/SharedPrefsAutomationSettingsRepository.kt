package com.example.taskpulse.data.repository

import android.content.Context
import com.example.taskpulse.domain.repository.AutomationSettingsRepository

class SharedPrefsAutomationSettingsRepository(
    context: Context
) : AutomationSettingsRepository {
    private val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    override fun getSweepIntervalHours(): Long {
        return prefs.getLong(KEY_SWEEP_INTERVAL_HOURS, DEFAULT_SWEEP_INTERVAL_HOURS)
    }

    override fun setSweepIntervalHours(hours: Long) {
        prefs.edit().putLong(KEY_SWEEP_INTERVAL_HOURS, hours).apply()
    }

    private companion object {
        const val PREFS_NAME = "taskpulse_automation_settings"
        const val KEY_SWEEP_INTERVAL_HOURS = "sweep_interval_hours"
        const val DEFAULT_SWEEP_INTERVAL_HOURS = 1L
    }
}
