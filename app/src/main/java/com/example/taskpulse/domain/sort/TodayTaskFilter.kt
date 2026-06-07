package com.example.taskpulse.domain.sort

import com.example.taskpulse.domain.calendar.TaskCalendarDates
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.model.isTaskItem

/**
 * Tareas activas en "Tareas de hoy": vencen hoy o antes, o no tienen fecha.
 * Las completadas no se muestran (aunque se editen hoy, p. ej. al archivar).
 */
fun isTaskForToday(task: Task): Boolean {
    if (!task.isTaskItem) return false
    if (task.status == TaskStatus.COMPLETED) return false

    val today = TaskCalendarDates.today()
    val dueDate = task.dueAtMillis?.let(TaskCalendarDates::toLocalDate)
    if (dueDate == null) return true
    return !dueDate.isAfter(today)
}

fun orderTodayTasks(tasks: List<Task>): List<Task> = tasks
