package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class ObserveAllTasksUseCase(
    private val repository: TaskRepository
) {
    operator fun invoke(): Flow<List<Task>> = repository.observeAllTasks()
}
