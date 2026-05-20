package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.repository.TaskRepository

class RestoreArchivedEntryUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long) {
        repository.restoreTask(taskId, System.currentTimeMillis())
    }
}
