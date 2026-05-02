package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.repository.TaskRepository

class MarkTaskCompletedUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, nowMillis: Long) {
        repository.transitionTaskStatus(
            taskId = taskId,
            to = TaskStatus.COMPLETED,
            nowMillis = nowMillis,
            reason = "mark_completed"
        )
    }
}
