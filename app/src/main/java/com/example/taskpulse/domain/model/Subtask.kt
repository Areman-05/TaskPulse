package com.example.taskpulse.domain.model

data class Subtask(
    val id: Long,
    val taskId: Long,
    val title: String,
    val completed: Boolean
)
