package com.example.taskpulse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.taskpulse.data.local.entity.AutomationRuleEntity
import com.example.taskpulse.domain.model.AutomationAction
import com.example.taskpulse.domain.model.AutomationTrigger
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationDao {
    @Query("SELECT COUNT(*) FROM automation_rules")
    suspend fun countRules(): Int

    @Query("SELECT COUNT(*) FROM automation_rules WHERE enabled = 1")
    suspend fun countEnabledRules(): Int

    @Query("SELECT * FROM automation_rules ORDER BY id ASC")
    fun observeRules(): Flow<List<AutomationRuleEntity>>

    @Query("SELECT * FROM automation_rules ORDER BY id ASC")
    suspend fun listRules(): List<AutomationRuleEntity>

    @Query("SELECT * FROM automation_rules WHERE id = :ruleId LIMIT 1")
    suspend fun getRule(ruleId: Long): AutomationRuleEntity?

    @Query("UPDATE automation_rules SET enabled = :enabled WHERE id = :ruleId")
    suspend fun setRuleEnabled(ruleId: Long, enabled: Boolean)

    @Query("DELETE FROM automation_rules WHERE id = :ruleId")
    suspend fun deleteRule(ruleId: Long)

    @Query(
        """
        UPDATE automation_rules
        SET name = :name,
            trigger = :trigger,
            action = :action,
            thresholdDays = :thresholdDays
        WHERE id = :ruleId
        """
    )
    suspend fun updateRuleDefinition(
        ruleId: Long,
        name: String,
        trigger: AutomationTrigger,
        action: AutomationAction,
        thresholdDays: Int?
    )

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(rule: AutomationRuleEntity): Long
}
