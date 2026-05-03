package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.repository.TaskRepository

class MarkTaskFailedUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, nowMillis: Long, reason: String?) {
        repository.transitionTaskStatus(
            taskId = taskId,
            to = TaskStatus.FAILED,
            nowMillis = nowMillis,
            reason = reason
        )
    }
}
