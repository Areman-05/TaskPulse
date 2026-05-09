package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.repository.TaskRepository

/**
 * When a blocking task completes, dependent tasks can finally surface reminders.
 */
class ScheduleDependentRemindersUseCase(
    private val repository: TaskRepository,
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
    private val scheduleRecurringTaskUseCase: ScheduleRecurringTaskUseCase
) {
    suspend operator fun invoke(completedBlockerTaskId: Long) {
        val dependents = repository.listTasksBlockedBy(completedBlockerTaskId)
        dependents.forEach { dependent ->
            if (dependent.recurrence != null) {
                scheduleRecurringTaskUseCase(dependent)
            } else {
                scheduleTaskReminderUseCase(dependent)
            }
        }
    }
}
