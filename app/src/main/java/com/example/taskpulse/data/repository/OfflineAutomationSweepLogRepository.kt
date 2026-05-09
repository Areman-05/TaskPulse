package com.example.taskpulse.data.repository

import com.example.taskpulse.data.local.dao.AutomationSweepLogDao
import com.example.taskpulse.data.local.entity.AutomationSweepRunEntity
import com.example.taskpulse.domain.model.AutomationSweepRun
import com.example.taskpulse.domain.repository.AutomationSweepLogRepository

class OfflineAutomationSweepLogRepository(
    private val dao: AutomationSweepLogDao
) : AutomationSweepLogRepository {
    override suspend fun recordRun(triggeredMatchCount: Int, ranAtMillis: Long) {
        dao.insert(
            AutomationSweepRunEntity(
                ranAtMillis = ranAtMillis,
                triggeredMatchCount = triggeredMatchCount
            )
        )
    }

    override suspend fun recentRuns(limit: Int): List<AutomationSweepRun> =
        dao.recentRuns(limit).map {
            AutomationSweepRun(
                id = it.id,
                ranAtMillis = it.ranAtMillis,
                triggeredMatchCount = it.triggeredMatchCount
            )
        }
}
