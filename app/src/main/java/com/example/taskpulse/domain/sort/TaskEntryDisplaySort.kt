package com.example.taskpulse.domain.sort

import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskPriority

fun Task.priorityRank(): Int = when (priority) {
    TaskPriority.CRITICAL -> 0
    TaskPriority.HIGH -> 1
    TaskPriority.MEDIUM -> 2
    TaskPriority.LOW -> 3
}

private val priorityThenNewestComparator = compareBy<Task>(Task::priorityRank)
    .thenByDescending(Task::createdAtMillis)
    .thenBy { it.title.lowercase() }

fun List<Task>.sortedByDisplayPriority(): List<Task> = sortedWith(priorityThenNewestComparator)
