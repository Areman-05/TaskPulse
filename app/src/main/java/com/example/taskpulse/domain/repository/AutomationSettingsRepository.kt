package com.example.taskpulse.domain.repository

interface AutomationSettingsRepository {
    fun getSweepIntervalHours(): Long
    fun setSweepIntervalHours(hours: Long)
}
