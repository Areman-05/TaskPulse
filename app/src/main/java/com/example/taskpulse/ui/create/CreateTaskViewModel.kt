package com.example.taskpulse.ui.create

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.domain.calendar.TaskCalendarDates
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
import java.time.LocalDate

class CreateTaskViewModel(
    private val application: Application,
    private val createDefaultTaskUseCase: CreateDefaultTaskUseCase,
    private val upsertTaskUseCase: UpsertTaskUseCase,
    private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
    initialScheduleDate: LocalDate? = null,
    initialEntryType: TaskEntryType = TaskEntryType.TASK
) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(
        CreateTaskUiState(
            entryType = initialEntryType,
            scheduleDateEnabled = initialScheduleDate != null || initialEntryType == TaskEntryType.TASK,
            scheduleDate = initialScheduleDate ?: TaskCalendarDates.today()
        )
    )
    val uiState: StateFlow<CreateTaskUiState> = _uiState.asStateFlow()

    fun onEntryTypeChange(type: TaskEntryType) {
        _uiState.update {
            it.copy(
                entryType = type,
                reminderEnabled = if (type == TaskEntryType.NOTE) false else it.reminderEnabled,
                reminderMinutes = if (type == TaskEntryType.NOTE) 30 else it.reminderMinutes
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

    fun onScheduleDateEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(scheduleDateEnabled = enabled) }
    }

    fun onScheduleDateChange(date: LocalDate) {
        _uiState.update { it.copy(scheduleDate = date) }
    }

    fun onReminderEnabledChange(enabled: Boolean) {
        _uiState.update { it.copy(reminderEnabled = enabled) }
    }

    fun onReminderMinutesChange(minutes: Int) {
        _uiState.update { it.copy(reminderMinutes = minutes) }
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
            val hasCalendarDate = state.scheduleDateEnabled
            val dueAtMillis = resolveDueAtMillis(
                hasCalendarDate = hasCalendarDate,
                scheduleDate = state.scheduleDate,
                entryType = entryType,
                reminderEnabled = state.reminderEnabled,
                reminderMinutes = state.reminderMinutes,
                now = now
            )
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
                dueAtMillis = dueAtMillis
            )
            val taskId = upsertTaskUseCase(task)
            val savedTask = task.copy(id = taskId)
            if (entryType == TaskEntryType.TASK && state.reminderEnabled && dueAtMillis != null) {
                val fireAt = TaskCalendarDates.reminderFireAtMillis(
                    dueAtMillis = dueAtMillis,
                    offsetMinutes = state.reminderMinutes,
                    hasCalendarDate = hasCalendarDate
                )
                scheduleTaskReminderUseCase(savedTask, fireAt)
            }
            TaskPulseWidgetProvider.updatePendingCount(application)
            _uiState.update { it.copy(isSaving = false, saved = true) }
        }
    }

    private fun resolveDueAtMillis(
        hasCalendarDate: Boolean,
        scheduleDate: LocalDate,
        entryType: TaskEntryType,
        reminderEnabled: Boolean,
        reminderMinutes: Int,
        now: Long
    ): Long? = when {
        hasCalendarDate -> TaskCalendarDates.defaultDueMillis(scheduleDate)
        entryType == TaskEntryType.TASK && reminderEnabled ->
            now + reminderMinutes * 60L * 1000L
        else -> null
    }

    fun consumeSaved() {
        _uiState.update { it.copy(saved = false) }
    }

    class Factory(
        private val application: Application,
        private val createDefaultTaskUseCase: CreateDefaultTaskUseCase,
        private val upsertTaskUseCase: UpsertTaskUseCase,
        private val scheduleTaskReminderUseCase: ScheduleTaskReminderUseCase,
        private val initialScheduleDate: LocalDate? = null,
        private val initialEntryType: TaskEntryType = TaskEntryType.TASK
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CreateTaskViewModel(
                application,
                createDefaultTaskUseCase,
                upsertTaskUseCase,
                scheduleTaskReminderUseCase,
                initialScheduleDate,
                initialEntryType
            ) as T
        }
    }
}
