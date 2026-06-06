package com.example.taskpulse.domain.sort

import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.model.isTaskItem
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId

/**
 * Tareas visibles en "Tareas de hoy": vencen hoy, sin fecha activas, o completadas hoy.
 */
fun isTaskForToday(task: Task, zone: ZoneId = ZoneId.systemDefault()): Boolean {
    if (!task.isTaskItem) return false
    val today = LocalDate.now(zone)
    val dueDate = task.dueAtMillis?.let { Instant.ofEpochMilli(it).atZone(zone).toLocalDate() }
    val updatedDate = Instant.ofEpochMilli(task.updatedAtMillis).atZone(zone).toLocalDate()

    if (task.status == TaskStatus.COMPLETED) {
        return updatedDate == today
    }
    return dueDate == null || dueDate == today
}

fun orderTodayTasks(tasks: List<Task>): List<Task> {
    val pending = tasks.filter { it.status != TaskStatus.COMPLETED }
    val completed = tasks.filter { it.status == TaskStatus.COMPLETED }
    return pending + completed
}
