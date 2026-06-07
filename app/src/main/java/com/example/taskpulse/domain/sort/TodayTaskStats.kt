package com.example.taskpulse.domain.sort

import com.example.taskpulse.domain.calendar.TaskCalendarDates
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.model.isTaskItem

data class TodayTaskStats(
    /** Porcentaje de la carga de hoy ya completada (0–100). */
    val completionPercent: Int,
    /** Tareas activas que entran en "Tareas de hoy". */
    val activeTodayCount: Int,
    /** Completadas hoy (aunque ya no aparezcan en la lista). */
    val completedTodayCount: Int
)

fun computeTodayTaskStats(tasks: List<Task>): TodayTaskStats {
    val today = TaskCalendarDates.today()
    val taskItems = tasks.filter { it.isTaskItem }
    val activeToday = taskItems.filter { isTaskForToday(it) }
    val completedToday = taskItems.count { task ->
        task.status == TaskStatus.COMPLETED &&
            TaskCalendarDates.toLocalDate(task.updatedAtMillis) == today
    }
    val workload = activeToday.size + completedToday
    val completionPercent = if (workload == 0) {
        0
    } else {
        (completedToday * 100) / workload
    }
    return TodayTaskStats(
        completionPercent = completionPercent,
        activeTodayCount = activeToday.size,
        completedTodayCount = completedToday
    )
}

/** Próxima tarea pendiente con vencimiento futuro (recordatorio o calendario). */
fun findNextUpcomingTask(tasks: List<Task>): Task? {
    val now = System.currentTimeMillis()
    return tasks
        .filter { isTaskForToday(it) && it.dueAtMillis != null && it.dueAtMillis > now }
        .minByOrNull { it.dueAtMillis!! }
}
