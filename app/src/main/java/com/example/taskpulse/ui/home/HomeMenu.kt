package com.example.taskpulse.ui.home

import com.example.taskpulse.domain.sort.TaskSortField
import com.example.taskpulse.domain.sort.TaskSortOrder
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ViewList
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.GridView
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.SelectAll
import androidx.compose.material.icons.outlined.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.DpOffset
import androidx.compose.ui.unit.dp
import com.example.taskpulse.R
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.ui.theme.StitchTypography
import com.example.taskpulse.ui.theme.StitchThemeColors
import com.example.taskpulse.ui.theme.TaskPulseColors

@Composable
fun HomeTopBar(
    state: HomeUiState,
    viewModel: HomeViewModel,
    onSearchClick: () -> Unit = {}
) {
    if (state.selectionMode) {
        SelectionModeTopBar(
            selectedCount = state.selectedTaskIds.size,
            onCancel = viewModel::exitSelectionMode
        )
    } else {
        DefaultHomeTopBar(
            state = state,
            viewModel = viewModel,
            onSearchClick = onSearchClick
        )
    }
}

@Composable
private fun SelectionModeTopBar(
    selectedCount: Int,
    onCancel: () -> Unit
) {
    Surface(color = StitchThemeColors.topBarSurface(), tonalElevation = 0.dp) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 4.dp)
        ) {
            TextButton(
                onClick = onCancel,
                modifier = Modifier.align(Alignment.CenterStart)
            ) {
                Text(stringResource(R.string.home_selection_cancel))
            }
            Text(
                text = stringResource(R.string.home_selection_count, selectedCount),
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.align(Alignment.Center),
                textAlign = TextAlign.Center
            )
        }
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
    }
}

@Composable
private fun DefaultHomeTopBar(
    state: HomeUiState,
    viewModel: HomeViewModel,
    onSearchClick: () -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    var sortMenuExpanded by remember { mutableStateOf(false) }

    Surface(color = StitchThemeColors.topBarSurface(), tonalElevation = 0.dp) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    horizontalArrangement = Arrangement.spacedBy(16.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box {
                        IconButton(onClick = { menuExpanded = true }) {
                            Icon(
                                imageVector = Icons.Outlined.Menu,
                                contentDescription = stringResource(R.string.home_menu_cd),
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        HomeOverflowMenu(
                            expanded = menuExpanded,
                            sortMenuExpanded = sortMenuExpanded,
                            state = state,
                            onDismiss = {
                                menuExpanded = false
                                sortMenuExpanded = false
                            },
                            onOpenSortMenu = {
                                menuExpanded = false
                                sortMenuExpanded = true
                            },
                            viewModel = viewModel
                        )
                        HomeSortSubmenu(
                            expanded = sortMenuExpanded,
                            state = state,
                            onDismiss = { sortMenuExpanded = false },
                            viewModel = viewModel
                        )
                    }
                    Text(
                        text = stringResource(R.string.app_name),
                        style = StitchTypography.headlineMd,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = onSearchClick) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(R.string.home_search_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
        }
    }
}

@Composable
private fun HomeOverflowMenu(
    expanded: Boolean,
    sortMenuExpanded: Boolean,
    state: HomeUiState,
    onDismiss: () -> Unit,
    onOpenSortMenu: () -> Unit,
    viewModel: HomeViewModel
) {
    DropdownMenu(
        expanded = expanded && !sortMenuExpanded,
        onDismissRequest = onDismiss,
        modifier = Modifier.width(260.dp)
    ) {
        if (state.viewMode == TaskViewMode.LIST) {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.home_menu_view_gallery)) },
                leadingIcon = { Icon(Icons.Outlined.GridView, contentDescription = null) },
                onClick = {
                    viewModel.setShowAllNotes(true)
                    viewModel.setViewMode(TaskViewMode.GALLERY)
                    onDismiss()
                }
            )
        } else {
            DropdownMenuItem(
                text = { Text(stringResource(R.string.home_menu_view_list)) },
                leadingIcon = {
                    Icon(Icons.AutoMirrored.Outlined.ViewList, contentDescription = null)
                },
                onClick = {
                    viewModel.setViewMode(TaskViewMode.LIST)
                    onDismiss()
                }
            )
        }
        DropdownMenuItem(
            text = { Text(stringResource(R.string.home_menu_select_tasks)) },
            leadingIcon = { Icon(Icons.Outlined.SelectAll, contentDescription = null) },
            onClick = {
                viewModel.toggleSelectionMode()
                onDismiss()
            }
        )
        HorizontalDivider()
        DropdownMenuItem(
            text = { Text(stringResource(R.string.home_menu_sort_by)) },
            leadingIcon = { Icon(Icons.Outlined.Sort, contentDescription = null) },
            onClick = onOpenSortMenu
        )
    }
}

@Composable
private fun HomeSortSubmenu(
    expanded: Boolean,
    state: HomeUiState,
    onDismiss: () -> Unit,
    viewModel: HomeViewModel
) {
    DropdownMenu(
        expanded = expanded,
        onDismissRequest = onDismiss,
        offset = DpOffset((-248).dp, 0.dp),
        modifier = Modifier.width(248.dp)
    ) {
        SortMenuItem(
            label = stringResource(R.string.home_sort_priority),
            selected = state.sortField == TaskSortField.PRIORITY,
            onClick = {
                viewModel.setSortField(TaskSortField.PRIORITY)
                onDismiss()
            }
        )
        SortMenuItem(
            label = stringResource(R.string.home_sort_edit_date),
            selected = state.sortField == TaskSortField.EDIT_DATE,
            onClick = {
                viewModel.setSortField(TaskSortField.EDIT_DATE)
                onDismiss()
            }
        )
        SortMenuItem(
            label = stringResource(R.string.home_sort_creation_date),
            selected = state.sortField == TaskSortField.CREATION_DATE,
            onClick = {
                viewModel.setSortField(TaskSortField.CREATION_DATE)
                onDismiss()
            }
        )
        SortMenuItem(
            label = stringResource(R.string.home_sort_title),
            selected = state.sortField == TaskSortField.TITLE,
            onClick = {
                viewModel.setSortField(TaskSortField.TITLE)
                onDismiss()
            }
        )
        HorizontalDivider()
        SortMenuItem(
            label = stringResource(R.string.home_sort_newest_first),
            selected = state.sortOrder == TaskSortOrder.NEWEST_FIRST,
            onClick = {
                viewModel.setSortOrder(TaskSortOrder.NEWEST_FIRST)
                onDismiss()
            }
        )
        SortMenuItem(
            label = stringResource(R.string.home_sort_oldest_first),
            selected = state.sortOrder == TaskSortOrder.OLDEST_FIRST,
            onClick = {
                viewModel.setSortOrder(TaskSortOrder.OLDEST_FIRST)
                onDismiss()
            }
        )
    }
}

@Composable
private fun SortMenuItem(
    label: String,
    selected: Boolean,
    onClick: () -> Unit
) {
    DropdownMenuItem(
        text = { Text(label) },
        trailingIcon = {
            if (selected) {
                Icon(
                    Icons.Outlined.Check,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary
                )
            }
        },
        onClick = onClick
    )
}

@Composable
fun HomeSelectionDialogs(
    state: HomeUiState,
    viewModel: HomeViewModel
) {
    if (state.showDeleteConfirm) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirm,
            title = { Text(stringResource(R.string.home_delete_confirm_title)) },
            text = {
                Text(
                    stringResource(
                        R.string.home_delete_confirm_body,
                        state.selectedTaskIds.size
                    )
                )
            },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDeleteSelected) {
                    Text(stringResource(R.string.home_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirm) {
                    Text(stringResource(R.string.home_delete_cancel))
                }
            }
        )
    }
    if (state.showPriorityPicker) {
        AlertDialog(
            onDismissRequest = viewModel::dismissPriorityPicker,
            title = { Text(stringResource(R.string.home_priority_picker_title)) },
            text = {
                ColumnPriorityOptions(
                    onPriorityChosen = viewModel::applyPriorityToSelected
                )
            },
            confirmButton = {},
            dismissButton = {
                TextButton(onClick = viewModel::dismissPriorityPicker) {
                    Text(stringResource(R.string.home_delete_cancel))
                }
            }
        )
    }
}

@Composable
private fun ColumnPriorityOptions(
    onPriorityChosen: (TaskPriority) -> Unit
) {
    Column {
        TaskPriority.entries.forEach { priority ->
            TextButton(onClick = { onPriorityChosen(priority) }) {
                Text(priorityLabel(priority))
            }
        }
    }
}

@Composable
private fun priorityLabel(priority: TaskPriority): String = when (priority) {
    TaskPriority.CRITICAL -> stringResource(R.string.home_priority_critical_short)
    TaskPriority.HIGH -> stringResource(R.string.home_priority_high_short)
    TaskPriority.MEDIUM -> stringResource(R.string.home_priority_medium_short)
    TaskPriority.LOW -> stringResource(R.string.home_priority_low_short)
}
