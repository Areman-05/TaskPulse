package com.example.taskpulse.ui.home

import com.example.taskpulse.domain.model.Task

data class HomeUiState(
    val tasks: List<Task> = emptyList(),
    val isCreating: Boolean = false
)
