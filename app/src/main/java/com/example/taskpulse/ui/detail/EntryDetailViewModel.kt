package com.example.taskpulse.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskEntryType
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.isNote
import com.example.taskpulse.domain.usecase.ObserveTasksUseCase
import com.example.taskpulse.domain.usecase.UpsertTaskUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class EntryDetailUiState(
    val entry: Task? = null,
    val isEditing: Boolean = false,
    val editNoteBody: String = "",
    val editTitle: String = "",
    val editDescription: String = "",
    val editPriority: TaskPriority = TaskPriority.MEDIUM,
    val isSaving: Boolean = false
)

class EntryDetailViewModel(
    private val entryId: Long,
    observeTasksUseCase: ObserveTasksUseCase,
    private val upsertTaskUseCase: UpsertTaskUseCase
) : ViewModel() {
    private val _uiState = MutableStateFlow(EntryDetailUiState())
    val uiState: StateFlow<EntryDetailUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            observeTasksUseCase().collect { tasks ->
                val entry = tasks.find { task -> task.id == entryId }
                _uiState.update { previous ->
                    val keepDraft = previous.isEditing
                    previous.copy(
                        entry = entry,
                        editNoteBody = if (keepDraft) {
                            previous.editNoteBody
                        } else {
                            entry?.let { noteBodyFromEntry(it) }.orEmpty()
                        },
                        editTitle = if (keepDraft) previous.editTitle else entry?.title.orEmpty(),
                        editDescription = if (keepDraft) {
                            previous.editDescription
                        } else {
                            entry?.description.orEmpty()
                        },
                        editPriority = if (keepDraft) {
                            previous.editPriority
                        } else {
                            entry?.priority ?: TaskPriority.MEDIUM
                        }
                    )
                }
            }
        }
    }

    fun startEditing() {
        val entry = _uiState.value.entry ?: return
        _uiState.update {
            it.copy(
                isEditing = true,
                editNoteBody = noteBodyFromEntry(entry),
                editTitle = entry.title,
                editDescription = entry.description,
                editPriority = entry.priority
            )
        }
    }

    fun cancelEditing() {
        val entry = _uiState.value.entry ?: return
        _uiState.update {
            it.copy(
                isEditing = false,
                editNoteBody = noteBodyFromEntry(entry),
                editTitle = entry.title,
                editDescription = entry.description,
                editPriority = entry.priority
            )
        }
    }

    fun onEditNoteBodyChange(value: String) {
        _uiState.update { it.copy(editNoteBody = value) }
    }

    fun onEditTitleChange(value: String) {
        _uiState.update { it.copy(editTitle = value) }
    }

    fun onEditDescriptionChange(value: String) {
        _uiState.update { it.copy(editDescription = value) }
    }

    fun onEditPriorityChange(priority: TaskPriority) {
        _uiState.update { it.copy(editPriority = priority) }
    }

    fun saveEdits() {
        val entry = _uiState.value.entry ?: return
        if (_uiState.value.isSaving) return

        viewModelScope.launch {
            _uiState.update { it.copy(isSaving = true) }
            val now = System.currentTimeMillis()
            val updated = if (entry.isNote) {
                val body = _uiState.value.editNoteBody.trim()
                if (body.isEmpty()) {
                    _uiState.update { it.copy(isSaving = false) }
                    return@launch
                }
                val firstLine = body.lineSequence().firstOrNull()?.trim().orEmpty()
                val title = firstLine.ifBlank { body.take(48).trim() }
                entry.copy(
                    title = title.ifBlank { " " },
                    description = body,
                    updatedAtMillis = now
                )
            } else {
                val title = _uiState.value.editTitle.trim()
                if (title.isEmpty()) {
                    _uiState.update { it.copy(isSaving = false) }
                    return@launch
                }
                entry.copy(
                    title = title,
                    description = _uiState.value.editDescription.trim(),
                    priority = _uiState.value.editPriority,
                    updatedAtMillis = now
                )
            }
            upsertTaskUseCase(updated)
            _uiState.update {
                it.copy(
                    isEditing = false,
                    isSaving = false,
                    editNoteBody = noteBodyFromEntry(updated),
                    editTitle = updated.title,
                    editDescription = updated.description,
                    editPriority = updated.priority
                )
            }
        }
    }

    private fun noteBodyFromEntry(entry: Task): String {
        return if (entry.entryType == TaskEntryType.NOTE) {
            entry.description.ifBlank { entry.title }
        } else {
            entry.description
        }
    }

    class Factory(
        private val entryId: Long,
        private val observeTasksUseCase: ObserveTasksUseCase,
        private val upsertTaskUseCase: UpsertTaskUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return EntryDetailViewModel(
                entryId,
                observeTasksUseCase,
                upsertTaskUseCase
            ) as T
        }
    }
}
