package com.example.taskpulse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "automation_sweep_runs")
data class AutomationSweepRunEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val ranAtMillis: Long,
    val triggeredMatchCount: Int
)
