package com.example.taskpulse.domain.sort

import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.isNote
import com.example.taskpulse.domain.model.isTaskItem

fun Task.priorityRank(): Int = when (priority) {
    TaskPriority.CRITICAL -> 0
    TaskPriority.HIGH -> 1
    TaskPriority.MEDIUM -> 2
    TaskPriority.LOW -> 3
}

private val priorityThenDueComparator = compareBy<Task>(Task::priorityRank)
    .thenBy { it.dueAtMillis ?: Long.MAX_VALUE }
    .thenBy { it.title.lowercase() }

fun List<Task>.sortedByDisplayPriority(): List<Task> = sortedWith(priorityThenDueComparator)

/** Tareas por prioridad; notas debajo, por fecha de creación (más recientes primero). */
fun List<Task>.sortedTasksThenNotes(): List<Task> {
    val tasks = filter { it.isTaskItem }.sortedByDisplayPriority()
    val notes = filter { it.isNote }.sortedByDescending { it.dueAtMillis ?: it.createdAtMillis }
    return tasks + notes
}
