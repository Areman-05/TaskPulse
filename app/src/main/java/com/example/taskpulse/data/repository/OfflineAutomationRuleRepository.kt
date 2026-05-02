package com.example.taskpulse.data.repository

import com.example.taskpulse.data.local.dao.AutomationDao
import com.example.taskpulse.data.mapper.toDomain
import com.example.taskpulse.data.mapper.toEntity
import com.example.taskpulse.domain.model.AutomationAction
import com.example.taskpulse.domain.model.AutomationRule
import com.example.taskpulse.domain.model.AutomationTrigger
import com.example.taskpulse.domain.repository.AutomationRuleRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineAutomationRuleRepository(
    private val automationDao: AutomationDao
) : AutomationRuleRepository {
    override fun observeRules(): Flow<List<AutomationRule>> =
        automationDao.observeRules().map { entities -> entities.map { it.toDomain() } }

    override suspend fun ensureStarterRules() {
        if (automationDao.countRules() > 0) return

        automationDao.upsertRule(
            AutomationRule(
                id = 0,
                name = "Tareas caducadas notificadas",
                enabled = true,
                trigger = AutomationTrigger.TASK_NOT_COMPLETED,
                action = AutomationAction.SEND_NOTIFICATION,
                thresholdDays = null
            ).toEntity()
        )
        automationDao.upsertRule(
            AutomationRule(
                id = 0,
                name = "Mover a revisión cuando llevan tiempo sin tocarlas",
                enabled = false,
                trigger = AutomationTrigger.TASK_STALE_DAYS,
                action = AutomationAction.MARK_AS_IN_PROGRESS,
                thresholdDays = 5
            ).toEntity()
        )
    }
}
