package com.example.taskpulse.ui.settings.archive

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.usecase.CancelTaskReminderUseCase
import com.example.taskpulse.domain.usecase.DeleteTasksUseCase
import com.example.taskpulse.domain.usecase.ObserveArchivedTasksUseCase
import com.example.taskpulse.domain.usecase.RestoreArchivedEntryUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class ArchiveUiState(
    val entries: List<Task> = emptyList(),
    val confirmDeleteId: Long? = null
)

class ArchiveViewModel(
    observeArchivedTasksUseCase: ObserveArchivedTasksUseCase,
    private val restoreArchivedEntryUseCase: RestoreArchivedEntryUseCase,
    private val deleteTasksUseCase: DeleteTasksUseCase,
    private val cancelTaskReminderUseCase: CancelTaskReminderUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(ArchiveUiState())
    val uiState: StateFlow<ArchiveUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeArchivedTasksUseCase().collect { entries ->
                _uiState.update { it.copy(entries = entries) }
            }
        }
    }

    fun restore(entryId: Long) {
        viewModelScope.launch {
            restoreArchivedEntryUseCase(entryId)
        }
    }

    fun requestDelete(entryId: Long) {
        _uiState.update { it.copy(confirmDeleteId = entryId) }
    }

    fun dismissDeleteConfirm() {
        _uiState.update { it.copy(confirmDeleteId = null) }
    }

    fun confirmDelete() {
        val id = _uiState.value.confirmDeleteId ?: return
        viewModelScope.launch {
            cancelTaskReminderUseCase(id)
            deleteTasksUseCase(listOf(id))
            dismissDeleteConfirm()
        }
    }

    class Factory(
        private val observeArchivedTasksUseCase: ObserveArchivedTasksUseCase,
        private val restoreArchivedEntryUseCase: RestoreArchivedEntryUseCase,
        private val deleteTasksUseCase: DeleteTasksUseCase,
        private val cancelTaskReminderUseCase: CancelTaskReminderUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return ArchiveViewModel(
                observeArchivedTasksUseCase,
                restoreArchivedEntryUseCase,
                deleteTasksUseCase,
                cancelTaskReminderUseCase
            ) as T
        }
    }
}
