@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.taskpulse.ui.create

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.R
import com.example.taskpulse.domain.model.TaskEntryType
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.ui.components.TaskPulseAccentButton
import com.example.taskpulse.ui.components.TaskPulseFilterChip
import com.example.taskpulse.ui.components.TaskPulseScrollableColumn

private val FieldShape = RoundedCornerShape(10.dp)

@Composable
fun CreateTaskScreen(
    viewModel: CreateTaskViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(state.saved) {
        if (state.saved) {
            viewModel.consumeSaved()
            onBack()
        }
    }

    val canSave = when (state.entryType) {
        TaskEntryType.NOTE -> state.noteBody.trim().isNotEmpty()
        TaskEntryType.TASK -> state.title.trim().isNotEmpty()
    }

    val saveLabel = when {
        state.isSaving -> stringResource(R.string.create_task_saving)
        state.entryType == TaskEntryType.NOTE -> stringResource(R.string.create_note_save)
        else -> stringResource(R.string.create_task_save)
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (state.entryType == TaskEntryType.NOTE) {
                            stringResource(R.string.create_screen_title_note)
                        } else {
                            stringResource(R.string.create_screen_title_task)
                        },
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(
                            Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.create_task_back_cd)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.background,
                tonalElevation = 0.dp
            ) {
                TaskPulseAccentButton(
                    text = saveLabel,
                    onClick = viewModel::saveTask,
                    enabled = canSave && !state.isSaving,
                    modifier = Modifier
                        .fillMaxWidth()
                        .navigationBarsPadding()
                        .padding(horizontal = 20.dp, vertical = 12.dp)
                )
            }
        }
    ) { innerPadding ->
        TaskPulseScrollableColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            showAmbientGrid = false,
            scrollbarCompact = true,
            contentPaddingBottom = 16.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TaskPulseFilterChip(
                        selected = state.entryType == TaskEntryType.NOTE,
                        onClick = { viewModel.onEntryTypeChange(TaskEntryType.NOTE) },
                        label = { Text(stringResource(R.string.create_entry_note)) }
                    )
                    TaskPulseFilterChip(
                        selected = state.entryType == TaskEntryType.TASK,
                        onClick = { viewModel.onEntryTypeChange(TaskEntryType.TASK) },
                        label = { Text(stringResource(R.string.create_entry_task)) }
                    )
                }

                if (state.entryType == TaskEntryType.NOTE) {
                    TextField(
                        value = state.noteBody,
                        onValueChange = viewModel::onNoteBodyChange,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(420.dp),
                        placeholder = {
                            Text(
                                stringResource(R.string.create_note_placeholder),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        textStyle = MaterialTheme.typography.bodyLarge,
                        colors = noteFieldColors()
                    )
                } else {
                    OutlinedTextField(
                        value = state.title,
                        onValueChange = viewModel::onTitleChange,
                        label = { Text(stringResource(R.string.create_task_title_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        shape = FieldShape,
                        colors = fieldColors()
                    )
                    OutlinedTextField(
                        value = state.description,
                        onValueChange = viewModel::onDescriptionChange,
                        label = { Text(stringResource(R.string.create_task_description_label)) },
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(120.dp),
                        shape = FieldShape,
                        colors = fieldColors()
                    )

                    Text(
                        text = stringResource(R.string.create_task_priority_label),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(TaskPriority.CRITICAL, TaskPriority.HIGH).forEach { priority ->
                                TaskPulseFilterChip(
                                    selected = state.priority == priority,
                                    onClick = { viewModel.onPriorityChange(priority) },
                                    label = { Text(priorityLabel(priority)) }
                                )
                            }
                        }
                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf(TaskPriority.MEDIUM, TaskPriority.LOW).forEach { priority ->
                                TaskPulseFilterChip(
                                    selected = state.priority == priority,
                                    onClick = { viewModel.onPriorityChange(priority) },
                                    label = { Text(priorityLabel(priority)) }
                                )
                            }
                        }
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = stringResource(R.string.create_task_reminder_label),
                                style = MaterialTheme.typography.bodyLarge
                            )
                            Text(
                                text = stringResource(R.string.create_task_reminder_hint),
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = state.scheduleReminder,
                            onCheckedChange = viewModel::onScheduleReminderChange
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun noteFieldColors() = TextFieldDefaults.colors(
    focusedContainerColor = Color.Transparent,
    unfocusedContainerColor = Color.Transparent,
    disabledContainerColor = Color.Transparent,
    focusedIndicatorColor = Color.Transparent,
    unfocusedIndicatorColor = Color.Transparent,
    disabledIndicatorColor = Color.Transparent,
    cursorColor = MaterialTheme.colorScheme.tertiary
)

@Composable
private fun fieldColors() = OutlinedTextFieldDefaults.colors(
    focusedBorderColor = MaterialTheme.colorScheme.tertiary,
    unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
)

@Composable
private fun priorityLabel(priority: TaskPriority): String = when (priority) {
    TaskPriority.CRITICAL -> stringResource(R.string.home_priority_critical_short)
    TaskPriority.HIGH -> stringResource(R.string.home_priority_high_short)
    TaskPriority.MEDIUM -> stringResource(R.string.home_priority_medium_short)
    TaskPriority.LOW -> stringResource(R.string.home_priority_low_short)
}
