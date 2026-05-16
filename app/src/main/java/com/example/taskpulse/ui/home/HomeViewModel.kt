package com.example.taskpulse.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.usecase.CompleteTaskAndStopRemindersUseCase
import com.example.taskpulse.domain.usecase.ObserveTasksUseCase
import com.example.taskpulse.notification.TaskNotificationHelper
import com.example.taskpulse.widget.TaskPulseWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    observeTasksUseCase: ObserveTasksUseCase,
    private val application: Application,
    private val completeTaskAndStopRemindersUseCase: CompleteTaskAndStopRemindersUseCase
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeTasksUseCase().collect { tasks ->
                _uiState.update { previous ->
                    val sorted = tasks.sortedByDescending { it.updatedAtMillis }
                    previous.copy(
                        tasks = sorted,
                        filteredTasks = applySearch(sorted, previous.searchQuery)
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(value: String) {
        _uiState.update { previous ->
            previous.copy(
                searchQuery = value,
                filteredTasks = applySearch(previous.tasks, value)
            )
        }
    }

    fun markCompleted(taskId: Long) {
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            completeTaskAndStopRemindersUseCase(taskId, now)
            TaskNotificationHelper(application).cancelReminderNotification(taskId)
            TaskPulseWidgetProvider.updatePendingCount(application)
        }
    }

    private fun applySearch(tasks: List<Task>, query: String): List<Task> {
        val q = query.trim().lowercase()
        if (q.isBlank()) return tasks
        return tasks.filter { task ->
            task.title.lowercase().contains(q) ||
                task.description.lowercase().contains(q)
        }
    }

    class Factory(
        private val observeTasksUseCase: ObserveTasksUseCase,
        private val application: Application,
        private val completeTaskAndStopRemindersUseCase: CompleteTaskAndStopRemindersUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(
                observeTasksUseCase,
                application,
                completeTaskAndStopRemindersUseCase
            ) as T
        }
    }
}
