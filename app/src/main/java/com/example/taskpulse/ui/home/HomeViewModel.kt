package com.example.taskpulse.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.data.repository.SharedPreferencesThemeRepository
import com.example.taskpulse.domain.metrics.ProductivityStreakCalculator
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.usecase.CreateDefaultTaskUseCase
import com.example.taskpulse.domain.usecase.CompleteTaskAndStopRemindersUseCase
import com.example.taskpulse.domain.usecase.ObserveDailyProductivityUseCase
import com.example.taskpulse.domain.usecase.ObserveTasksUseCase
import com.example.taskpulse.domain.usecase.ScheduleTaskReminderUseCase
import com.example.taskpulse.notification.TaskNotificationHelper
import com.example.taskpulse.domain.usecase.UpsertTaskUseCase
import com.example.taskpulse.widget.TaskPulseWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class HomeViewModel(
    observeTasksUseCase: ObserveTasksUseCase,
    observeDailyProductivityUseCase: ObserveDailyProductivityUseCase,
    private val application: Application,
    private val createDefaultTaskUseCase: CreateDefaultTaskUseCase,
    private val upsertTaskUseCase: UpsertTaskUseCase,
    private val completeTaskAndStopRemindersUseCase: CompleteTaskAndStopRemindersUseCase,
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
    private val themeRepository: SharedPreferencesThemeRepository
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeTasksUseCase().collect { tasks ->
                _uiState.update { previous ->
                    val filtered = applyCombinedFilters(
                        tasks = tasks,
                        filter = previous.selectedFilter,
                        priority = previous.priorityFilter,
                        query = previous.searchQuery
                    )
                    previous.copy(
                        tasks = tasks,
                        filteredTasks = filtered,
                        pendingCount = tasks.count { it.status != TaskStatus.COMPLETED },
                        completedCount = tasks.count { it.status == TaskStatus.COMPLETED }
                    )
                }
            }
        }

        viewModelScope.launch {
            observeDailyProductivityUseCase(limit = 30).collect { points ->
                val streak = ProductivityStreakCalculator.currentStreak(
                    productivityPoints = points,
                    nowMillis = System.currentTimeMillis()
                )
                _uiState.update {
                    it.copy(
                        productivityWeek = points.take(7).reversed(),
                        completionStreak = streak
                    )
                }
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
                dueAtMillis = now + 30L * 60L * 1000L
            )
            val taskId = upsertTaskUseCase(task)
            scheduleTaskReminderUseCase(task.copy(id = taskId))
            _uiState.update { it.copy(isCreating = false) }
            TaskPulseWidgetProvider.updatePendingCount(application)
        }
    }

    fun createChainedDemoTasks() {
        if (_uiState.value.isCreatingDemoChain) return
        viewModelScope.launch {
            _uiState.update { it.copy(isCreatingDemoChain = true) }
            val now = System.currentTimeMillis()
            val blocker = createDefaultTaskUseCase(
                title = "Bloqueante ${now % 1000}",
                categoryId = 1L,
                nowMillis = now
            ).copy(dueAtMillis = now + 90_000L)
            val blockerId = upsertTaskUseCase(blocker)
            scheduleTaskReminderUseCase(blocker.copy(id = blockerId))

            val dependent = createDefaultTaskUseCase(
                title = "Dependiente (espera bloqueante)",
                categoryId = 1L,
                nowMillis = now
            ).copy(
                dueAtMillis = now + 120_000L,
                blockedByTaskId = blockerId
            )
            val depId = upsertTaskUseCase(dependent)
            scheduleTaskReminderUseCase(dependent.copy(id = depId))
            _uiState.update { it.copy(isCreatingDemoChain = false) }
            TaskPulseWidgetProvider.updatePendingCount(application)
        }
    }

    fun cycleTheme() {
        themeRepository.cyclePreferredMode()
    }

    fun selectFilter(filter: HomeTaskFilter) {
        _uiState.update { previous ->
            val filtered = applyCombinedFilters(
                tasks = previous.tasks,
                filter = filter,
                priority = previous.priorityFilter,
                query = previous.searchQuery
            )
            previous.copy(selectedFilter = filter, filteredTasks = filtered)
        }
    }

    fun selectPriorityFilter(priority: HomePriorityFilter) {
        _uiState.update { previous ->
            val filtered = applyCombinedFilters(
                tasks = previous.tasks,
                filter = previous.selectedFilter,
                priority = priority,
                query = previous.searchQuery
            )
            previous.copy(priorityFilter = priority, filteredTasks = filtered)
        }
    }

    fun onSearchQueryChange(value: String) {
        _uiState.update { previous ->
            val filtered = applyCombinedFilters(
                tasks = previous.tasks,
                filter = previous.selectedFilter,
                priority = previous.priorityFilter,
                query = value
            )
            previous.copy(searchQuery = value, filteredTasks = filtered)
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

    private fun applyCombinedFilters(
        tasks: List<Task>,
        filter: HomeTaskFilter,
        priority: HomePriorityFilter,
        query: String
    ): List<Task> {
        val q = query.trim().lowercase()
        return tasks
            .asSequence()
            .filter { task ->
                when (filter) {
                    HomeTaskFilter.ALL -> true
                    HomeTaskFilter.PENDING -> task.status != TaskStatus.COMPLETED
                    HomeTaskFilter.COMPLETED -> task.status == TaskStatus.COMPLETED
                }
            }
            .filter { task ->
                when (priority) {
                    HomePriorityFilter.ALL -> true
                    HomePriorityFilter.CRITICAL -> task.priority == TaskPriority.CRITICAL
                    HomePriorityFilter.HIGH -> task.priority == TaskPriority.HIGH
                    HomePriorityFilter.MEDIUM -> task.priority == TaskPriority.MEDIUM
                    HomePriorityFilter.LOW -> task.priority == TaskPriority.LOW
                }
            }
            .filter { task ->
                if (q.isBlank()) true
                else {
                    task.title.lowercase().contains(q) ||
                        task.description.lowercase().contains(q)
                }
            }
            .toList()
    }

    class Factory(
        private val observeTasksUseCase: ObserveTasksUseCase,
        private val observeDailyProductivityUseCase: ObserveDailyProductivityUseCase,
        private val application: Application,
        private val createDefaultTaskUseCase: CreateDefaultTaskUseCase,
        private val upsertTaskUseCase: UpsertTaskUseCase,
        private val completeTaskAndStopRemindersUseCase: CompleteTaskAndStopRemindersUseCase,
        private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
        private val themeRepository: SharedPreferencesThemeRepository
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(
                observeTasksUseCase,
                observeDailyProductivityUseCase,
                application,
                createDefaultTaskUseCase,
                upsertTaskUseCase,
                completeTaskAndStopRemindersUseCase,
                scheduleTaskReminderUseCase,
                themeRepository
            ) as T
        }
    }
}
