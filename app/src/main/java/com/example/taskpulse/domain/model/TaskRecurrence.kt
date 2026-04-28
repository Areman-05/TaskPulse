package com.example.taskpulse.domain.model

enum class RecurrenceUnit {
    DAY,
    WEEK,
    MONTH
}

data class TaskRecurrence(
    val interval: Int,
    val unit: RecurrenceUnit
)
