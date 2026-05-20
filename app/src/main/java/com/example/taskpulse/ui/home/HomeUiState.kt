package com.example.taskpulse.ui.home

import com.example.taskpulse.domain.model.Task

data class HomeUiState(
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val searchQuery: String = "",
    val viewMode: TaskViewMode = TaskViewMode.LIST,
    val selectionMode: Boolean = false,
    val selectedTaskIds: Set<Long> = emptySet(),
    val sortField: TaskSortField = TaskSortField.PRIORITY,
    val sortOrder: TaskSortOrder = TaskSortOrder.NEWEST_FIRST,
    val showDeleteConfirm: Boolean = false,
    val showPriorityPicker: Boolean = false
)
