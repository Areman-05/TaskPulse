@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.taskpulse.ui.detail

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.R
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.isNote
import com.example.taskpulse.domain.model.isTaskItem
import com.example.taskpulse.ui.components.TaskPulsePrimaryButton
import com.example.taskpulse.ui.components.TaskPulseScrollableColumn
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DetailDateFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm").withZone(ZoneId.systemDefault())

private val FieldShape = RoundedCornerShape(10.dp)

@Composable
fun EntryDetailScreen(
    viewModel: EntryDetailViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val entry = state.entry

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = when {
                            entry == null -> stringResource(R.string.detail_loading)
                            state.isEditing -> stringResource(R.string.detail_edit_title)
                            entry.isNote -> stringResource(R.string.detail_note_title)
                            else -> stringResource(R.string.detail_task_title)
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (state.isEditing) viewModel.cancelEditing() else onBack()
                    }) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.create_task_back_cd)
                        )
                    }
                },
                actions = {
                    if (entry != null && !state.isEditing) {
                        IconButton(onClick = viewModel::startEditing) {
                            Icon(
                                imageVector = Icons.Outlined.Edit,
                                contentDescription = stringResource(R.string.detail_edit_cd)
                            )
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { innerPadding ->
        if (entry == null) {
            Text(
                text = stringResource(R.string.detail_not_found),
                modifier = Modifier.padding(innerPadding).padding(20.dp),
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            return@Scaffold
        }

        TaskPulseScrollableColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 20.dp, end = 4.dp),
            showAmbientGrid = false,
            scrollbarCompact = true,
            contentPaddingBottom = 32.dp
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (state.isEditing) {
                    EntryEditContent(state = state, viewModel = viewModel, entry = entry)
                    Spacer(modifier = Modifier.height(20.dp))
                    TaskPulsePrimaryButton(
                        text = if (state.isSaving) {
                            stringResource(R.string.create_task_saving)
                        } else {
                            stringResource(R.string.detail_save)
                        },
                        onClick = viewModel::saveEdits,
                        enabled = !state.isSaving && canSaveEdits(state, entry)
                    )
                    TextButton(
                        onClick = viewModel::cancelEditing,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(stringResource(R.string.detail_cancel_edit))
                    }
                } else {
                    EntryViewContent(entry = entry)
                }

                if (!state.isEditing) {
                    Spacer(modifier = Modifier.height(24.dp))
                    EntryMetaSection(entry = entry)
                }
            }
        }
    }
}

private fun canSaveEdits(state: EntryDetailUiState, entry: com.example.taskpulse.domain.model.Task): Boolean {
    return if (entry.isNote) {
        state.editNoteBody.trim().isNotEmpty()
    } else {
        state.editTitle.trim().isNotEmpty()
    }
}

@Composable
private fun EntryViewContent(entry: com.example.taskpulse.domain.model.Task) {
    if (entry.isNote) {
        Text(
            text = entry.description.ifBlank { entry.title },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    } else {
        Text(
            text = entry.title,
            style = MaterialTheme.typography.headlineSmall,
            color = MaterialTheme.colorScheme.onSurface
        )
        if (entry.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = stringResource(R.string.detail_notes_section),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = entry.description,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun EntryEditContent(
    state: EntryDetailUiState,
    viewModel: EntryDetailViewModel,
    entry: com.example.taskpulse.domain.model.Task
) {
    if (entry.isNote) {
        OutlinedTextField(
            value = state.editNoteBody,
            onValueChange = viewModel::onEditNoteBodyChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(280.dp),
            placeholder = {
                Text(stringResource(R.string.create_note_placeholder))
            },
            shape = FieldShape,
            colors = fieldColors()
        )
    } else {
        OutlinedTextField(
            value = state.editTitle,
            onValueChange = viewModel::onEditTitleChange,
            label = { Text(stringResource(R.string.create_task_title_label)) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            shape = FieldShape,
            colors = fieldColors()
        )
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = state.editDescription,
            onValueChange = viewModel::onEditDescriptionChange,
            label = { Text(stringResource(R.string.create_task_description_label)) },
            modifier = Modifier
                .fillMaxWidth()
                .height(160.dp),
            shape = FieldShape,
            colors = fieldColors()
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(R.string.create_task_priority_label),
            style = MaterialTheme.typography.labelLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(8.dp))
        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(TaskPriority.CRITICAL, TaskPriority.HIGH).forEach { priority ->
                    FilterChip(
                        selected = state.editPriority == priority,
                        onClick = { viewModel.onEditPriorityChange(priority) },
                        label = { Text(priorityLabel(priority)) }
                    )
                }
            }
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                listOf(TaskPriority.MEDIUM, TaskPriority.LOW).forEach { priority ->
                    FilterChip(
                        selected = state.editPriority == priority,
                        onClick = { viewModel.onEditPriorityChange(priority) },
                        label = { Text(priorityLabel(priority)) }
                    )
                }
            }
        }
    }
}

@Composable
private fun EntryMetaSection(entry: com.example.taskpulse.domain.model.Task) {
    Text(
        text = stringResource(R.string.detail_meta_section),
        style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Spacer(modifier = Modifier.height(8.dp))
    if (entry.isTaskItem) {
        Text(
            text = stringResource(R.string.detail_status_line, entry.status.name),
            style = MaterialTheme.typography.bodyMedium
        )
        Text(
            text = stringResource(R.string.detail_priority_line, entry.priority.name),
            style = MaterialTheme.typography.bodyMedium
        )
    }
    Text(
        text = stringResource(
            R.string.detail_created_line,
            DetailDateFormatter.format(Instant.ofEpochMilli(entry.createdAtMillis))
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
        text = stringResource(
            R.string.detail_edited_line,
            DetailDateFormatter.format(Instant.ofEpochMilli(entry.updatedAtMillis))
        ),
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)

@Composable
private fun priorityLabel(priority: TaskPriority): String = when (priority) {
    TaskPriority.CRITICAL -> stringResource(R.string.home_priority_critical_short)
    TaskPriority.HIGH -> stringResource(R.string.home_priority_high_short)
    TaskPriority.MEDIUM -> stringResource(R.string.home_priority_medium_short)
    TaskPriority.LOW -> stringResource(R.string.home_priority_low_short)
}
