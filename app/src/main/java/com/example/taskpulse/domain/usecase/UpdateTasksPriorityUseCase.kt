package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.repository.TaskRepository

class UpdateTasksPriorityUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskIds: List<Long>, priority: TaskPriority) {
        if (taskIds.isEmpty()) return
        repository.updateTasksPriority(taskIds, priority, System.currentTimeMillis())
    }
}
