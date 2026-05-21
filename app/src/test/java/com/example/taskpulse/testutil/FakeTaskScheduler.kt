package com.example.taskpulse.testutil

import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.scheduler.TaskScheduler

class FakeTaskScheduler : TaskScheduler {
    val scheduledReminders = mutableListOf<Pair<Long, Long?>>()
    val cancelledReminders = mutableListOf<Long>()

    override fun scheduleReminder(task: Task, fireAtMillis: Long?) {
        scheduledReminders.add(task.id to fireAtMillis)
    }

    override fun scheduleRecurring(task: Task) = Unit

    override fun cancelReminder(taskId: Long) {
        cancelledReminders.add(taskId)
    }

    override fun cancelAll() {
        cancelledReminders.clear()
        scheduledReminders.clear()
    }
}
