package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.repository.TaskRepository

class DeleteTasksUseCase(
    private val repository: TaskRepository,
    private val cancelTaskReminderUseCase: CancelTaskReminderUseCase
) {
    suspend operator fun invoke(taskIds: List<Long>) {
        if (taskIds.isEmpty()) return
        taskIds.forEach { cancelTaskReminderUseCase(it) }
        repository.deleteTasks(taskIds)
    }
}
