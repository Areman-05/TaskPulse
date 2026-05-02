package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.repository.TaskRepository

class MarkTaskInProgressUseCase(
    private val repository: TaskRepository
) {
    suspend operator fun invoke(taskId: Long, nowMillis: Long) {
        repository.transitionTaskStatus(
            taskId = taskId,
            to = TaskStatus.IN_PROGRESS,
            nowMillis = nowMillis,
            reason = "mark_in_progress"
        )
    }
}
