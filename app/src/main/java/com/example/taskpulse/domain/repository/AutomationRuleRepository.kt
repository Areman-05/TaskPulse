package com.example.taskpulse.domain.repository

import com.example.taskpulse.domain.model.AutomationRule
import kotlinx.coroutines.flow.Flow

interface AutomationRuleRepository {
    fun observeRules(): Flow<List<AutomationRule>>
    suspend fun listRules(): List<AutomationRule>
    suspend fun setRuleEnabled(ruleId: Long, enabled: Boolean)
    suspend fun upsertRule(rule: AutomationRule): Long
    suspend fun updateRuleDefinition(rule: AutomationRule)
    suspend fun deleteRule(ruleId: Long)
    suspend fun ensureStarterRules()
}
