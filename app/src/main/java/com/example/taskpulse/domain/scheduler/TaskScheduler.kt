package com.example.taskpulse.domain.scheduler

import com.example.taskpulse.domain.model.Task

interface TaskScheduler {
    fun scheduleReminder(task: Task, fireAtMillis: Long? = null)
    fun scheduleRecurring(task: Task)
    fun cancelReminder(taskId: Long)
    fun cancelAll()
}
