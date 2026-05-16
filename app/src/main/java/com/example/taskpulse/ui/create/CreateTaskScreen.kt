@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.taskpulse.ui.create

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
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.R
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.ui.components.TaskPulsePrimaryButton
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

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.create_task_title),
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
        }
    ) { innerPadding ->
        TaskPulseScrollableColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp, vertical = 8.dp),
            showAmbientGrid = false,
            contentPaddingBottom = 32.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
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
                        .height(140.dp),
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
                            FilterChip(
                                selected = state.priority == priority,
                                onClick = { viewModel.onPriorityChange(priority) },
                                label = { Text(priorityLabel(priority)) }
                            )
                        }
                    }
                    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        listOf(TaskPriority.MEDIUM, TaskPriority.LOW).forEach { priority ->
                            FilterChip(
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

                Spacer(modifier = Modifier.height(8.dp))

                TaskPulsePrimaryButton(
                    text = if (state.isSaving) {
                        stringResource(R.string.create_task_saving)
                    } else {
                        stringResource(R.string.create_task_save)
                    },
                    onClick = viewModel::saveTask,
                    enabled = state.title.isNotBlank() && !state.isSaving
                )
            }
        }
    }
}

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
