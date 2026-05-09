package com.example.taskpulse.domain.repository

import com.example.taskpulse.domain.model.AutomationSweepRun

interface AutomationSweepLogRepository {
    suspend fun recordRun(triggeredMatchCount: Int, ranAtMillis: Long = System.currentTimeMillis())

    suspend fun recentRuns(limit: Int): List<AutomationSweepRun>
}
