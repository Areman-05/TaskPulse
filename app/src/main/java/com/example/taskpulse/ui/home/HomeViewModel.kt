package com.example.taskpulse.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.usecase.CreateDefaultTaskUseCase
import com.example.taskpulse.domain.usecase.MarkTaskCompletedUseCase
import com.example.taskpulse.domain.usecase.ObserveDailyProductivityUseCase
import com.example.taskpulse.domain.usecase.ObserveTasksUseCase
import com.example.taskpulse.domain.usecase.ScheduleTaskReminderUseCase
import com.example.taskpulse.domain.usecase.UpsertTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    observeTasksUseCase: ObserveTasksUseCase,
    observeDailyProductivityUseCase: ObserveDailyProductivityUseCase,
    private val createDefaultTaskUseCase: CreateDefaultTaskUseCase,
    private val upsertTaskUseCase: UpsertTaskUseCase,
    private val markTaskCompletedUseCase: MarkTaskCompletedUseCase,
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeTasksUseCase().collect { tasks ->
                _uiState.update { previous ->
                    previous.copy(
                        tasks = tasks,
                        filteredTasks = applyFilter(tasks, previous.selectedFilter),
                        pendingCount = tasks.count { it.status != TaskStatus.COMPLETED },
                        completedCount = tasks.count { it.status == TaskStatus.COMPLETED }
                    )
                }
            }
        }

        viewModelScope.launch {
            observeDailyProductivityUseCase(limit = 7).collect { points ->
                _uiState.update { it.copy(productivityWeek = points.asReversed()) }
            }
        }
    }

    fun createQuickTask() {
        if (_uiState.value.isCreating) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCreating = true) }
            val now = System.currentTimeMillis()
            val task = createDefaultTaskUseCase(
                title = "Nueva tarea ${now % 10000}",
                categoryId = 1L,
                nowMillis = now
            ).copy(
                dueAtMillis = now + 60_000L
            )
            val taskId = upsertTaskUseCase(task)
            scheduleTaskReminderUseCase(task.copy(id = taskId))
            _uiState.update { it.copy(isCreating = false) }
        }
    }

    fun selectFilter(filter: HomeTaskFilter) {
        _uiState.update { previous ->
            previous.copy(
                selectedFilter = filter,
                filteredTasks = applyFilter(previous.tasks, filter)
            )
        }
    }

    fun markCompleted(taskId: Long) {
        viewModelScope.launch {
            markTaskCompletedUseCase(taskId, System.currentTimeMillis())
        }
    }

    private fun applyFilter(tasks: List<Task>, filter: HomeTaskFilter): List<Task> = when (filter) {
        HomeTaskFilter.ALL -> tasks
        HomeTaskFilter.PENDING -> tasks.filter { it.status != TaskStatus.COMPLETED }
        HomeTaskFilter.COMPLETED -> tasks.filter { it.status == TaskStatus.COMPLETED }
    }

    class Factory(
        private val observeTasksUseCase: ObserveTasksUseCase,
        private val observeDailyProductivityUseCase: ObserveDailyProductivityUseCase,
        private val createDefaultTaskUseCase: CreateDefaultTaskUseCase,
        private val upsertTaskUseCase: UpsertTaskUseCase,
        private val markTaskCompletedUseCase: MarkTaskCompletedUseCase,
        private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(
                observeTasksUseCase,
                observeDailyProductivityUseCase,
                createDefaultTaskUseCase,
                upsertTaskUseCase,
                markTaskCompletedUseCase,
                scheduleTaskReminderUseCase
            ) as T
        }
    }
}
