package com.example.taskpulse.domain.usecase

import com.example.taskpulse.notification.NotificationCooldownStore

class SnoozeTaskAndReminderUseCase(
    private val snoozeTaskUseCase: SnoozeTaskUseCase,
    private val getTaskUseCase: GetTaskUseCase,
    private val cancelTaskReminderUseCase: CancelTaskReminderUseCase,
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
    private val cooldownStore: NotificationCooldownStore
) {
    suspend operator fun invoke(taskId: Long, snoozeMillis: Long, nowMillis: Long) {
        snoozeTaskUseCase(taskId, snoozeMillis, nowMillis)
        cooldownStore.clear(NotificationCooldownStore.reminderKey(taskId))
        cancelTaskReminderUseCase(taskId)
        val task = getTaskUseCase(taskId) ?: return
        scheduleTaskReminderUseCase(task)
    }
}
