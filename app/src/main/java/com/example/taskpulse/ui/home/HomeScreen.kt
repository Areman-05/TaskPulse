@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.taskpulse.ui.home

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Delete
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.EditNote
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material.icons.outlined.NotificationsActive
import androidx.compose.material.icons.outlined.PushPin
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.automirrored.outlined.TrendingUp
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
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
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.R
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.model.isTaskItem
import com.example.taskpulse.ui.theme.StitchThemeColors
import com.example.taskpulse.ui.theme.StitchTypography
import com.example.taskpulse.ui.theme.TaskPriorityColors
import com.example.taskpulse.ui.theme.TaskPulseColors
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val SearchShape = RoundedCornerShape(999.dp)
private val GlassCardShape = RoundedCornerShape(12.dp)
private val TaskCardShape = RoundedCornerShape(8.dp)
private val SwipeCompleteGreen = Color(0xFF188038)
private val TimeFormatter = DateTimeFormatter.ofPattern("HH:mm").withZone(ZoneId.systemDefault())
private val FabShadowColor = Color(0x14000000)
private const val NOTES_CAROUSEL_LIMIT = 2

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onNavigateToCreate: () -> Unit,
    onNavigateToCreateNote: () -> Unit,
    onOpenEntryDetail: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val scrollState = rememberScrollState()
    val searchFocusRequester = remember { FocusRequester() }

    HomeSelectionDialogs(state = state, viewModel = viewModel)

    Box(modifier = Modifier.fillMaxSize()) {
        StitchHomeBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = Color.Transparent,
            topBar = {
                HomeTopBar(
                    state = state,
                    viewModel = viewModel,
                    onSearchClick = { searchFocusRequester.requestFocus() }
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(scrollState)
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = if (state.selectionMode) 160.dp else 112.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                StitchSearchBar(
                    query = state.searchQuery,
                    onQueryChange = viewModel::onSearchQueryChange,
                    focusRequester = searchFocusRequester
                )

                if (state.viewMode == TaskViewMode.GALLERY) {
                    TaskGalleryGrid(
                        tasks = state.filteredNotes,
                        selectionMode = state.selectionMode,
                        selectedIds = state.selectedTaskIds,
                        onTaskClick = { taskId ->
                            if (state.selectionMode) viewModel.onTaskClick(taskId)
                            else onOpenEntryDetail(taskId)
                        }
                    )
                } else {
                    StitchTasksSection(
                        tasks = state.filteredTasks,
                        selectionMode = state.selectionMode,
                        selectedIds = state.selectedTaskIds,
                        onTaskClick = { taskId ->
                            if (state.selectionMode) viewModel.onTaskClick(taskId)
                            else onOpenEntryDetail(taskId)
                        },
                        onDelete = viewModel::deleteTask,
                        onComplete = viewModel::markTaskCompleted,
                        emptyMessage = if (state.searchQuery.isBlank()) {
                            stringResource(R.string.home_empty_tasks)
                        } else {
                            stringResource(R.string.home_no_tasks_match)
                        }
                    )

                    if (state.searchQuery.isBlank()) {
                        StitchBentoRow(allTasks = state.tasks)
                    }

                    val visibleNotes = if (state.showAllNotes) {
                        state.filteredNotes
                    } else {
                        state.filteredNotes.take(NOTES_CAROUSEL_LIMIT)
                    }

                    StitchNotesSection(
                        notes = visibleNotes,
                        showAllNotes = state.showAllNotes,
                        hasMoreNotes = state.filteredNotes.size > NOTES_CAROUSEL_LIMIT,
                        selectionMode = state.selectionMode,
                        selectedIds = state.selectedTaskIds,
                        onNoteClick = { noteId ->
                            if (state.selectionMode) viewModel.onTaskClick(noteId)
                            else onOpenEntryDetail(noteId)
                        },
                        onViewAll = { viewModel.setShowAllNotes(true) },
                        onCreateNote = onNavigateToCreateNote
                    )
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
                    .padding(bottom = 72.dp)
            )
        } else {
            FloatingActionButton(
                onClick = onNavigateToCreate,
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .padding(end = 16.dp, bottom = 72.dp)
                    .size(56.dp)
                    .shadow(
                        elevation = 8.dp,
                        shape = CircleShape,
                        ambientColor = FabShadowColor,
                        spotColor = FabShadowColor
                    ),
                containerColor = TaskPulseColors.Bronze,
                contentColor = TaskPulseColors.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(
                    defaultElevation = 4.dp,
                    pressedElevation = 6.dp
                )
            ) {
                Icon(
                    imageVector = Icons.Outlined.Edit,
                    contentDescription = stringResource(R.string.home_fab_create_cd),
                    modifier = Modifier.size(24.dp)
                )
            }
        }
    }
}

@Composable
private fun StitchSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    focusRequester: FocusRequester
) {
    val borderColor = StitchThemeColors.cardBorder()
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 8.dp)
            .shadow(1.dp, SearchShape, clip = false)
            .clip(SearchShape)
            .background(StitchThemeColors.searchBarBackground())
            .border(1.dp, borderColor, SearchShape)
            .height(48.dp)
    ) {
        Icon(
            imageVector = Icons.Outlined.Search,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier
                .align(Alignment.CenterStart)
                .padding(start = 16.dp)
                .size(20.dp)
        )
        BasicTextField(
            value = query,
            onValueChange = onQueryChange,
            singleLine = true,
            textStyle = StitchTypography.bodyLg.copy(
                color = MaterialTheme.colorScheme.onSurface
            ),
            modifier = Modifier
                .fillMaxSize()
                .focusRequester(focusRequester)
                .padding(start = 48.dp, end = 16.dp),
            decorationBox = { inner ->
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.CenterStart
                ) {
                    if (query.isEmpty()) {
                        Text(
                            text = stringResource(R.string.home_search_label),
                            style = StitchTypography.bodyLg,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    inner()
                }
            }
        )
    }
}

@Composable
private fun StitchGlassCard(
    modifier: Modifier = Modifier,
    backgroundColor: Color? = null,
    borderColor: Color? = null,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = modifier,
        shape = GlassCardShape,
        color = backgroundColor ?: StitchThemeColors.glassSurface(),
        border = BorderStroke(1.dp, borderColor ?: StitchThemeColors.cardBorder()),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp,
        content = content
    )
}

@Composable
private fun StitchTasksSection(
    tasks: List<Task>,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    onTaskClick: (Long) -> Unit,
    onDelete: (Long) -> Unit,
    onComplete: (Long) -> Unit,
    emptyMessage: String
) {
    val pendingCount = tasks.count { it.status != TaskStatus.COMPLETED }

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_tasks_today),
                style = StitchTypography.headlineSm,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = stringResource(R.string.home_pending_count, pendingCount),
                style = StitchTypography.labelLg,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }

        StitchGlassCard(modifier = Modifier.fillMaxWidth()) {
            if (tasks.isEmpty()) {
                Text(
                    text = emptyMessage,
                    style = StitchTypography.bodyMd,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(16.dp),
                    textAlign = TextAlign.Center
                )
            } else {
                tasks.forEachIndexed { index, task ->
                    key(task.id) {
                        TaskListItem(
                            task = task,
                            selectionMode = selectionMode,
                            selected = task.id in selectedIds,
                            showBottomBorder = index < tasks.lastIndex,
                            onClick = { onTaskClick(task.id) },
                            onDelete = { onDelete(task.id) },
                            onComplete = { onComplete(task.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StitchBentoRow(allTasks: List<Task>) {
    val taskItems = allTasks.filter { it.isTaskItem }
    val completionPercent = if (taskItems.isEmpty()) 0 else {
        (taskItems.count { it.status == TaskStatus.COMPLETED } * 100) / taskItems.size
    }
    val todayCompletedPercent = computeTodayCompletedPercent(taskItems)
    val nextTask = findNextUpcomingTask(allTasks)

    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        StitchGlassCard(modifier = Modifier.weight(1f)) {
            Column(
                modifier = Modifier.padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.TrendingUp,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(24.dp)
                )
                Text(
                    text = stringResource(R.string.home_productivity).uppercase(),
                    style = StitchTypography.labelLg,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    letterSpacing = StitchTypography.labelLg.letterSpacing
                )
                Row(
                    verticalAlignment = Alignment.Bottom,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(
                        text = "$completionPercent%",
                        style = StitchTypography.headlineMd,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    if (todayCompletedPercent > 0) {
                        Text(
                            text = stringResource(
                                R.string.home_productivity_today_delta,
                                todayCompletedPercent
                            ),
                            style = StitchTypography.labelLg,
                            color = TaskPriorityColors.Low,
                            modifier = Modifier.padding(bottom = 2.dp)
                        )
                    }
                }
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 8.dp)
                        .height(4.dp)
                        .clip(RoundedCornerShape(999.dp))
                        .background(MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.6f))
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .fillMaxWidth(completionPercent / 100f)
                            .clip(RoundedCornerShape(999.dp))
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }

        StitchGlassCard(
            modifier = Modifier.weight(1f),
            backgroundColor = StitchThemeColors.secondaryContainerMuted(),
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.3f)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(120.dp)
                    .padding(16.dp),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.Top
                ) {
                    Text(
                        text = stringResource(R.string.home_next_event).uppercase(),
                        style = StitchTypography.labelLg,
                        color = MaterialTheme.colorScheme.onSecondaryContainer
                    )
                    Icon(
                        imageVector = Icons.Outlined.NotificationsActive,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSecondaryContainer,
                        modifier = Modifier.size(20.dp)
                    )
                }
                if (nextTask != null) {
                    Column {
                        Text(
                            text = entryListTitle(nextTask),
                            style = StitchTypography.bodyLg.copy(fontWeight = FontWeight.Medium),
                            color = MaterialTheme.colorScheme.onSurface,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = formatUpcomingLabel(nextTask.dueAtMillis!!),
                            style = StitchTypography.bodyMd,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    Text(
                        text = stringResource(R.string.home_no_upcoming),
                        style = StitchTypography.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

@Composable
private fun StitchNotesSection(
    notes: List<Task>,
    showAllNotes: Boolean,
    hasMoreNotes: Boolean,
    selectionMode: Boolean,
    selectedIds: Set<Long>,
    onNoteClick: (Long) -> Unit,
    onViewAll: () -> Unit,
    onCreateNote: () -> Unit
) {
    Column(
        modifier = Modifier.padding(bottom = 32.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = stringResource(R.string.home_recent_notes),
                style = StitchTypography.headlineSm,
                color = MaterialTheme.colorScheme.onSurface
            )
            if (!showAllNotes && hasMoreNotes) {
                TextButton(onClick = onViewAll) {
                    Text(
                        text = stringResource(R.string.home_view_all),
                        style = StitchTypography.labelLg,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            notes.forEach { note ->
                key(note.id) {
                    StitchNoteCard(
                        note = note,
                        selected = note.id in selectedIds,
                        selectionMode = selectionMode,
                        onClick = { onNoteClick(note.id) }
                    )
                }
            }
            StitchNewNoteCard(onClick = onCreateNote)
        }
    }
}

@Composable
private fun StitchNoteCard(
    note: Task,
    selected: Boolean,
    selectionMode: Boolean,
    onClick: () -> Unit
) {
    val lines = note.description.lineSequence()
        .map { it.trim() }
        .filter { it.isNotBlank() }
        .toList()
    val isListNote = lines.size >= 2

    StitchGlassCard(
        modifier = Modifier
            .width(256.dp)
            .height(160.dp)
            .then(
                if (selected && selectionMode) {
                    Modifier.border(2.dp, MaterialTheme.colorScheme.primary, GlassCardShape)
                } else {
                    Modifier
                }
            )
            .clickable(onClick = onClick)
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                Text(
                    text = entryListTitle(note),
                    style = StitchTypography.bodyLg.copy(fontWeight = FontWeight.Medium),
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                if (note.priority == TaskPriority.HIGH || note.priority == TaskPriority.CRITICAL) {
                    Icon(
                        imageVector = Icons.Outlined.PushPin,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(18.dp)
                    )
                }
            }

            if (isListNote) {
                Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                    lines.take(3).forEach { line ->
                        Row(
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(6.dp)
                                    .border(1.dp, MaterialTheme.colorScheme.outline, CircleShape)
                            )
                            Text(
                                text = line,
                                style = StitchTypography.bodyMd,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                        }
                    }
                }
            } else {
                Text(
                    text = note.description.trim().ifBlank { note.title },
                    style = StitchTypography.bodyMd,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 3,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(modifier = Modifier.weight(1f))
            Text(
                text = formatRelativeTime(note.updatedAtMillis),
                style = StitchTypography.labelLg,
                color = MaterialTheme.colorScheme.outline
            )
        }
    }
}

@Composable
private fun StitchNewNoteCard(onClick: () -> Unit) {
    val borderColor = StitchThemeColors.cardBorder()
    Box(
        modifier = Modifier
            .width(256.dp)
            .height(160.dp)
            .clip(GlassCardShape)
            .drawBehind {
                drawRoundRect(
                    color = borderColor,
                    style = Stroke(
                        width = 1.dp.toPx(),
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(12f, 8f))
                    ),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(12.dp.toPx())
                )
            }
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Icon(
                imageVector = Icons.Outlined.EditNote,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(30.dp)
            )
            Text(
                text = stringResource(R.string.home_new_note),
                style = StitchTypography.bodyMd.copy(fontWeight = FontWeight.Medium),
                color = MaterialTheme.colorScheme.primary
            )
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
                    color = if (enabled) MaterialTheme.colorScheme.error
                    else MaterialTheme.colorScheme.onSurfaceVariant
                )
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
                if (rowTasks.size == 1) Spacer(modifier = Modifier.weight(1f))
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
    StitchGlassCard(
        modifier = modifier
            .aspectRatio(1f)
            .then(
                if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, GlassCardShape)
                else Modifier
            )
            .clickable(onClick = onClick)
    ) {
        Box(modifier = Modifier.fillMaxSize().padding(12.dp)) {
            if (selectionMode) {
                SelectionIndicator(
                    selected = selected,
                    modifier = Modifier.align(Alignment.TopStart)
                )
            }
            Row(
                modifier = Modifier.align(Alignment.BottomStart).fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                PriorityDot(task = task)
                Text(
                    text = entryListTitle(task),
                    style = StitchTypography.bodyMd,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    color = if (completed && task.isTaskItem) {
                        MaterialTheme.colorScheme.onSurfaceVariant
                    } else {
                        MaterialTheme.colorScheme.onSurface
                    },
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun TaskListItem(
    task: Task,
    selectionMode: Boolean,
    selected: Boolean,
    showBottomBorder: Boolean,
    onClick: () -> Unit,
    onDelete: () -> Unit,
    onComplete: () -> Unit
) {
    val completed = task.status == TaskStatus.COMPLETED
    val rowAlpha = if (completed && task.isTaskItem) 0.6f else 1f

    val rowContent: @Composable () -> Unit = {
        StitchTaskRow(
            task = task,
            completed = completed,
            onClick = onClick,
            modifier = Modifier
                .alpha(rowAlpha)
                .padding(16.dp)
                .then(
                    if (showBottomBorder) {
                        Modifier.border(
                            width = 0.dp,
                            color = Color.Transparent
                        )
                    } else Modifier
                )
        )
        if (showBottomBorder) {
            HorizontalDivider(color = StitchThemeColors.cardBorder(), thickness = 1.dp)
        }
    }

    if (selectionMode) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            SelectionIndicator(
                selected = selected,
                modifier = Modifier.padding(start = 12.dp)
            )
            Box(modifier = Modifier.weight(1f)) { rowContent() }
        }
        return
    }

    SwipeToDeleteContainer(onDelete = onDelete, modifier = Modifier.fillMaxWidth()) {
        if (task.isTaskItem && !completed) {
            SwipeToCompleteContainer(onComplete = onComplete) {
                rowContent()
            }
        } else {
            rowContent()
        }
    }
}

@Composable
private fun StitchTaskRow(
    task: Task,
    completed: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val rowBackground = if (pressed) StitchThemeColors.rowHighlight() else Color.Transparent

    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(rowBackground)
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            ),
        verticalAlignment = Alignment.Top,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        if (completed && task.isTaskItem) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = stringResource(R.string.home_entry_task_completed_cd),
                tint = MaterialTheme.colorScheme.primaryContainer,
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(20.dp)
            )
        } else {
            PriorityDot(
                task = task,
                modifier = Modifier.padding(top = 4.dp)
            )
        }

        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = entryListTitle(task),
                style = StitchTypography.bodyLg,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
                textDecoration = if (completed && task.isTaskItem) TextDecoration.LineThrough
                else TextDecoration.None
            )
            if (!(completed && task.isTaskItem)) {
                val subtitle = entrySubtitle(task)
                val dueAt = task.dueAtMillis
                when {
                    subtitle != null -> {
                        Text(
                            text = subtitle,
                            style = StitchTypography.bodyMd,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            maxLines = 2,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                    dueAt != null -> {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp),
                            modifier = Modifier.padding(top = 4.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Outlined.Schedule,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(16.dp)
                            )
                            Text(
                                text = formatDueTimeRange(dueAt),
                                style = StitchTypography.labelLg,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun PriorityDot(task: Task, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(8.dp)
            .clip(CircleShape)
            .background(TaskPriorityColors.forEntry(task))
    )
}

@Composable
private fun SwipeToDeleteContainer(
    onDelete: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable RowScope.() -> Unit
) {
    val deleteState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                onDelete()
                true
            } else false
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.82f }
    )
    SwipeToDismissBox(
        modifier = modifier,
        state = deleteState,
        enableDismissFromStartToEnd = true,
        enableDismissFromEndToStart = false,
        backgroundContent = { SwipeDeleteBackground() },
        content = content
    )
}

@Composable
private fun SwipeToCompleteContainer(
    onComplete: () -> Unit,
    content: @Composable RowScope.() -> Unit
) {
    val completeState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onComplete()
                true
            } else false
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.82f }
    )
    SwipeToDismissBox(
        state = completeState,
        enableDismissFromStartToEnd = false,
        enableDismissFromEndToStart = true,
        backgroundContent = { SwipeCompleteBackground() },
        content = content
    )
}

@Composable
private fun SwipeDeleteBackground() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.error),
        contentAlignment = Alignment.CenterStart
    ) {
        Icon(
            imageVector = Icons.Outlined.Delete,
            contentDescription = stringResource(R.string.home_swipe_delete_hint),
            tint = MaterialTheme.colorScheme.onError,
            modifier = Modifier.padding(start = 20.dp).size(24.dp)
        )
    }
}

@Composable
private fun SwipeCompleteBackground() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(SwipeCompleteGreen),
        contentAlignment = Alignment.CenterEnd
    ) {
        Icon(
            imageVector = Icons.Outlined.Check,
            contentDescription = stringResource(R.string.home_swipe_complete_hint),
            tint = Color.White,
            modifier = Modifier.padding(end = 20.dp).size(24.dp)
        )
    }
}

@Composable
private fun SelectionIndicator(selected: Boolean, modifier: Modifier = Modifier) {
    Box(
        modifier = modifier
            .size(22.dp)
            .clip(CircleShape)
            .border(
                width = 1.5.dp,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.outline,
                shape = CircleShape
            )
            .background(
                if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.surface,
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

private fun entryListTitle(task: Task): String {
    if (task.isTaskItem) return task.title
    return task.title.ifBlank {
        task.description.lineSequence().firstOrNull()?.trim().orEmpty()
    }.ifBlank { task.description.trim() }
}

private fun entrySubtitle(task: Task): String? {
    if (!task.isTaskItem) return null
    return task.description.lineSequence().firstOrNull()?.trim()?.takeIf { it.isNotBlank() }
}

private fun findNextUpcomingTask(tasks: List<Task>): Task? {
    val now = System.currentTimeMillis()
    return tasks
        .filter { it.isTaskItem && it.status != TaskStatus.COMPLETED && it.dueAtMillis != null }
        .filter { it.dueAtMillis!! > now }
        .minByOrNull { it.dueAtMillis!! }
}

private fun computeTodayCompletedPercent(tasks: List<Task>): Int {
    if (tasks.isEmpty()) return 0
    val zone = ZoneId.systemDefault()
    val today = LocalDate.now(zone)
    val completedToday = tasks.count { task ->
        task.status == TaskStatus.COMPLETED &&
            Instant.ofEpochMilli(task.updatedAtMillis).atZone(zone).toLocalDate() == today
    }
    return (completedToday * 100) / tasks.size
}

private fun formatDueTimeRange(dueAtMillis: Long): String {
    val start = Instant.ofEpochMilli(dueAtMillis)
    val end = start.plus(Duration.ofHours(1))
    return "${TimeFormatter.format(start)} - ${TimeFormatter.format(end)}"
}

private fun formatUpcomingLabel(dueAtMillis: Long): String {
    val minutes = Duration.between(Instant.now(), Instant.ofEpochMilli(dueAtMillis)).toMinutes()
    return when {
        minutes < 60 -> "En ${minutes.coerceAtLeast(1)} min"
        minutes < 24 * 60 -> "En ${minutes / 60} h"
        else -> TimeFormatter.format(Instant.ofEpochMilli(dueAtMillis))
    }
}

private fun formatRelativeTime(millis: Long): String {
    val minutes = Duration.between(Instant.ofEpochMilli(millis), Instant.now()).toMinutes()
    return when {
        minutes < 1 -> "Ahora"
        minutes < 60 -> "Hace ${minutes} min"
        minutes < 24 * 60 -> "Hace ${minutes / 60} h"
        minutes < 48 * 60 -> "Ayer"
        else -> DateTimeFormatter.ofPattern("d MMM")
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(millis))
    }
}
