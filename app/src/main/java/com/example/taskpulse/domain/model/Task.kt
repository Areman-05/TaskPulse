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
    /**
     * If set, reminders for this task wait until [blockedByTaskId] enters [TaskStatus.COMPLETED].
     */
    val blockedByTaskId: Long? = null,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
