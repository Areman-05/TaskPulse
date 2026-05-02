package com.example.taskpulse.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.taskpulse.domain.model.AutomationAction
import com.example.taskpulse.domain.model.AutomationTrigger

@Entity(tableName = "automation_rules")
data class AutomationRuleEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val enabled: Boolean,
    val trigger: AutomationTrigger,
    val action: AutomationAction,
    val thresholdDays: Int?
)
