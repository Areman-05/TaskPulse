package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.scheduler.TaskScheduler

class ScheduleTaskReminderUseCase(
    private val scheduler: TaskScheduler
) {
    operator fun invoke(task: Task) {
        scheduler.scheduleReminder(task)
    }
}
