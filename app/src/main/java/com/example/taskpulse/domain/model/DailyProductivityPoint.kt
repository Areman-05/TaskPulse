package com.example.taskpulse.domain.model

data class DailyProductivityPoint(
    val dayStartMillis: Long,
    val completedCount: Int
)
