package com.example.taskpulse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.taskpulse.data.local.entity.AutomationSweepRunEntity

@Dao
interface AutomationSweepLogDao {
    @Insert
    suspend fun insert(run: AutomationSweepRunEntity): Long

    @Query("SELECT * FROM automation_sweep_runs ORDER BY ranAtMillis DESC LIMIT :limit")
    suspend fun recentRuns(limit: Int): List<AutomationSweepRunEntity>
}
