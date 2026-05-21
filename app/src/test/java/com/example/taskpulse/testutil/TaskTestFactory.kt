package com.example.taskpulse.testutil

import com.example.taskpulse.domain.calendar.TaskCalendarDates
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskEntryType
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import java.time.LocalDate

object TaskTestFactory {
    fun task(
        id: Long = 1L,
        title: String = "Tarea de prueba",
        description: String = "",
        status: TaskStatus = TaskStatus.PENDING,
        priority: TaskPriority = TaskPriority.MEDIUM,
        dueAtMillis: Long? = null,
        entryType: TaskEntryType = TaskEntryType.TASK,
        createdAtMillis: Long = 1_000_000L,
        updatedAtMillis: Long = createdAtMillis,
        archivedAtMillis: Long? = null,
        categoryId: Long = 1L
    ): Task = Task(
        id = id,
        categoryId = categoryId,
        title = title,
        description = description,
        status = status,
        priority = priority,
        dueAtMillis = dueAtMillis,
        recurrence = null,
        blockedByTaskId = null,
        entryType = entryType,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis,
        archivedAtMillis = archivedAtMillis
    )

    fun dueOn(day: LocalDate): Long = TaskCalendarDates.defaultDueMillis(day)

    fun note(
        id: Long = 2L,
        title: String = "Nota",
        dueAtMillis: Long? = null,
        createdAtMillis: Long = 1_000_000L
    ): Task = task(
        id = id,
        title = title,
        entryType = TaskEntryType.NOTE,
        dueAtMillis = dueAtMillis,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = createdAtMillis
    )
}
