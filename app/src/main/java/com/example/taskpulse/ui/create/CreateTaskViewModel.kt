package com.example.taskpulse.ui.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.domain.model.TaskEntryType
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.usecase.CreateDefaultTaskUseCase
import com.example.taskpulse.domain.usecase.ScheduleTaskReminderUseCase
import com.example.taskpulse.domain.usecase.UpsertTaskUseCase
import com.example.taskpulse.widget.TaskPulseWidgetProvider
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class CreateTaskViewModel(
    private val application: Application,
    private val createDefaultTaskUseCase: CreateDefaultTaskUseCase,
    private val upsertTaskUseCase: UpsertTaskUseCase,
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(CreateTaskUiState())
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    fun onEntryTypeChange(type: TaskEntryType) {
        _uiState.update {
            it.copy(
                entryType = type,
                scheduleReminder = if (type == TaskEntryType.NOTE) false else it.scheduleReminder
            )
        }
    }

    fun onTitleChange(value: String) {
        _uiState.update { it.copy(title = value) }
    }

    fun onDescriptionChange(value: String) {
        _uiState.update { it.copy(description = value) }
    }

    fun onPriorityChange(priority: TaskPriority) {
        _uiState.update { it.copy(priority = priority) }
    }

    fun onScheduleReminderChange(enabled: Boolean) {
        _uiState.update { it.copy(scheduleReminder = enabled) }
    }

    fun saveTask() {
        val title = _uiState.value.title.trim()
        if (title.isEmpty() || _uiState.value.isSaving) return
        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val now = System.currentTimeMillis()
            val entryType = _uiState.value.entryType
            val task = createDefaultTaskUseCase(
                title = title,
                categoryId = 1L,
                nowMillis = now,
                entryType = entryType
            ).copy(
                description = _uiState.value.description.trim(),
                priority = if (entryType == TaskEntryType.TASK) {
                    _uiState.value.priority
                } else {
                    TaskPriority.MEDIUM
                },
                dueAtMillis = if (entryType == TaskEntryType.TASK && _uiState.value.scheduleReminder) {
                    now + 30L * 60L * 1000L
                } else {
                    null
                }
            )
            val taskId = upsertTaskUseCase(task)
            val savedTask = task.copy(id = taskId)
            if (entryType == TaskEntryType.TASK && _uiState.value.scheduleReminder) {
                scheduleTaskReminderUseCase(savedTask)
            }
            TaskPulseWidgetProvider.updatePendingCount(application)
            _uiState.update { it.copy(isSaving = false, saved = true) }
        }
    }

    fun consumeSaved() {
        _uiState.update { it.copy(saved = false) }
    }

    class Factory(
        private val application: Application,
        private val createDefaultTaskUseCase: CreateDefaultTaskUseCase,
        private val upsertTaskUseCase: UpsertTaskUseCase,
        private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateTaskViewModel(
                application,
                createDefaultTaskUseCase,
                upsertTaskUseCase,
                scheduleTaskReminderUseCase
            ) as T
        }
    }
}
