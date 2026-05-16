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

    fun onNoteBodyChange(value: String) {
        _uiState.update { it.copy(noteBody = value) }
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
        val state = _uiState.value
        if (state.isSaving) return
        val entryType = state.entryType
        if (entryType == TaskEntryType.NOTE && state.noteBody.trim().isEmpty()) return
        if (entryType == TaskEntryType.TASK && state.title.trim().isEmpty()) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val now = System.currentTimeMillis()
            val (title, description) = if (entryType == TaskEntryType.NOTE) {
                val body = state.noteBody.trim()
                val firstLine = body.lineSequence().firstOrNull()?.trim().orEmpty()
                val noteTitle = firstLine.ifBlank { body.take(48).trim() }
                noteTitle to body
            } else {
                state.title.trim() to state.description.trim()
            }
            val task = createDefaultTaskUseCase(
                title = title.ifBlank { " " },
                categoryId = 1L,
                nowMillis = now,
                entryType = entryType
            ).copy(
                description = description,
                priority = if (entryType == TaskEntryType.TASK) {
                    state.priority
                } else {
                    TaskPriority.MEDIUM
                },
                dueAtMillis = if (entryType == TaskEntryType.TASK && state.scheduleReminder) {
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
