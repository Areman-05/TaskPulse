package com.example.taskpulse.domain.model

data class TaskDetails(
    val task: Task,
    val subtasks: List<Subtask>,
    val history: List<TaskHistory>
)
