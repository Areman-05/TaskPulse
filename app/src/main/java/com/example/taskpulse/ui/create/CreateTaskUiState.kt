package com.example.taskpulse.ui.create

import com.example.taskpulse.domain.calendar.TaskCalendarDates
import com.example.taskpulse.domain.model.TaskEntryType
import com.example.taskpulse.domain.model.TaskPriority
import java.time.LocalDate

data class CreateTaskUiState(
    val entryType: TaskEntryType = TaskEntryType.TASK,
    val noteBody: String = "",
    val title: String = "",
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val scheduleDateEnabled: Boolean = false,
    val scheduleDate: LocalDate = TaskCalendarDates.today(),
    val reminderEnabled: Boolean = true,
    val reminderMinutes: Int = 30,
    val isSaving: Boolean = false,
    val saved: Boolean = false
)
