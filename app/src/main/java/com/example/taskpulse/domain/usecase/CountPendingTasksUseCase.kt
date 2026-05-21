package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.repository.TaskRepository

class CountPendingTasksUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(): Int = repository.countPendingTasks()
}
