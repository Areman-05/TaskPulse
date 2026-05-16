package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.scheduler.TaskScheduler

class CancelTaskReminderUseCase(
    private val scheduler: TaskScheduler
) {
    operator fun invoke(taskId: Long) {
        scheduler.cancelReminder(taskId)
    }
}
