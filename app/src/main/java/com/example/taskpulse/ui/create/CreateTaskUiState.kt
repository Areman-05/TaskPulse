package com.example.taskpulse.ui.create

import com.example.taskpulse.domain.model.TaskEntryType
import com.example.taskpulse.domain.model.TaskPriority

data class CreateTaskUiState(
    val entryType: TaskEntryType = TaskEntryType.TASK,
    val noteBody: String = "",
    val title: String = "",
    val description: String = "",
    val priority: TaskPriority = TaskPriority.MEDIUM,
    val scheduleReminder: Boolean = true,
    val isSaving: Boolean = false,
    val saved: Boolean = false
)
