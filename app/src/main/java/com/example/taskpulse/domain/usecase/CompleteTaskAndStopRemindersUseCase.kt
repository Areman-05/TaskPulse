package com.example.taskpulse.domain.usecase

import com.example.taskpulse.notification.NotificationCooldownStore

class CompleteTaskAndStopRemindersUseCase(
    private val markTaskCompletedUseCase: MarkTaskCompletedUseCase,
    private val cancelTaskReminderUseCase: CancelTaskReminderUseCase,
    private val scheduleDependentRemindersUseCase: ScheduleDependentRemindersUseCase,
    private val cooldownStore: NotificationCooldownStore
) {
    suspend operator fun invoke(taskId: Long, nowMillis: Long) {
        markTaskCompletedUseCase(taskId, nowMillis)
        cancelTaskReminderUseCase(taskId)
        cooldownStore.clear(NotificationCooldownStore.reminderKey(taskId))
        scheduleDependentRemindersUseCase(taskId)
    }
}
