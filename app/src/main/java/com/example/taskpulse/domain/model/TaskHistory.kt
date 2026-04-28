package com.example.taskpulse.domain.model

data class TaskHistory(
    val id: Long,
    val taskId: Long,
    val fromStatus: TaskStatus?,
    val toStatus: TaskStatus,
    val changedAtMillis: Long,
    val reason: String?
)
