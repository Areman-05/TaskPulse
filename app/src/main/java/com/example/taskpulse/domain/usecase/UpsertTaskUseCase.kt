package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.repository.TaskRepository

class UpsertTaskUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(task: Task): Long = repository.upsertTask(task)
}
