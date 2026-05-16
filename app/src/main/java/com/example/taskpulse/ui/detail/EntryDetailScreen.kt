@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.taskpulse.ui.detail

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.R
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.model.isNote
import com.example.taskpulse.domain.model.isTaskItem
import com.example.taskpulse.ui.components.TaskPulseAccentButton
import com.example.taskpulse.ui.components.TaskPulseFilterChip
import com.example.taskpulse.ui.components.TaskPulseScrollableColumn
import com.example.taskpulse.ui.create.TaskReminderIntervals
import com.example.taskpulse.ui.create.TaskReminderSelectorRow
import com.example.taskpulse.ui.create.closestReminderMinutes
import com.example.taskpulse.ui.create.taskReminderEnabled
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
                        if (entry.isTaskItem && entry.status != TaskStatus.COMPLETED) {
                            IconButton(onClick = viewModel::markTaskCompleted) {
                                Icon(
                                    imageVector = Icons.Outlined.Check,
                                    contentDescription = stringResource(R.string.detail_complete_cd),
                                    tint = MaterialTheme.colorScheme.tertiary
                                )
                            }
                        }
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
        },
        bottomBar = {
            when {
                entry != null && state.isEditing -> {
                    Surface(color = MaterialTheme.colorScheme.background) {
                        Column(
                            modifier = Modifier
                                .fillMaxWidth()
                                .navigationBarsPadding()
                                .padding(horizontal = 20.dp, vertical = 12.dp)
                        ) {
                            TaskPulseAccentButton(
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
                        }
                    }
                }
                entry != null && entry.isTaskItem && !state.isEditing -> {
                    EntryTaskMetaFooter(entry = entry)
                }
            }
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

        val bottomPadding = when {
            state.isEditing -> 120.dp
            entry.isTaskItem -> 88.dp
            else -> 32.dp
        }

        TaskPulseScrollableColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 20.dp, end = 4.dp),
            showAmbientGrid = false,
            scrollbarCompact = true,
            contentPaddingBottom = bottomPadding
        ) {
            Column(modifier = Modifier.fillMaxWidth()) {
                if (state.isEditing) {
                    EntryEditContent(state = state, viewModel = viewModel, entry = entry)
                } else {
                    EntryViewContent(entry = entry)
                }
            }
        }
    }
}

private fun canSaveEdits(state: EntryDetailUiState, entry: Task): Boolean {
    return if (entry.isNote) {
        state.editNoteBody.trim().isNotEmpty()
    } else {
        state.editTitle.trim().isNotEmpty()
    }
}

@Composable
private fun EntryCreatedDateLine(createdAtMillis: Long) {
    Text(
        text = DetailDateFormatter.format(Instant.ofEpochMilli(createdAtMillis)),
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun EntryViewContent(entry: Task) {
    if (entry.isNote) {
        EntryCreatedDateLine(createdAtMillis = entry.createdAtMillis)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = entry.description.ifBlank { entry.title },
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurface
        )
    } else {
        EntryCreatedDateLine(createdAtMillis = entry.createdAtMillis)
        Spacer(modifier = Modifier.height(12.dp))
        Text(
            text = entry.title,
            style = MaterialTheme.typography.headlineSmall,
            color = if (entry.status == TaskStatus.COMPLETED) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            }
        )
        if (entry.description.isNotBlank()) {
            Spacer(modifier = Modifier.height(16.dp))
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
    entry: Task
) {
    if (entry.isNote) {
        EntryCreatedDateLine(createdAtMillis = entry.createdAtMillis)
        Spacer(modifier = Modifier.height(12.dp))
        OutlinedTextField(
            value = state.editNoteBody,
            onValueChange = viewModel::onEditNoteBodyChange,
            modifier = Modifier
                .fillMaxWidth()
                .height(320.dp),
            placeholder = {
                Text(stringResource(R.string.create_note_placeholder))
            },
            shape = FieldShape,
            colors = fieldColors()
        )
    } else {
        EntryCreatedDateLine(createdAtMillis = entry.createdAtMillis)
        Spacer(modifier = Modifier.height(12.dp))
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskPriority.entries.forEach { priority ->
                TaskPulseFilterChip(
                    selected = state.editPriority == priority,
                    onClick = { viewModel.onEditPriorityChange(priority) },
                    label = { Text(priorityShortLabel(priority)) }
                )
            }
        }
        Spacer(modifier = Modifier.height(16.dp))
        TaskReminderSelectorRow(
            enabled = state.editReminderEnabled,
            onEnabledChange = viewModel::onEditReminderEnabledChange,
            selectedMinutes = state.editReminderMinutes,
            onMinutesSelected = viewModel::onEditReminderMinutesChange
        )
    }
}

@Composable
private fun EntryTaskMetaFooter(entry: Task) {
    val reminderLabel = if (taskReminderEnabled(entry)) {
        TaskReminderIntervals
            .find { it.minutes == closestReminderMinutesForDisplay(entry) }
            ?.let { stringResource(it.labelRes) }
            ?: stringResource(R.string.create_reminder_30min)
    } else {
        stringResource(R.string.detail_reminder_off)
    }

    Surface(color = MaterialTheme.colorScheme.background) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            )
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 12.dp, vertical = 14.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.Top
            ) {
                TaskMetaColumn(
                    label = stringResource(R.string.detail_meta_status),
                    value = taskStatusLabel(entry.status),
                    modifier = Modifier.weight(1f)
                )
                TaskMetaColumn(
                    label = stringResource(R.string.detail_meta_priority),
                    value = priorityShortLabel(entry.priority),
                    modifier = Modifier.weight(1f)
                )
                TaskMetaColumn(
                    label = stringResource(R.string.detail_meta_reminder),
                    value = reminderLabel,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TaskMetaColumn(
    label: String,
    value: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurface,
            textAlign = TextAlign.Center,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent
)

private fun closestReminderMinutesForDisplay(entry: Task): Int =
    closestReminderMinutes(entry.dueAtMillis, entry.createdAtMillis)

@Composable
private fun taskStatusLabel(status: TaskStatus): String = when (status) {
    TaskStatus.PENDING -> stringResource(R.string.detail_status_pending)
    TaskStatus.IN_PROGRESS -> stringResource(R.string.detail_status_in_progress)
    TaskStatus.COMPLETED -> stringResource(R.string.detail_status_completed)
    TaskStatus.FAILED -> stringResource(R.string.detail_status_failed)
}

@Composable
private fun priorityShortLabel(priority: TaskPriority): String = when (priority) {
    TaskPriority.CRITICAL -> stringResource(R.string.home_priority_critical_short)
    TaskPriority.HIGH -> stringResource(R.string.home_priority_high_short)
    TaskPriority.MEDIUM -> stringResource(R.string.home_priority_medium_short)
    TaskPriority.LOW -> stringResource(R.string.home_priority_low_short)
}
