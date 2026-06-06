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
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.runtime.key
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.R
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.model.isNote
import com.example.taskpulse.domain.model.isTaskItem
import com.example.taskpulse.ui.components.TaskPulseScrollableColumn
import com.example.taskpulse.ui.theme.EntryPriorityDot
import com.example.taskpulse.ui.theme.EntryPriorityLabel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val ListDateFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm").withZone(ZoneId.systemDefault())

private val SearchShape = RoundedCornerShape(28.dp)
private val TaskCardShape = RoundedCornerShape(8.dp)
private val SwipeCompleteGreen = Color(0xFF188038)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCreate: () -> Unit,
    onOpenEntryDetail: (Long) -> Unit
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
                    .padding(start = 16.dp, end = 16.dp),
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
                            focusedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.65f),
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.45f),
                            focusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                        )
                    )

                    val hasFilteredTasks = state.filteredTasks.isNotEmpty()
                    val hasFilteredNotes = state.filteredNotes.isNotEmpty()
                    val hasAnyFiltered = hasFilteredTasks || hasFilteredNotes

                    when {
                        !hasAnyFiltered && state.searchQuery.isBlank() -> {
                            Text(
                                text = stringResource(R.string.home_empty_tasks),
                                style = MaterialTheme.typography.bodyLarge,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 48.dp),
                                textAlign = TextAlign.Center
                            )
                        }
                        !hasAnyFiltered -> {
                            Text(
                                text = stringResource(R.string.home_no_tasks_match),
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(top = 32.dp)
                            )
                        }
                        state.viewMode == TaskViewMode.GALLERY -> {
                            TaskGalleryGrid(
                                tasks = state.filteredTasks + state.filteredNotes,
                                selectionMode = state.selectionMode,
                                selectedIds = state.selectedTaskIds,
                                onTaskClick = { taskId ->
                                    if (state.selectionMode) {
                                        viewModel.onTaskClick(taskId)
                                    } else {
                                        onOpenEntryDetail(taskId)
                                    }
                                }
                            )
                        }
                        else -> {
                            if (hasFilteredTasks) {
                                HomeSectionHeader(
                                    title = stringResource(R.string.home_tasks_section)
                                )
                            }
                            state.filteredTasks.forEach { task ->
                                key(task.id) {
                                    TaskListItem(
                                        task = task,
                                        selectionMode = state.selectionMode,
                                        selected = task.id in state.selectedTaskIds,
                                        onClick = {
                                            if (state.selectionMode) {
                                                viewModel.onTaskClick(task.id)
                                            } else {
                                                onOpenEntryDetail(task.id)
                                            }
                                        },
                                        onDelete = { viewModel.deleteTask(task.id) },
                                        onComplete = { viewModel.markTaskCompleted(task.id) }
                                    )
                                }
                            }
                            if (hasFilteredNotes) {
                                HomeSectionHeader(
                                    title = stringResource(R.string.home_notes_section),
                                    showDividerAbove = hasFilteredTasks
                                )
                                state.filteredNotes.forEach { note ->
                                    key(note.id) {
                                        TaskListItem(
                                            task = note,
                                            selectionMode = state.selectionMode,
                                            selected = note.id in state.selectedTaskIds,
                                            onClick = {
                                                if (state.selectionMode) {
                                                    viewModel.onTaskClick(note.id)
                                                } else {
                                                    onOpenEntryDetail(note.id)
                                                }
                                            },
                                            onDelete = { viewModel.deleteTask(note.id) },
                                            onComplete = { viewModel.markTaskCompleted(note.id) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        if (state.selectionMode) {
            val selectedEntries = (state.filteredTasks + state.filteredNotes)
                .filter { it.id in state.selectedTaskIds }
            val canComplete = selectedEntries.any { it.isTaskItem && it.status != TaskStatus.COMPLETED }
            val canSetPriority = selectedEntries.any { it.isTaskItem }
            SelectionActionBar(
                enabled = state.selectedTaskIds.isNotEmpty(),
                showComplete = canComplete,
                showPriority = canSetPriority,
                onComplete = viewModel::completeSelectedTasks,
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
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
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
    showComplete: Boolean,
    showPriority: Boolean,
    onComplete: () -> Unit,
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
                .padding(horizontal = 4.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            if (showComplete) {
                TextButton(onClick = onComplete, enabled = enabled) {
                    Text(
                        stringResource(R.string.home_selection_complete),
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
            if (showPriority) {
                TextButton(onClick = onPriority, enabled = enabled) {
                    Text(stringResource(R.string.home_selection_priority))
                }
            }
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
        }
    }
}

@Composable
private fun HomeSectionHeader(
    title: String,
    showDividerAbove: Boolean = false
) {
    Column(
        modifier = Modifier.padding(
            top = if (showDividerAbove) 16.dp else 4.dp,
            bottom = 8.dp
        )
    ) {
        if (showDividerAbove) {
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
            )
            Spacer(modifier = Modifier.height(10.dp))
        }
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(4.dp))
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
    val borderColor = cardBorderColor(task, selected, completed)

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
                modifier = Modifier.align(Alignment.TopStart)
            )
        }
        CompletedTaskBadge(
            task = task,
            modifier = Modifier.align(Alignment.TopEnd)
        )
        Row(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            EntryPriorityDot(task = task, size = 8.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entryListTitle(task),
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (completed && task.isTaskItem) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    }
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = formatCreatedAt(task.createdAtMillis),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            EntryPriorityLabel(task = task)
        }
    }
}

@Composable
private fun TaskListItem(
    task: Task,
    selectionMode: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
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

    SwipeToDeleteContainer(
        onDelete = onDelete,
        modifier = cardModifier
    ) {
        if (task.isTaskItem && !completed) {
            SwipeToCompleteContainer(onComplete = onComplete) {
                BorderedTaskCard(
                    task = task,
                    completed = false,
                    onClick = onClick
                )
            }
        } else {
            BorderedTaskCard(
                task = task,
                completed = completed,
                onClick = onClick
            )
        }
    }
}

@Composable
private fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val deleteState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.82f }
    )
    SwipeToDismissBox(
        modifier = modifier,
        state = deleteState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        backgroundContent = { SwipeDeleteBackground() }
    ) {
        content()
    }
}

@Composable
private fun SwipeToCompleteContainer(
    onComplete: () -> Unit,
    content: @Composable () -> Unit
) {
    val completeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onComplete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.82f }
    )
    SwipeToDismissBox(
        state = completeState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = { SwipeCompleteBackground() }
    ) {
        content()
    }
}

@Composable
private fun SwipeDeleteBackground() {
    val deleteStripShape = RoundedCornerShape(
        topStart = 12.dp,
        bottomStart = 12.dp,
        topEnd = 4.dp,
        bottomEnd = 4.dp
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterStart)
                .width(56.dp)
                .fillMaxHeight()
                .clip(deleteStripShape)
                .background(MaterialTheme.colorScheme.error),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Delete,
                contentDescription = stringResource(R.string.home_swipe_delete_hint),
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun SwipeCompleteBackground() {
    val completeStripShape = RoundedCornerShape(
        topStart = 4.dp,
        bottomStart = 4.dp,
        topEnd = 12.dp,
        bottomEnd = 12.dp
    )
    Box(modifier = Modifier.fillMaxSize()) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .width(56.dp)
                .fillMaxHeight()
                .clip(completeStripShape)
                .background(SwipeCompleteGreen),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Outlined.Check,
                contentDescription = stringResource(R.string.home_swipe_complete_hint),
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
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
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.outline
                },
                shape = CircleShape
            )
            .background(
                if (selected) {
                    MaterialTheme.colorScheme.primary
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
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(14.dp)
            )
        }
    }
}

@Composable
private fun cardBorderColor(task: Task, selected: Boolean, completed: Boolean) = when {
    selected -> MaterialTheme.colorScheme.primary
    completed && task.isTaskItem -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
    task.isNote -> MaterialTheme.colorScheme.outline.copy(alpha = 0.35f)
    else -> MaterialTheme.colorScheme.outline.copy(alpha = 0.5f)
}

private fun entryListTitle(task: Task): String {
    if (task.isTaskItem) return task.title
    return task.title.ifBlank {
        task.description.lineSequence().firstOrNull()?.trim().orEmpty()
    }.ifBlank {
        task.description.trim()
    }
}

private fun formatCreatedAt(createdAtMillis: Long): String =
    ListDateFormatter.format(Instant.ofEpochMilli(createdAtMillis))

@Composable
private fun CompletedTaskBadge(
    task: Task,
    modifier: Modifier = Modifier
) {
    if (!task.isTaskItem || task.status != TaskStatus.COMPLETED) return
    Icon(
        imageVector = Icons.Outlined.Check,
        contentDescription = stringResource(R.string.home_entry_task_completed_cd),
        tint = MaterialTheme.colorScheme.primary,
        modifier = modifier.size(22.dp)
    )
}

@Composable
private fun BorderedTaskCard(
    task: Task,
    completed: Boolean,
    selected: Boolean = false,
    onClick: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    val borderColor = cardBorderColor(task, selected, completed)
    val borderWidth = if (selected) 2.dp else 0.dp

    val titleColor = if (completed && task.isTaskItem) {
        MaterialTheme.colorScheme.onSurfaceVariant
    } else {
        MaterialTheme.colorScheme.onSurface
    }

    Column(modifier = modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (borderWidth > 0.dp) {
                        Modifier
                            .clip(TaskCardShape)
                            .border(borderWidth, borderColor, TaskCardShape)
                    } else {
                        Modifier
                    }
                )
                .clickable(onClick = onClick)
                .padding(horizontal = 4.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EntryPriorityDot(task = task)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = entryListTitle(task),
                    style = MaterialTheme.typography.bodyLarge,
                    color = titleColor,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.size(4.dp))
                Text(
                    text = formatCreatedAt(task.createdAtMillis),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Column(
                horizontalAlignment = Alignment.End,
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                EntryPriorityLabel(task = task)
                CompletedTaskBadge(task = task)
            }
        }
        HorizontalDivider(
            color = MaterialTheme.colorScheme.outline.copy(alpha = 0.35f),
            thickness = 1.dp
        )
    }
}
