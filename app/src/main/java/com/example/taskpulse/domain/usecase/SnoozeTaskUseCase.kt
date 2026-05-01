package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.repository.TaskRepository

class SnoozeTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, snoozeMillis: Long, nowMillis: Long) {
        repository.updateTaskDueDate(taskId, nowMillis + snoozeMillis, nowMillis)
    }
}
