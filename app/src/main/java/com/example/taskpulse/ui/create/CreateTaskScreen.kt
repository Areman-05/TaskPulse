@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.taskpulse.ui.create

import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.ArrowBack
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Notes
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.R
import com.example.taskpulse.domain.calendar.TaskCalendarDates
import com.example.taskpulse.domain.model.TaskEntryType
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.ui.theme.StitchTypography
import com.example.taskpulse.ui.theme.TaskPulseColors
import java.time.LocalDate

private val CardShape = RoundedCornerShape(12.dp)
private val ChipShape = RoundedCornerShape(999.dp)

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

    Box(modifier = Modifier.fillMaxSize()) {
        StitchCreateBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                StitchCreateTopBar(
                    onBack = onBack,
                    onSave = viewModel::saveTask,
                    saveEnabled = canSave && !state.isSaving,
                    saving = state.isSaving
                )
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(bottom = 16.dp)
                    .navigationBarsPadding()
                    .imePadding(),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Spacer(modifier = Modifier.height(8.dp))

                StitchEntryTypeChips(
                    selected = state.entryType,
                    onTypeSelected = viewModel::onEntryTypeChange
                )

                when (state.entryType) {
                    TaskEntryType.TASK -> {
                        StitchDetailsCard(
                            title = state.title,
                            onTitleChange = viewModel::onTitleChange,
                            titlePlaceholder = stringResource(R.string.create_title_placeholder),
                            body = state.description,
                            onBodyChange = viewModel::onDescriptionChange,
                            bodyPlaceholder = stringResource(R.string.create_description_placeholder)
                        )

                        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                            StitchDueDateRow(
                                selectedDate = state.scheduleDate,
                                onDateSelected = viewModel::onScheduleDateChange
                            )
                            StitchReminderRow(
                                enabled = state.reminderEnabled,
                                onEnabledChange = viewModel::onReminderEnabledChange,
                                selectedMinutes = state.reminderMinutes,
                                onMinutesSelected = viewModel::onReminderMinutesChange
                            )
                        }

                        StitchPrioritySection(
                            selected = state.priority,
                            onPrioritySelected = viewModel::onPriorityChange
                        )
                    }
                    TaskEntryType.NOTE -> {
                        StitchDetailsCard(
                            title = "",
                            onTitleChange = {},
                            titlePlaceholder = stringResource(R.string.create_title_placeholder),
                            body = state.noteBody,
                            onBodyChange = viewModel::onNoteBodyChange,
                            bodyPlaceholder = stringResource(R.string.create_note_placeholder),
                            showTitle = false
                        )

                        StitchDueDateRow(
                            selectedDate = state.scheduleDate,
                            onDateSelected = {
                                viewModel.onScheduleDateEnabledChange(true)
                                viewModel.onScheduleDateChange(it)
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StitchCreateBackground(modifier: Modifier = Modifier) {
    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(TaskPulseColors.Gray50, TaskPulseColors.SurfaceContainer),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
        )
    }
}

@Composable
private fun StitchCreateTopBar(
    onBack: () -> Unit,
    onSave: () -> Unit,
    saveEnabled: Boolean,
    saving: Boolean
) {
    Surface(color = TaskPulseColors.Gray50, tonalElevation = 0.dp, shadowElevation = 0.dp) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(64.dp)
                .padding(horizontal = 16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                Icon(
                    imageVector = Icons.AutoMirrored.Outlined.ArrowBack,
                    contentDescription = stringResource(R.string.create_task_back_cd),
                    tint = TaskPulseColors.OnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }
            Text(
                text = stringResource(R.string.create_screen_title),
                style = StitchTypography.headlineSm,
                color = TaskPulseColors.Black.copy(alpha = 0.87f),
                modifier = Modifier.weight(1f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
            TextButton(
                onClick = onSave,
                enabled = saveEnabled,
                modifier = Modifier.padding(end = 4.dp)
            ) {
                Text(
                    text = if (saving) {
                        stringResource(R.string.create_task_saving)
                    } else {
                        stringResource(R.string.create_save)
                    },
                    style = StitchTypography.labelLg,
                    color = if (saveEnabled) {
                        TaskPulseColors.Primary
                    } else {
                        TaskPulseColors.OnSurfaceVariant.copy(alpha = 0.4f)
                    }
                )
            }
        }
    }
}

@Composable
private fun StitchEntryTypeChips(
    selected: TaskEntryType,
    onTypeSelected: (TaskEntryType) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        StitchTypeChip(
            label = stringResource(R.string.create_entry_task),
            icon = Icons.Outlined.TaskAlt,
            selectedIcon = Icons.Filled.TaskAlt,
            selected = selected == TaskEntryType.TASK,
            onClick = { onTypeSelected(TaskEntryType.TASK) },
            modifier = Modifier.weight(1f)
        )
        StitchTypeChip(
            label = stringResource(R.string.create_entry_note),
            icon = Icons.Outlined.Notes,
            selectedIcon = Icons.Outlined.Notes,
            selected = selected == TaskEntryType.NOTE,
            onClick = { onTypeSelected(TaskEntryType.NOTE) },
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun StitchTypeChip(
    label: String,
    icon: ImageVector,
    selectedIcon: ImageVector,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val background = if (selected) TaskPulseColors.PrimaryContainer else TaskPulseColors.Gray50
    val borderColor = if (selected) TaskPulseColors.Primary else TaskPulseColors.OutlineVariant
    val contentColor = if (selected) TaskPulseColors.OnPrimaryContainer else TaskPulseColors.OnSurfaceVariant

    Row(
        modifier = modifier
            .scale(if (pressed) 0.95f else 1f)
            .clip(ChipShape)
            .border(1.dp, borderColor, ChipShape)
            .background(background)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = if (selected) selectedIcon else icon,
            contentDescription = null,
            tint = contentColor,
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(4.dp))
        Text(text = label, style = StitchTypography.labelLg, color = contentColor)
    }
}

@Composable
private fun StitchDetailsCard(
    title: String,
    onTitleChange: (String) -> Unit,
    titlePlaceholder: String,
    body: String,
    onBodyChange: (String) -> Unit,
    bodyPlaceholder: String,
    showTitle: Boolean = true
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = TaskPulseColors.White,
        border = BorderStroke(1.dp, TaskPulseColors.SurfaceVariant),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column {
            if (showTitle) {
                BasicTextField(
                    value = title,
                    onValueChange = onTitleChange,
                    singleLine = true,
                    textStyle = StitchTypography.headlineMd.copy(
                        color = Color(0xFF191C1D),
                        fontWeight = FontWeight.W600
                    ),
                    cursorBrush = SolidColor(TaskPulseColors.Primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 8.dp),
                    decorationBox = { inner ->
                        Box {
                            if (title.isEmpty()) {
                                Text(
                                    text = titlePlaceholder,
                                    style = StitchTypography.headlineMd,
                                    color = TaskPulseColors.OnSurfaceVariant.copy(alpha = 0.5f)
                                )
                            }
                            inner()
                        }
                    }
                )
                HorizontalDivider(color = TaskPulseColors.SurfaceVariant, thickness = 1.dp)
            }
            BasicTextField(
                value = body,
                onValueChange = onBodyChange,
                textStyle = StitchTypography.bodyMd.copy(color = Color(0xFF191C1D)),
                cursorBrush = SolidColor(TaskPulseColors.Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (showTitle) 128.dp else 200.dp)
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                decorationBox = { inner ->
                    Box {
                        if (body.isEmpty()) {
                            Text(
                                text = bodyPlaceholder,
                                style = StitchTypography.bodyMd,
                                color = TaskPulseColors.OnSurfaceVariant.copy(alpha = 0.6f)
                            )
                        }
                        inner()
                    }
                }
            )
        }
    }
}

@Composable
private fun StitchDueDateRow(
    selectedDate: LocalDate,
    onDateSelected: (LocalDate) -> Unit
) {
    var showPicker by remember { mutableStateOf(false) }
    val dateLabel = TaskCalendarDates.formatDayLabel(selectedDate)
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(interactionSource = interactionSource, indication = null) {
                showPicker = true
            },
        shape = CardShape,
        color = if (pressed) TaskPulseColors.SurfaceContainerLow else TaskPulseColors.White,
        border = BorderStroke(1.dp, TaskPulseColors.SurfaceVariant),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                Icon(
                    imageVector = Icons.Outlined.CalendarToday,
                    contentDescription = null,
                    tint = TaskPulseColors.Primary,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.create_due_date_label),
                        style = StitchTypography.bodyMd,
                        color = Color(0xFF191C1D)
                    )
                    Text(
                        text = dateLabel,
                        style = StitchTypography.labelLg,
                        color = TaskPulseColors.OnSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = stringResource(R.string.create_schedule_date_cd),
                tint = TaskPulseColors.OnSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
    }

    if (showPicker) {
        val pickerState = rememberDatePickerState(
            initialSelectedDateMillis = TaskCalendarDates.defaultDueMillis(selectedDate)
        )
        DatePickerDialog(
            onDismissRequest = { showPicker = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        pickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(TaskCalendarDates.toLocalDate(millis))
                        }
                        showPicker = false
                    }
                ) {
                    Text(stringResource(R.string.create_schedule_date_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showPicker = false }) {
                    Text(stringResource(R.string.detail_cancel_edit))
                }
            }
        ) {
            DatePicker(state = pickerState)
        }
    }
}

@Composable
private fun StitchReminderRow(
    enabled: Boolean,
    onEnabledChange: (Boolean) -> Unit,
    selectedMinutes: Int,
    onMinutesSelected: (Int) -> Unit
) {
    var menuExpanded by remember { mutableStateOf(false) }
    val selectedOption = TaskReminderIntervals.find { it.minutes == selectedMinutes }
        ?: TaskReminderIntervals.first { it.minutes == 30 }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = TaskPulseColors.White,
        border = BorderStroke(1.dp, TaskPulseColors.SurfaceVariant),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .then(
                    if (enabled) Modifier.clickable { menuExpanded = true } else Modifier
                )
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = Icons.Outlined.Notifications,
                    contentDescription = null,
                    tint = TaskPulseColors.OnSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = stringResource(R.string.create_task_reminder_label),
                        style = StitchTypography.bodyMd,
                        color = Color(0xFF191C1D)
                    )
                    if (enabled) {
                        Text(
                            text = stringResource(
                                R.string.create_task_reminder_hint,
                                stringResource(selectedOption.labelRes)
                            ),
                            style = StitchTypography.labelLg,
                            color = TaskPulseColors.OnSurfaceVariant
                        )
                    }
                }
            }
            Box {
                StitchToggle(checked = enabled, onCheckedChange = onEnabledChange)
                DropdownMenu(
                    expanded = menuExpanded && enabled,
                    onDismissRequest = { menuExpanded = false }
                ) {
                    TaskReminderIntervals.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(stringResource(option.labelRes)) },
                            onClick = {
                                onMinutesSelected(option.minutes)
                                menuExpanded = false
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StitchToggle(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val thumbOffset by animateDpAsState(
        targetValue = if (checked) 26.dp else 2.dp,
        label = "toggleThumb"
    )
    val trackColor = if (checked) TaskPulseColors.Primary else TaskPulseColors.SurfaceVariant
    val thumbColor = if (checked) TaskPulseColors.White else TaskPulseColors.Outline

    Box(
        modifier = Modifier
            .width(48.dp)
            .height(24.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(trackColor)
            .clickable { onCheckedChange(!checked) },
        contentAlignment = Alignment.CenterStart
    ) {
        Box(
            modifier = Modifier
                .offset(x = thumbOffset)
                .size(20.dp)
                .clip(CircleShape)
                .background(thumbColor)
        )
    }
}

@Composable
private fun StitchPrioritySection(
    selected: TaskPriority,
    onPrioritySelected: (TaskPriority) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = stringResource(R.string.create_priority_section).uppercase(),
            style = StitchTypography.labelLg,
            color = TaskPulseColors.OnSurfaceVariant,
            modifier = Modifier.padding(horizontal = 4.dp)
        )
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(bottom = 8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            TaskPriority.entries.forEach { priority ->
                StitchPriorityChip(
                    priority = priority,
                    selected = selected == priority,
                    onClick = { onPrioritySelected(priority) }
                )
            }
        }
    }
}

@Composable
private fun StitchPriorityChip(
    priority: TaskPriority,
    selected: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val (background, borderColor, contentColor, dotColor) = priorityChipColors(priority, selected)

    Row(
        modifier = Modifier
            .scale(if (pressed) 0.95f else 1f)
            .clip(ChipShape)
            .border(1.dp, borderColor, ChipShape)
            .background(background)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .background(dotColor)
        )
        Text(
            text = priorityLabel(priority),
            style = StitchTypography.labelLg,
            color = contentColor
        )
    }
}

private fun priorityChipColors(
    priority: TaskPriority,
    selected: Boolean
): PriorityChipColors {
    if (!selected) {
        return PriorityChipColors(
            background = TaskPulseColors.Gray50,
            borderColor = TaskPulseColors.OutlineVariant,
            contentColor = TaskPulseColors.OnSurfaceVariant,
            dotColor = priorityDotColor(priority)
        )
    }
    return when (priority) {
        TaskPriority.CRITICAL -> PriorityChipColors(
            background = TaskPulseColors.ErrorContainer,
            borderColor = TaskPulseColors.Error,
            contentColor = TaskPulseColors.OnErrorContainer,
            dotColor = TaskPulseColors.Error
        )
        TaskPriority.HIGH -> PriorityChipColors(
            background = TaskPulseColors.PrimaryContainer,
            borderColor = TaskPulseColors.Primary,
            contentColor = TaskPulseColors.OnPrimaryContainer,
            dotColor = TaskPulseColors.PrimaryFixedDim
        )
        else -> PriorityChipColors(
            background = TaskPulseColors.Gray50,
            borderColor = TaskPulseColors.OutlineVariant,
            contentColor = TaskPulseColors.OnSurfaceVariant,
            dotColor = priorityDotColor(priority)
        )
    }
}

private fun priorityDotColor(priority: TaskPriority): Color = when (priority) {
    TaskPriority.CRITICAL -> TaskPulseColors.Error
    TaskPriority.HIGH -> TaskPulseColors.PrimaryFixedDim
    TaskPriority.MEDIUM -> TaskPulseColors.Tertiary
    TaskPriority.LOW -> TaskPulseColors.Secondary
}

private data class PriorityChipColors(
    val background: Color,
    val borderColor: Color,
    val contentColor: Color,
    val dotColor: Color
)

@Composable
private fun priorityLabel(priority: TaskPriority): String = when (priority) {
    TaskPriority.CRITICAL -> stringResource(R.string.home_priority_critical_label)
    TaskPriority.HIGH -> stringResource(R.string.home_priority_high_label)
    TaskPriority.MEDIUM -> stringResource(R.string.home_priority_medium_label)
    TaskPriority.LOW -> stringResource(R.string.home_priority_low_label)
}
