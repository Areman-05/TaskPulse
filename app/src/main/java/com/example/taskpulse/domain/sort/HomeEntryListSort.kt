package com.example.taskpulse.domain.sort

import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.isNote
import com.example.taskpulse.domain.model.isTaskItem

enum class TaskSortField {
    PRIORITY,
    EDIT_DATE,
    CREATION_DATE,
    TITLE
}

enum class TaskSortOrder {
    NEWEST_FIRST,
    OLDEST_FIRST
}

data class HomeDisplayedEntries(
    val tasks: List<Task>,
    val notes: List<Task>
)

fun filterAndPartitionHomeEntries(
    tasks: List<Task>,
    query: String,
    sortField: TaskSortField,
    sortOrder: TaskSortOrder,
    todayOnly: Boolean = true
): HomeDisplayedEntries {
    val q = query.trim().lowercase()
    val filtered = if (q.isBlank()) {
        tasks
    } else {
        tasks.filter { task ->
            task.title.lowercase().contains(q) ||
                task.description.lowercase().contains(q)
        }
    }
    val taskItems = filtered.filter { it.isTaskItem }
    val todayTasks = if (q.isBlank() && todayOnly) {
        orderTodayTasks(taskItems.filter { isTaskForToday(it) })
    } else {
        taskItems
    }
    return HomeDisplayedEntries(
        tasks = sortHomeEntries(todayTasks, sortField, sortOrder, byPriority = true),
        notes = sortHomeEntries(filtered.filter { it.isNote }, sortField, sortOrder, byPriority = false)
    )
}

private fun sortHomeEntries(
    items: List<Task>,
    sortField: TaskSortField,
    sortOrder: TaskSortOrder,
    byPriority: Boolean
): List<Task> {
    val sorted = when (sortField) {
        TaskSortField.PRIORITY -> if (byPriority) {
            items.sortedWith(
                compareBy<Task> { it.priorityRank() }
                    .thenBy { it.dueAtMillis ?: Long.MAX_VALUE }
                    .thenBy { it.title.lowercase() }
            )
        } else {
            items.sortedByDescending { it.dueAtMillis ?: it.createdAtMillis }
        }
        TaskSortField.EDIT_DATE -> items.sortedBy { it.updatedAtMillis }
        TaskSortField.CREATION_DATE -> items.sortedBy { it.createdAtMillis }
        TaskSortField.TITLE -> items.sortedBy { it.title.lowercase() }
    }
    return when {
        sortField == TaskSortField.PRIORITY && byPriority && sortOrder == TaskSortOrder.NEWEST_FIRST -> sorted
        sortField == TaskSortField.PRIORITY && byPriority -> sorted.reversed()
        sortOrder == TaskSortOrder.NEWEST_FIRST -> sorted.reversed()
        else -> sorted
    }
}
