@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.taskpulse.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Brightness4
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.R
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.ui.components.TaskPulsePrimaryButton
import com.example.taskpulse.ui.components.TaskPulseSecondaryButton
import com.example.taskpulse.ui.components.TaskPulseSectionCard
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DayLabelFormatter =
    DateTimeFormatter.ofPattern("dd/MM").withZone(ZoneId.systemDefault())

private val MinimalShape = RoundedCornerShape(4.dp)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val isEffectivelyDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = stringResource(R.string.app_name).uppercase(),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Text(
                        text = stringResource(R.string.home_screen_subtitle),
                        style = MaterialTheme.typography.headlineSmall
                    )
                }
                IconButton(onClick = { viewModel.cycleTheme(isEffectivelyDark) }) {
                    Icon(
                        Icons.Outlined.Brightness4,
                        contentDescription = stringResource(R.string.home_theme_toggle_cd),
                        tint = MaterialTheme.colorScheme.onSurface
                    )
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            TaskPulsePrimaryButton(
                text = if (state.isCreating) {
                    stringResource(R.string.home_creating_quick)
                } else {
                    stringResource(R.string.home_create_quick)
                },
                onClick = viewModel::createQuickTask,
                enabled = !state.isCreating
            )
            TaskPulseSecondaryButton(
                text = if (state.isCreatingDemoChain) {
                    stringResource(R.string.home_chained_creating)
                } else {
                    stringResource(R.string.home_create_chained_demo)
                },
                onClick = viewModel::createChainedDemoTasks,
                enabled = !state.isCreatingDemoChain
            )

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text(stringResource(R.string.home_search_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = MinimalShape,
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = MaterialTheme.colorScheme.primary,
                    unfocusedBorderColor = MaterialTheme.colorScheme.outline
                )
            )

            TaskPulseSectionCard {
                Text(
                    stringResource(R.string.home_stats_title),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(stringResource(R.string.home_pending_count, state.pendingCount))
                Text(stringResource(R.string.home_completed_count, state.completedCount))
                Text(
                    stringResource(R.string.home_streak, state.completionStreak),
                    color = MaterialTheme.colorScheme.tertiary
                )
            }

            if (state.productivityWeek.isNotEmpty()) {
                TaskPulseSectionCard {
                    Text(
                        text = stringResource(R.string.home_weekly_completions),
                        style = MaterialTheme.typography.labelLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    state.productivityWeek.forEach { point ->
                        val label = DayLabelFormatter.format(Instant.ofEpochMilli(point.dayStartMillis))
                        Text(
                            text = stringResource(R.string.home_day_completion_line, label, point.completedCount),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            FilterChipRow(
                title = stringResource(R.string.home_filters_status),
                labels = listOf(
                    stringResource(R.string.home_filter_all),
                    stringResource(R.string.home_filter_pending),
                    stringResource(R.string.home_filter_completed)
                ),
                onSelected = { index ->
                    viewModel.selectFilter(
                        when (index) {
                            1 -> HomeTaskFilter.PENDING
                            2 -> HomeTaskFilter.COMPLETED
                            else -> HomeTaskFilter.ALL
                        }
                    )
                }
            )

            FilterChipRow(
                title = stringResource(R.string.home_filters_priority),
                labels = HomePriorityFilter.entries.map { shortPriorityLabel(it) },
                onSelected = { index ->
                    viewModel.selectPriorityFilter(HomePriorityFilter.entries[index])
                }
            )

            LazyColumn(
                contentPadding = PaddingValues(bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                items(items = state.filteredTasks, key = { it.id }) { task ->
                    val animatedAlpha by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = tween(220),
                        label = "taskAppear"
                    )
                    Column(modifier = Modifier.alpha(animatedAlpha)) {
                        if (task.status != TaskStatus.COMPLETED) {
                            val dismissState = rememberSwipeToDismissBoxState(
                                confirmValueChange = { value ->
                                    if (value == SwipeToDismissBoxValue.StartToEnd ||
                                        value == SwipeToDismissBoxValue.EndToStart
                                    ) {
                                        viewModel.markCompleted(task.id)
                                        true
                                    } else {
                                        false
                                    }
                                }
                            )
                            SwipeToDismissBox(
                                state = dismissState,
                                enableDismissFromStartToEnd = true,
                                enableDismissFromEndToStart = true,
                                backgroundContent = {
                                    Box(
                                        Modifier
                                            .fillMaxWidth()
                                            .height(120.dp)
                                            .background(MaterialTheme.colorScheme.primary),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            stringResource(R.string.home_swipe_complete_hint),
                                            modifier = Modifier.padding(16.dp),
                                            color = MaterialTheme.colorScheme.onPrimary,
                                            style = MaterialTheme.typography.labelLarge
                                        )
                                    }
                                }
                            ) {
                                TaskRowCard(task = task, onComplete = { viewModel.markCompleted(task.id) })
                            }
                        } else {
                            TaskRowCard(task = task, onComplete = null)
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FilterChipRow(
    title: String,
    labels: List<String>,
    onSelected: (Int) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
        Text(title, style = MaterialTheme.typography.labelLarge, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            labels.forEachIndexed { index, label ->
                TextButton(onClick = { onSelected(index) }) {
                    Text(label, style = MaterialTheme.typography.labelLarge)
                }
            }
        }
    }
}

@Composable
private fun shortPriorityLabel(filter: HomePriorityFilter): String =
    when (filter) {
        HomePriorityFilter.ALL -> stringResource(R.string.home_priority_all)
        HomePriorityFilter.CRITICAL -> stringResource(R.string.home_priority_critical_short)
        HomePriorityFilter.HIGH -> stringResource(R.string.home_priority_high_short)
        HomePriorityFilter.MEDIUM -> stringResource(R.string.home_priority_medium_short)
        HomePriorityFilter.LOW -> stringResource(R.string.home_priority_low_short)
    }

@Composable
private fun TaskRowCard(
    task: Task,
    onComplete: (() -> Unit)?
) {
    val accent = when (task.status) {
        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiary
        TaskStatus.FAILED -> MaterialTheme.colorScheme.onSurfaceVariant
        TaskStatus.COMPLETED -> MaterialTheme.colorScheme.outline
        TaskStatus.PENDING -> MaterialTheme.colorScheme.primary
    }
    val prioLabel = when (task.priority) {
        TaskPriority.CRITICAL -> stringResource(R.string.home_priority_critical_short)
        TaskPriority.HIGH -> stringResource(R.string.home_priority_high_short)
        TaskPriority.MEDIUM -> stringResource(R.string.home_priority_medium_short)
        TaskPriority.LOW -> stringResource(R.string.home_priority_low_short)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = MinimalShape,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline),
        elevation = CardDefaults.cardElevation(0.dp)
    ) {
        Row(
            modifier = Modifier.padding(14.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .background(accent, CircleShape)
                    .align(Alignment.Top)
            )
            Column(modifier = Modifier.weight(1f)) {
                Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                Text(
                    text = stringResource(R.string.home_task_status_line, task.status.name, prioLabel),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                task.blockedByTaskId?.let { blockerId ->
                    Text(
                        text = stringResource(R.string.home_blocked_by, blockerId),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.tertiary
                    )
                }
                if (onComplete != null) {
                    TextButton(onClick = onComplete) {
                        Text(
                            stringResource(R.string.home_mark_complete),
                            style = MaterialTheme.typography.labelLarge
                        )
                    }
                }
            }
        }
    }
}
