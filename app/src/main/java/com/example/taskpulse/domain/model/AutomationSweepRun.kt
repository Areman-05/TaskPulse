package com.example.taskpulse.domain.model

data class AutomationSweepRun(
    val id: Long,
    val ranAtMillis: Long,
    val triggeredMatchCount: Int
)
