package com.example.taskpulse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.taskpulse.data.local.entity.AutomationRuleEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AutomationDao {
    @Query("SELECT COUNT(*) FROM automation_rules")
    suspend fun countRules(): Int

    @Query("SELECT * FROM automation_rules ORDER BY id ASC")
    fun observeRules(): Flow<List<AutomationRuleEntity>>

    @Query("SELECT * FROM automation_rules ORDER BY id ASC")
    suspend fun listRules(): List<AutomationRuleEntity>

    @Query("UPDATE automation_rules SET enabled = :enabled WHERE id = :ruleId")
    suspend fun setRuleEnabled(ruleId: Long, enabled: Boolean)

    @Query("DELETE FROM automation_rules WHERE id = :ruleId")
    suspend fun deleteRule(ruleId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertRule(rule: AutomationRuleEntity): Long
}
