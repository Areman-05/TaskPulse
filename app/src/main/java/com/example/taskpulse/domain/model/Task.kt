package com.example.taskpulse.domain.model

data class Task(
    val id: Long,
    val categoryId: Long,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val dueAtMillis: Long?,
    val recurrence: TaskRecurrence?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
