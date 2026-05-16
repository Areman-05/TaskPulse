package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskEntryType
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus

class CreateDefaultTaskUseCase {
    operator fun invoke(
        title: String,
        categoryId: Long,
        nowMillis: Long,
        entryType: TaskEntryType = TaskEntryType.TASK
    ): Task = Task(
        id = 0,
        categoryId = categoryId,
        title = title,
        description = "",
        status = TaskStatus.PENDING,
        priority = TaskPriority.MEDIUM,
        dueAtMillis = null,
        recurrence = null,
        blockedByTaskId = null,
        entryType = entryType,
        createdAtMillis = nowMillis,
        updatedAtMillis = nowMillis
    )
}
