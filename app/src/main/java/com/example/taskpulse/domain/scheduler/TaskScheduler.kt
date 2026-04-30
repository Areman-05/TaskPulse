package com.example.taskpulse.domain.scheduler

import com.example.taskpulse.domain.model.Task

interface TaskScheduler {
    fun scheduleReminder(task: Task)
    fun scheduleRecurring(task: Task)
    fun cancelReminder(taskId: Long)
    fun cancelAll()
}
