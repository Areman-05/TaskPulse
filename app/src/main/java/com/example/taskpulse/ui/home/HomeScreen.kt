@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.taskpulse.ui.home

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DarkMode
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.R
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DayLabelFormatter =
    DateTimeFormatter.ofPattern("dd/MM").withZone(ZoneId.systemDefault())

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = stringResource(R.string.app_name),
                    style = MaterialTheme.typography.headlineSmall,
                    modifier = Modifier.padding(start = 8.dp)
                )
                IconButton(onClick = viewModel::cycleTheme) {
                    Icon(Icons.Filled.DarkMode, contentDescription = stringResource(R.string.home_theme_toggle_cd))
                }
            }
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = viewModel::createQuickTask,
                enabled = !state.isCreating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isCreating) stringResource(R.string.home_creating_quick) else stringResource(R.string.home_create_quick))
            }
            Button(
                onClick = viewModel::createChainedDemoTasks,
                enabled = !state.isCreatingDemoChain,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    if (state.isCreatingDemoChain) {
                        stringResource(R.string.home_chained_creating)
                    } else {
                        stringResource(R.string.home_create_chained_demo)
                    }
                )
            }

            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = viewModel::onSearchQueryChange,
                label = { Text(stringResource(R.string.home_search_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text(stringResource(R.string.home_pending_count, state.pendingCount))
                    Text(stringResource(R.string.home_completed_count, state.completedCount))
                    Text(stringResource(R.string.home_streak, state.completionStreak))
                }
            }

            if (state.productivityWeek.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = stringResource(R.string.home_weekly_completions),
                            style = MaterialTheme.typography.titleSmall
                        )
                        state.productivityWeek.forEach { point ->
                            val label = DayLabelFormatter.format(Instant.ofEpochMilli(point.dayStartMillis))
                            Text(
                                text = stringResource(R.string.home_day_completion_line, label, point.completedCount),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.home_filters_status), style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { viewModel.selectFilter(HomeTaskFilter.ALL) }) {
                        Text(stringResource(R.string.home_filter_all))
                    }
                    TextButton(onClick = { viewModel.selectFilter(HomeTaskFilter.PENDING) }) {
                        Text(stringResource(R.string.home_filter_pending))
                    }
                    TextButton(onClick = { viewModel.selectFilter(HomeTaskFilter.COMPLETED) }) {
                        Text(stringResource(R.string.home_filter_completed))
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(stringResource(R.string.home_filters_priority), style = MaterialTheme.typography.titleSmall)
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    HomePriorityFilter.entries.forEach { p ->
                        TextButton(onClick = { viewModel.selectPriorityFilter(p) }) {
                            Text(shortPriorityLabel(p))
                        }
                    }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = state.filteredTasks,
                    key = { it.id }
                ) { task ->
                    val animatedAlpha by animateFloatAsState(
                        targetValue = 1f,
                        animationSpec = tween(220),
                        label = "taskAppear"
                    )
                    Column(
                        modifier = Modifier.alpha(animatedAlpha)
                    ) {
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
                                            .background(MaterialTheme.colorScheme.errorContainer),
                                        contentAlignment = Alignment.CenterStart
                                    ) {
                                        Text(
                                            stringResource(R.string.home_swipe_complete_hint),
                                            modifier = Modifier.padding(16.dp),
                                            color = MaterialTheme.colorScheme.onErrorContainer
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
    task: com.example.taskpulse.domain.model.Task,
    onComplete: (() -> Unit)?
) {
    val statusTint = when (task.status) {
        TaskStatus.COMPLETED -> MaterialTheme.colorScheme.primaryContainer
        TaskStatus.IN_PROGRESS -> MaterialTheme.colorScheme.tertiaryContainer
        TaskStatus.FAILED -> MaterialTheme.colorScheme.errorContainer
        TaskStatus.PENDING -> MaterialTheme.colorScheme.surfaceVariant
    }
    val prioLabel = when (task.priority) {
        TaskPriority.CRITICAL -> stringResource(R.string.home_priority_critical_short)
        TaskPriority.HIGH -> stringResource(R.string.home_priority_high_short)
        TaskPriority.MEDIUM -> stringResource(R.string.home_priority_medium_short)
        TaskPriority.LOW -> stringResource(R.string.home_priority_low_short)
    }
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(containerColor = statusTint)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Text(text = task.title, style = MaterialTheme.typography.titleMedium)
            Text(
                text = stringResource(R.string.home_task_status_line, task.status.name, prioLabel),
                style = MaterialTheme.typography.bodyMedium
            )
            task.blockedByTaskId?.let { blockerId ->
                Text(
                    text = stringResource(R.string.home_blocked_by, blockerId),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            if (onComplete != null && task.status != TaskStatus.COMPLETED) {
                TextButton(onClick = onComplete) {
                    Text(stringResource(R.string.home_mark_complete))
                }
            }
        }
    }
}
