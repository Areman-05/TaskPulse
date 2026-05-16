@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.taskpulse.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
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

    Box(modifier = Modifier.fillMaxSize()) {
        Scaffold(
            modifier = Modifier.fillMaxSize(),
            containerColor = MaterialTheme.colorScheme.background,
            topBar = {
                HomeTopBar()
            }
        ) { innerPadding ->
            TaskPulseScrollableColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .padding(start = 20.dp, end = 8.dp),
                contentPaddingBottom = 100.dp
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
                        else -> {
                            state.filteredTasks.forEach { task ->
                                TaskListItem(
                                    task = task,
                                    onComplete = { viewModel.markCompleted(task.id) }
                                )
                            }
                        }
                    }
                }
            }
        }

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

@Composable
private fun HomeTopBar() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 12.dp)
    ) {
        Text(
            text = stringResource(R.string.app_name),
            style = MaterialTheme.typography.displayMedium,
            color = MaterialTheme.colorScheme.onSurface,
            modifier = Modifier
                .align(Alignment.Center)
                .padding(horizontal = 48.dp),
            textAlign = TextAlign.Center
        )
        IconButton(
            onClick = { },
            modifier = Modifier.align(Alignment.CenterEnd)
        ) {
            Icon(
                imageVector = Icons.Outlined.MoreVert,
                contentDescription = stringResource(R.string.home_menu_cd),
                tint = MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

@Composable
private fun TaskListItem(
    task: Task,
    onComplete: () -> Unit
) {
    val completed = task.status == TaskStatus.COMPLETED
    val cardModifier = Modifier
        .fillMaxWidth()
        .padding(bottom = 10.dp)

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
            BorderedTaskCard(task = task, completed = false)
        }
    } else {
        BorderedTaskCard(
            task = task,
            completed = true,
            modifier = cardModifier
        )
    }
}

@Composable
private fun BorderedTaskCard(
    task: Task,
    completed: Boolean,
    modifier: Modifier = Modifier
) {
    val borderColor = if (completed) {
        MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
    } else {
        MaterialTheme.colorScheme.tertiary
    }
    val borderWidth = if (completed) 1.dp else 1.5.dp

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
        Spacer(modifier = Modifier.height(6.dp))
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
