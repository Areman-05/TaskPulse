@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.taskpulse.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.R
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.ui.components.TaskPulseScrollableColumn
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val NoteDateFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm").withZone(ZoneId.systemDefault())

private val SearchShape = RoundedCornerShape(10.dp)
private val TaskCardShape = RoundedCornerShape(12.dp)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCreate: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    HomeSelectionDialogs(state = state, viewModel = viewModel)

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                HomeTopBar(state = state, viewModel = viewModel)
            }
        ) { innerPadding ->
            TaskPulseScrollableColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(start = 20.dp, end = 8.dp),
                contentPaddingBottom = if (state.selectionMode) 160.dp else 100.dp
            ) {
                Column(modifier = Modifier.fillMaxWidth()) {
                    OutlinedTextField(
                        value = state.searchQuery,
                        onValueChange = viewModel::onSearchQueryChange,
                        placeholder = {
                            Text(
                                stringResource(R.string.home_search_label),
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(bottom = 12.dp),
                        singleLine = true,
                        shape = SearchShape,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.25f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.2f)
                        )
                    )

                    when {
                        state.filteredTasks.isEmpty() && state.searchQuery.isBlank() -> {
                            Text(
                                text = stringResource(R.string.home_empty_tasks),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 48.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        state.filteredTasks.isEmpty() -> {
                            Text(
                                text = stringResource(R.string.home_no_tasks_match),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 32.dp)
                            )
                        }
                        state.viewMode == TaskViewMode.GALLERY -> {
                            TaskGalleryGrid(
                                tasks = state.filteredTasks,
                                selectionMode = state.selectionMode,
                                selectedIds = state.selectedTaskIds,
                                onTaskClick = viewModel::onTaskClick
                            )
                        }
                        else -> {
                            state.filteredTasks.forEach { task ->
                                TaskListItem(
                                    task = task,
                                    selectionMode = state.selectionMode,
                                    selected = task.id in state.selectedTaskIds,
                                    onClick = { viewModel.onTaskClick(task.id) },
                                    onComplete = { viewModel.markCompleted(task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

        if (state.selectionMode) {
            SelectionActionBar(
                enabled = state.selectedTaskIds.isNotEmpty(),
                onDelete = viewModel::requestDeleteSelected,
                onPriority = viewModel::showPriorityPicker,
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(bottom = 88.dp)
            )
        } else {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 24.dp, bottom = 88.dp),
                containerColor = MaterialTheme.colorScheme.tertiary,
                contentColor = MaterialTheme.colorScheme.onTertiary,
                shape = CircleShape
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.home_fab_create_cd)
                )
            }
        }
    }
}

@Composable
private fun SelectionActionBar(
    enabled: Boolean,
    onDelete: () -> Unit,
    onPriority: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 20.dp),
        shape = RoundedCornerShape(12.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = 2.dp,
        shadowElevation = 4.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            TextButton(onClick = onDelete, enabled = enabled) {
                Text(
                    stringResource(R.string.home_selection_delete),
                    color = if (enabled) {
                        MaterialTheme.colorScheme.error
                    } else {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    }
                )
            }
            TextButton(onClick = onPriority, enabled = enabled) {
                Text(stringResource(R.string.home_selection_priority))
            }
        }
    }
}

@Composable
private fun TaskGalleryGrid(
    tasks: List<Task>,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    onTaskClick: (Long) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
        tasks.chunked(2).forEach { rowTasks ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                rowTasks.forEach { task ->
                    GalleryTaskCard(
                        task = task,
                        selectionMode = selectionMode,
                        selected = task.id in selectedIds,
                        onClick = { onTaskClick(task.id) },
                        modifier = Modifier.weight(1f)
                    )
                }
                if (rowTasks.size == 1) {
                    Spacer(modifier = Modifier.weight(1f))
                }
            }
        }
    }
}

@Composable
private fun GalleryTaskCard(
    task: Task,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val completed = task.status == TaskStatus.COMPLETED
    val borderColor = when {
        selected -> MaterialTheme.colorScheme.tertiary
        completed -> MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.tertiary
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clip(TaskCardShape)
            .border(
                width = if (selected) 2.dp else 1.5.dp,
                color = borderColor,
                shape = TaskCardShape
            )
            .background(MaterialTheme.colorScheme.surface, TaskCardShape)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        if (selectionMode) {
            SelectionIndicator(
                selected = selected,
                modifier = Modifier.align(Alignment.TopEnd)
            )
        }
        Column(modifier = Modifier.align(Alignment.BottomStart)) {
            Text(
                text = task.title,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 3,
                overflow = TextOverflow.Ellipsis,
                color = if (completed) {
                    MaterialTheme.colorScheme.onSurfaceVariant
                } else {
                    MaterialTheme.colorScheme.onSurface
                }
            )
            Spacer(modifier = Modifier.size(4.dp))
            Text(
                text = NoteDateFormatter.format(Instant.ofEpochMilli(task.updatedAtMillis)),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Composable
private fun TaskListItem(
    task: Task,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onComplete: () -> Unit
) {
    val completed = task.status == TaskStatus.COMPLETED
    val cardModifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 10.dp)

    if (selectionMode) {
        SelectableTaskCard(
            task = task,
            completed = completed,
            selected = selected,
            onClick = onClick,
            modifier = cardModifier
        )
        return
    }

    if (!completed) {
        val dismissState = rememberSwipeToDismissBoxState(
            confirmValueChange = { value ->
                if (value == SwipeToDismissBoxValue.StartToEnd ||
                    value == SwipeToDismissBoxValue.EndToStart
                ) {
                    onComplete()
                    true
                } else {
                    false
                }
            }
        )
        val swiping = dismissState.targetValue != SwipeToDismissBoxValue.Settled

        SwipeToDismissBox(
            modifier = cardModifier,
            state = dismissState,
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(TaskCardShape)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.22f)),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    if (swiping) {
                        Icon(
                            imageVector = Icons.Outlined.Check,
                            contentDescription = stringResource(R.string.home_swipe_complete_hint),
                            tint = MaterialTheme.colorScheme.tertiary,
                            modifier = Modifier.padding(end = 20.dp)
                        )
                    }
                }
            }
        ) {
            BorderedTaskCard(task = task, completed = false, onClick = onClick)
        }
    } else {
        BorderedTaskCard(
            task = task,
            completed = true,
            onClick = onClick,
            modifier = cardModifier
        )
    }
}

@Composable
private fun SelectableTaskCard(
    task: Task,
    completed: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        SelectionIndicator(selected = selected)
        BorderedTaskCard(
            task = task,
            completed = completed,
            selected = selected,
            onClick = onClick,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun SelectionIndicator(
    selected: Boolean,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(
                width = 1.5.dp,
                color = if (selected) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = CircleShape
            )
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.tertiary
                } else {
                    MaterialTheme.colorScheme.surface
                },
                CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        if (selected) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onTertiary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun BorderedTaskCard(
    task: Task,
    completed: Boolean,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val borderColor = when {
        selected -> MaterialTheme.colorScheme.tertiary
        completed -> MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.tertiary
    }
    val borderWidth = when {
        selected -> 2.dp
        completed -> 1.dp
        else -> 1.5.dp
    }

    val subtitle = when {
        task.description.isNotBlank() -> task.description.lineSequence().first()
        else -> NoteDateFormatter.format(Instant.ofEpochMilli(task.updatedAtMillis))
    }

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(TaskCardShape)
            .border(borderWidth, borderColor, TaskCardShape)
            .background(MaterialTheme.colorScheme.surface, TaskCardShape)
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 14.dp)
    ) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.titleMedium,
            color = if (completed) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 2,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.size(6.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                alpha = if (completed) 0.75f else 0.9f
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
