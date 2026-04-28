package com.example.taskpulse.data.mapper

import com.example.taskpulse.data.local.entity.TaskEntity
import com.example.taskpulse.data.local.relation.TaskWithDetailsEntity
import com.example.taskpulse.domain.model.RecurrenceUnit
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskDetails
import com.example.taskpulse.domain.model.TaskHistory
import com.example.taskpulse.domain.model.TaskRecurrence
import com.example.taskpulse.domain.model.Subtask

fun TaskEntity.toDomain(): Task {
    val recurrence = if (recurrenceInterval == null || recurrenceUnit == null) {
        null
    } else {
        TaskRecurrence(recurrenceInterval, RecurrenceUnit.valueOf(recurrenceUnit))
    }
    return Task(
        id = id,
        categoryId = categoryId,
        title = title,
        description = description,
        status = status,
        priority = priority,
        dueAtMillis = dueAtMillis,
        recurrence = recurrence,
        createdAtMillis = createdAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}

fun Task.toEntity(): TaskEntity = TaskEntity(
    id = id,
    categoryId = categoryId,
    title = title,
    description = description,
    status = status,
    priority = priority,
    dueAtMillis = dueAtMillis,
    recurrenceInterval = recurrence?.interval,
    recurrenceUnit = recurrence?.unit?.name,
    createdAtMillis = createdAtMillis,
    updatedAtMillis = updatedAtMillis
)

fun TaskWithDetailsEntity.toDomain(): TaskDetails = TaskDetails(
    task = task.toDomain(),
    subtasks = subtasks.map {
        Subtask(
            id = it.id,
            taskId = it.taskId,
            title = it.title,
            completed = it.completed
        )
    },
    history = history.map {
        TaskHistory(
            id = it.id,
            taskId = it.taskId,
            fromStatus = it.fromStatus,
            toStatus = it.toStatus,
            changedAtMillis = it.changedAtMillis,
            reason = it.reason
        )
    }
)
