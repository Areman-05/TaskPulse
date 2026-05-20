package com.example.taskpulse.ui.theme

import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.model.isNote

object TaskPriorityColors {
    val Critical = Color(0xFFE57373)
    val High = Color(0xFFFFB74D)
    val Medium = TaskPulseColors.Celestial
    val Low = Color(0xFF9E9E9E)
    val Note = Color(0xFFB0B0B0)

    fun forPriority(priority: TaskPriority): Color = when (priority) {
        TaskPriority.CRITICAL -> Critical
        TaskPriority.HIGH -> High
        TaskPriority.MEDIUM -> Medium
        TaskPriority.LOW -> Low
    }

    @Composable
    fun forEntry(task: Task): Color {
        if (task.isNote) return Note
        val base = forPriority(task.priority)
        return if (task.status == TaskStatus.COMPLETED) {
            base.copy(alpha = 0.45f)
        } else {
            base
        }
    }
}
