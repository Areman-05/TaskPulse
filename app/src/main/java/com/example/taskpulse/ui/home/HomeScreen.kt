@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.taskpulse.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Edit
import androidx.compose.material.icons.outlined.MoreVert
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.HorizontalDivider
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
                            .padding(bottom = 8.dp),
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
                                NoteStyleTaskRow(
                                    task = task,
                                    onComplete = { viewModel.markCompleted(task.id) }
                                )
                                HorizontalDivider(
                                    color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                                    thickness = 0.5.dp
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
private fun NoteStyleTaskRow(
    task: Task,
    onComplete: () -> Unit
) {
    if (task.status != TaskStatus.COMPLETED) {
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
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = true,
            backgroundContent = {
                Box(
                    Modifier
                        .fillMaxWidth()
                        .height(72.dp)
                        .background(MaterialTheme.colorScheme.tertiary.copy(alpha = 0.85f)),
                    contentAlignment = Alignment.CenterEnd
                ) {
                    Text(
                        stringResource(R.string.home_swipe_complete_hint),
                        modifier = Modifier.padding(horizontal = 20.dp),
                        color = MaterialTheme.colorScheme.onTertiary,
                        style = MaterialTheme.typography.labelLarge
                    )
                }
            }
        ) {
            NoteRowContent(task = task)
        }
    } else {
        NoteRowContent(task = task, completed = true)
    }
}

@Composable
private fun NoteRowContent(
    task: Task,
    completed: Boolean = false
) {
    val subtitle = when {
        task.description.isNotBlank() -> task.description.lineSequence().first()
        else -> NoteDateFormatter.format(Instant.ofEpochMilli(task.updatedAtMillis))
    }
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 14.dp, horizontal = 4.dp)
    ) {
        Text(
            text = task.title,
            style = MaterialTheme.typography.titleMedium,
            color = if (completed) {
                MaterialTheme.colorScheme.onSurfaceVariant
            } else {
                MaterialTheme.colorScheme.onSurface
            },
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = subtitle,
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
