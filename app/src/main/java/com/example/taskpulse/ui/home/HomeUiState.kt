package com.example.taskpulse.ui.home

import com.example.taskpulse.domain.model.DailyProductivityPoint
import com.example.taskpulse.domain.model.Task

data class HomeUiState(
    val tasks: List<Task> = emptyList(),
    val filteredTasks: List<Task> = emptyList(),
    val selectedFilter: HomeTaskFilter = HomeTaskFilter.ALL,
    val priorityFilter: HomePriorityFilter = HomePriorityFilter.ALL,
    val searchQuery: String = "",
    val pendingCount: Int = 0,
    val completedCount: Int = 0,
    val productivityWeek: List<DailyProductivityPoint> = emptyList(),
    val completionStreak: Int = 0,
    val isCreating: Boolean = false,
    val isCreatingDemoChain: Boolean = false
)
