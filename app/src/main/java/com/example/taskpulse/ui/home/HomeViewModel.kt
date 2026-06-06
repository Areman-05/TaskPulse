package com.example.taskpulse.ui.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.model.isTaskItem
import com.example.taskpulse.domain.sort.TaskSortField
import com.example.taskpulse.domain.sort.TaskSortOrder
import com.example.taskpulse.domain.sort.filterAndPartitionHomeEntries
import com.example.taskpulse.domain.usecase.CompleteTaskAndStopRemindersUseCase
import com.example.taskpulse.domain.usecase.DeleteTasksUseCase
import com.example.taskpulse.domain.usecase.ObserveTasksUseCase
import com.example.taskpulse.domain.usecase.UpdateTasksPriorityUseCase
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
    private val deleteTasksUseCase: DeleteTasksUseCase,
    private val updateTasksPriorityUseCase: UpdateTasksPriorityUseCase,
    private val completeTaskAndStopRemindersUseCase: CompleteTaskAndStopRemindersUseCase
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(HomeUiState())
    val uiState: StateFlow<HomeUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeTasksUseCase().collect { tasks ->
                _uiState.update { previous ->
                    val entries = filterAndPartitionHomeEntries(
                        tasks = tasks,
                        query = previous.searchQuery,
                        sortField = previous.sortField,
                        sortOrder = previous.sortOrder
                    )
                    previous.copy(
                        tasks = tasks,
                        filteredTasks = entries.tasks,
                        filteredNotes = entries.notes
                    )
                }
            }
        }
    }

    fun onSearchQueryChange(value: String) {
        _uiState.update { previous ->
            val entries = filterAndPartitionHomeEntries(
                tasks = previous.tasks,
                query = value,
                sortField = previous.sortField,
                sortOrder = previous.sortOrder
            )
            previous.copy(
                searchQuery = value,
                filteredTasks = entries.tasks,
                filteredNotes = entries.notes
            )
        }
    }

    fun setViewMode(mode: TaskViewMode) {
        _uiState.update { it.copy(viewMode = mode) }
    }

    fun setShowAllNotes(showAll: Boolean) {
        _uiState.update { it.copy(showAllNotes = showAll) }
    }

    fun toggleSelectionMode() {
        _uiState.update { previous ->
            if (previous.selectionMode) {
                previous.copy(selectionMode = false, selectedTaskIds = emptySet())
            } else {
                previous.copy(selectionMode = true, selectedTaskIds = emptySet())
            }
        }
    }

    fun exitSelectionMode() {
        _uiState.update {
            it.copy(
                selectionMode = false,
                selectedTaskIds = emptySet(),
                showDeleteConfirm = false,
                showPriorityPicker = false
            )
        }
    }

    fun onTaskClick(taskId: Long) {
        _uiState.update { previous ->
            if (!previous.selectionMode) return@update previous
            val next = previous.selectedTaskIds.toMutableSet()
            if (taskId in next) next.remove(taskId) else next.add(taskId)
            previous.copy(selectedTaskIds = next)
        }
    }

    fun setSortField(field: TaskSortField) {
        _uiState.update { previous ->
            val entries = filterAndPartitionHomeEntries(
                tasks = previous.tasks,
                query = previous.searchQuery,
                sortField = field,
                sortOrder = previous.sortOrder
            )
            previous.copy(
                sortField = field,
                filteredTasks = entries.tasks,
                filteredNotes = entries.notes
            )
        }
    }

    fun setSortOrder(order: TaskSortOrder) {
        _uiState.update { previous ->
            val entries = filterAndPartitionHomeEntries(
                tasks = previous.tasks,
                query = previous.searchQuery,
                sortField = previous.sortField,
                sortOrder = order
            )
            previous.copy(
                sortOrder = order,
                filteredTasks = entries.tasks,
                filteredNotes = entries.notes
            )
        }
    }

    fun requestDeleteSelected() {
        if (_uiState.value.selectedTaskIds.isEmpty()) return
        _uiState.update { it.copy(showDeleteConfirm = true) }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(showDeleteConfirm = false) }
    }

    fun confirmDeleteSelected() {
        val ids = _uiState.value.selectedTaskIds.toList()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            ids.forEach { TaskNotificationHelper(application).cancelReminderNotification(it) }
            deleteTasksUseCase(ids)
            TaskPulseWidgetProvider.updatePendingCount(application)
            exitSelectionMode()
        }
    }

    fun showPriorityPicker() {
        if (selectedTaskIdsOnly().isEmpty()) return
        _uiState.update { it.copy(showPriorityPicker = true) }
    }

    fun dismissPriorityPicker() {
        _uiState.update { it.copy(showPriorityPicker = false) }
    }

    fun applyPriorityToSelected(priority: TaskPriority) {
        val taskIds = selectedTaskIdsOnly()
        if (taskIds.isEmpty()) return
        viewModelScope.launch {
            updateTasksPriorityUseCase(taskIds, priority)
            exitSelectionMode()
        }
    }

    fun completeSelectedTasks() {
        val ids = selectedCompletableTaskIds()
        if (ids.isEmpty()) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            ids.forEach { taskId ->
                completeTaskAndStopRemindersUseCase(taskId, now)
                TaskNotificationHelper(application).cancelReminderNotification(taskId)
            }
            TaskPulseWidgetProvider.updatePendingCount(application)
            exitSelectionMode()
        }
    }

    fun markTaskCompleted(taskId: Long) {
        if (_uiState.value.selectionMode) return
        val task = _uiState.value.tasks.find { it.id == taskId } ?: return
        if (!task.isTaskItem || task.status == TaskStatus.COMPLETED) return
        viewModelScope.launch {
            val now = System.currentTimeMillis()
            completeTaskAndStopRemindersUseCase(taskId, now)
            TaskNotificationHelper(application).cancelReminderNotification(taskId)
            TaskPulseWidgetProvider.updatePendingCount(application)
        }
    }

    fun deleteTask(taskId: Long) {
        if (_uiState.value.selectionMode) return
        viewModelScope.launch {
            TaskNotificationHelper(application).cancelReminderNotification(taskId)
            deleteTasksUseCase(listOf(taskId))
            TaskPulseWidgetProvider.updatePendingCount(application)
        }
    }

    private fun selectedCompletableTaskIds(): List<Long> {
        val selected = _uiState.value.selectedTaskIds
        return _uiState.value.tasks
            .filter { it.id in selected && it.isTaskItem && it.status != TaskStatus.COMPLETED }
            .map { it.id }
    }

    private fun selectedTaskIdsOnly(): List<Long> {
        val selected = _uiState.value.selectedTaskIds
        return _uiState.value.tasks
            .filter { it.id in selected && it.isTaskItem }
            .map { it.id }
    }

    class Factory(
        private val observeTasksUseCase: ObserveTasksUseCase,
        private val application: Application,
        private val deleteTasksUseCase: DeleteTasksUseCase,
        private val updateTasksPriorityUseCase: UpdateTasksPriorityUseCase,
        private val completeTaskAndStopRemindersUseCase: CompleteTaskAndStopRemindersUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return HomeViewModel(
                observeTasksUseCase,
                application,
                deleteTasksUseCase,
                updateTasksPriorityUseCase,
                completeTaskAndStopRemindersUseCase
            ) as T
        }
    }
}
