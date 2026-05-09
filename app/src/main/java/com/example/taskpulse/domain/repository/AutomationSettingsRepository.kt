package com.example.taskpulse.domain.repository

interface AutomationSettingsRepository {
    fun getSweepIntervalHours(): Long
    fun setSweepIntervalHours(hours: Long)

    /** When true periodic sweeps ask WorkManager for unmetered (Wi‑Fi‑like) network. */
    fun isSweepUnmeteredOnly(): Boolean
    fun setSweepUnmeteredOnly(value: Boolean)

    fun isSweepRequiresCharging(): Boolean
    fun setSweepRequiresCharging(value: Boolean)
}
