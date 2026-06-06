@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.taskpulse.ui.calendar

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.outlined.Add
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.ChevronLeft
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Schedule
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.R
import com.example.taskpulse.domain.calendar.TaskCalendarDates
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.model.isNote
import com.example.taskpulse.domain.model.isTaskItem
import com.example.taskpulse.ui.theme.StitchThemeColors
import com.example.taskpulse.ui.theme.StitchTypography
import com.example.taskpulse.ui.theme.TaskPriorityColors
import com.example.taskpulse.ui.theme.TaskPulseColors
import java.time.Duration
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.util.Locale

private val CardShape = RoundedCornerShape(12.dp)
private val FabShadowColor = Color(0x1F000000)
private val TimeFormatter = DateTimeFormatter.ofPattern("h:mm a", Locale("es", "ES"))

private data class CalendarCell(
    val date: LocalDate,
    val inCurrentMonth: Boolean
)

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onOpenEntry: (Long) -> Unit,
    onNavigateToCreate: (LocalDate) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Box(modifier = Modifier.fillMaxSize()) {
        StitchCalendarBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            topBar = {
                StitchCalendarTopBar(onAddClick = { onNavigateToCreate(state.selectedDate) })
            }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 8.dp, bottom = 112.dp)
            ) {
                StitchCalendarCard(
                    month = state.visibleMonth,
                    selectedDate = state.selectedDate,
                    datesWithEntries = state.datesWithEntries,
                    onPreviousMonth = viewModel::previousMonth,
                    onNextMonth = viewModel::nextMonth,
                    onMonthTitleClick = viewModel::showMonthYearPicker,
                    onSelectDate = viewModel::selectDate
                )

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = TaskCalendarDates.formatDayHeading(state.selectedDate),
                    style = StitchTypography.headlineSm,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.padding(start = 8.dp, bottom = 8.dp)
                )

                StitchDayEntriesCard(
                    tasks = state.selectedDayTasks,
                    notes = state.selectedDayNotes,
                    onOpenEntry = onOpenEntry
                )
            }
        }

        FloatingActionButton(
            onClick = { onNavigateToCreate(state.selectedDate) },
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 72.dp)
                .size(56.dp)
                .shadow(8.dp, CircleShape, ambientColor = FabShadowColor, spotColor = FabShadowColor),
            containerColor = TaskPulseColors.Primary,
            contentColor = TaskPulseColors.White,
            shape = CircleShape,
            elevation = FloatingActionButtonDefaults.elevation(
                defaultElevation = 4.dp,
                pressedElevation = 6.dp
            )
        ) {
            Icon(
                imageVector = Icons.Filled.Edit,
                contentDescription = stringResource(R.string.calendar_fab_create_cd),
                modifier = Modifier.size(24.dp)
            )
        }
    }

    if (state.showMonthYearPicker) {
        MonthYearPickerDialog(
            yearMonth = state.visibleMonth,
            onDismiss = viewModel::dismissMonthYearPicker,
            onConfirm = { month, year -> viewModel.applyMonthYear(month, year) }
        )
    }
}

@Composable
private fun StitchCalendarBackground(modifier: Modifier = Modifier) {
    val pageBg = StitchThemeColors.pageBackground()
    val bronzeGlow = StitchThemeColors.calendarGradientBronze()
    Canvas(modifier = modifier) {
        drawRect(color = pageBg)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(bronzeGlow, Color.Transparent),
                center = Offset(size.width / 2f, 0f),
                radius = size.height * 0.7f
            ),
            center = Offset(size.width / 2f, 0f),
            radius = size.height * 0.7f
        )
    }
}

@Composable
private fun StitchCalendarTopBar(onAddClick: () -> Unit) {
    Surface(
        color = StitchThemeColors.topBarSurface(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Menu,
                        contentDescription = stringResource(R.string.home_menu_cd),
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(24.dp)
                    )
                }
                Text(
                    text = stringResource(R.string.calendar_title),
                    style = StitchTypography.headlineMd,
                    color = MaterialTheme.colorScheme.primary
                )
                IconButton(onClick = onAddClick, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Add,
                        contentDescription = stringResource(R.string.calendar_fab_create_cd),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
        }
    }
}

@Composable
private fun StitchCalendarCard(
    month: YearMonth,
    selectedDate: LocalDate,
    datesWithEntries: Set<LocalDate>,
    onPreviousMonth: () -> Unit,
    onNextMonth: () -> Unit,
    onMonthTitleClick: () -> Unit,
    onSelectDate: (LocalDate) -> Unit
) {
    val locale = Locale("es", "ES")
    val weekdayLabels = remember {
        listOf(
            java.time.DayOfWeek.SUNDAY,
            java.time.DayOfWeek.MONDAY,
            java.time.DayOfWeek.TUESDAY,
            java.time.DayOfWeek.WEDNESDAY,
            java.time.DayOfWeek.THURSDAY,
            java.time.DayOfWeek.FRIDAY,
            java.time.DayOfWeek.SATURDAY
        ).map { it.getDisplayName(TextStyle.SHORT_STANDALONE, locale).take(1).uppercase(locale) }
    }
    val cells = remember(month) { buildMonthGrid(month) }
    val today = remember { TaskCalendarDates.today() }
    val cardBg = StitchThemeColors.cardBackground()
    val cardBorder = StitchThemeColors.cardBorder()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = TaskCalendarDates.formatMonthYear(month),
                    style = StitchTypography.headlineSm,
                    color = MaterialTheme.colorScheme.onSurface,
                    modifier = Modifier.clickable(onClick = onMonthTitleClick)
                )
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    IconButton(onClick = onPreviousMonth, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Outlined.ChevronLeft,
                            stringResource(R.string.calendar_prev_month_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = onNextMonth, modifier = Modifier.size(32.dp)) {
                        Icon(
                            Icons.Outlined.ChevronRight,
                            stringResource(R.string.calendar_next_month_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(modifier = Modifier.fillMaxWidth()) {
                weekdayLabels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = StitchTypography.labelLg,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            cells.chunked(7).forEach { week ->
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    week.forEach { cell ->
                        StitchDayCell(
                            cell = cell,
                            isSelected = cell.date == selectedDate,
                            isToday = cell.date == today,
                            hasEntries = cell.date in datesWithEntries,
                            onClick = { onSelectDate(cell.date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StitchDayCell(
    cell: CalendarCell,
    isSelected: Boolean,
    isToday: Boolean,
    hasEntries: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    val onSurface = MaterialTheme.colorScheme.onSurface
    val textColor = when {
        isSelected -> MaterialTheme.colorScheme.onPrimary
        !cell.inCurrentMonth -> StitchThemeColors.mutedAdjacentDay()
        isToday -> MaterialTheme.colorScheme.primary
        else -> onSurface
    }

    Box(
        modifier = modifier.height(44.dp),
        contentAlignment = Alignment.Center
    ) {
        val circleModifier = when {
            isSelected -> Modifier
                .size(40.dp)
                .shadow(2.dp, CircleShape)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primary)
            isToday -> Modifier
                .size(40.dp)
                .clip(CircleShape)
                .border(2.dp, MaterialTheme.colorScheme.primary, CircleShape)
            pressed -> Modifier
                .size(36.dp)
                .clip(CircleShape)
                .background(StitchThemeColors.rowHighlight())
            else -> Modifier.size(36.dp)
        }

        Box(
            modifier = circleModifier
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = cell.date.dayOfMonth.toString(),
                    style = StitchTypography.bodyMd.copy(
                        fontWeight = if (isSelected || isToday) FontWeight.Medium else FontWeight.Normal
                    ),
                    color = textColor
                )
                if (hasEntries && cell.inCurrentMonth && !isSelected) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(4.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primary)
                    )
                }
            }
        }
    }
}

@Composable
private fun StitchDayEntriesCard(
    tasks: List<Task>,
    notes: List<Task>,
    onOpenEntry: (Long) -> Unit
) {
    val entries = tasks + notes

    val cardBg = StitchThemeColors.cardBackground()
    val cardBorder = StitchThemeColors.cardBorder()

    if (entries.isEmpty()) {
        Surface(
            modifier = Modifier.fillMaxWidth(),
            shape = CardShape,
            color = cardBg,
            border = BorderStroke(1.dp, cardBorder)
        ) {
            Text(
                text = stringResource(R.string.calendar_empty_day),
                style = StitchTypography.bodyMd,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(16.dp),
                textAlign = TextAlign.Center
            )
        }
        return
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = cardBg,
        border = BorderStroke(1.dp, cardBorder),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column {
            entries.forEachIndexed { index, entry ->
                StitchDayEntryRow(task = entry, onClick = { onOpenEntry(entry.id) })
                if (index < entries.lastIndex) {
                    HorizontalDivider(color = cardBorder, thickness = 1.dp)
                }
            }
        }
    }
}

@Composable
private fun StitchDayEntryRow(task: Task, onClick: () -> Unit) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val completed = task.isTaskItem && task.status == TaskStatus.COMPLETED

    val onSurface = MaterialTheme.colorScheme.onSurface

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(if (pressed) StitchThemeColors.rowHighlight() else Color.Transparent)
            .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
            .padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.Top
    ) {
        if (completed) {
            Spacer(modifier = Modifier.size(8.dp))
        } else {
            Box(
                modifier = Modifier
                    .padding(top = 4.dp)
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(entryDotColor(task))
            )
        }

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = entryTitle(task),
                style = StitchTypography.bodyLg,
                color = if (completed) MaterialTheme.colorScheme.outline else onSurface,
                textDecoration = if (completed) TextDecoration.LineThrough else TextDecoration.None,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )
            formatEntryTime(task)?.let { time ->
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(top = 4.dp)
                ) {
                    Icon(
                        imageVector = Icons.Outlined.Schedule,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(16.dp)
                    )
                    Text(
                        text = time,
                        style = StitchTypography.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }

        if (completed) {
            Icon(
                imageVector = Icons.Outlined.CheckCircle,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun MonthYearPickerDialog(
    yearMonth: YearMonth,
    onDismiss: () -> Unit,
    onConfirm: (month: Int, year: Int) -> Unit
) {
    var selectedMonth by remember { mutableIntStateOf(yearMonth.monthValue) }
    var selectedYear by remember { mutableIntStateOf(yearMonth.year) }
    val locale = Locale("es", "ES")
    val months = remember {
        (1..12).map { month ->
            java.time.Month.of(month).getDisplayName(TextStyle.FULL, locale)
                .replaceFirstChar { it.titlecase(locale) }
        }
    }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text(stringResource(R.string.calendar_pick_month_year)) },
        text = {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { selectedYear -= 1 }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowLeft, contentDescription = null)
                    }
                    Text(text = selectedYear.toString(), style = StitchTypography.headlineMd)
                    IconButton(onClick = { selectedYear += 1 }) {
                        Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null)
                    }
                }
                Spacer(modifier = Modifier.height(8.dp))
                LazyColumn(modifier = Modifier.height(200.dp)) {
                    items(months.size) { index ->
                        val month = index + 1
                        val selected = month == selectedMonth
                        Text(
                            text = months[index],
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(8.dp))
                                .background(
                                    if (selected) MaterialTheme.colorScheme.secondaryContainer
                                    else Color.Transparent
                                )
                                .clickable { selectedMonth = month }
                                .padding(vertical = 10.dp, horizontal = 12.dp),
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = { onConfirm(selectedMonth, selectedYear) }) {
                Text(stringResource(R.string.create_schedule_date_confirm))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(stringResource(R.string.detail_cancel_edit))
            }
        }
    )
}

private fun buildMonthGrid(month: YearMonth): List<CalendarCell> {
    val first = month.atDay(1)
    val startOffset = first.dayOfWeek.value % 7
    val cells = mutableListOf<CalendarCell>()

    val prevMonth = month.minusMonths(1)
    for (i in startOffset downTo 1) {
        cells.add(
            CalendarCell(
                date = prevMonth.atDay(prevMonth.lengthOfMonth() - i + 1),
                inCurrentMonth = false
            )
        )
    }
    for (day in 1..month.lengthOfMonth()) {
        cells.add(CalendarCell(date = month.atDay(day), inCurrentMonth = true))
    }
    var nextDay = 1
    val nextMonth = month.plusMonths(1)
    while (cells.size % 7 != 0) {
        cells.add(CalendarCell(date = nextMonth.atDay(nextDay++), inCurrentMonth = false))
    }
    return cells
}

private fun entryTitle(task: Task): String {
    if (!task.isNote) return task.title
    return task.title.ifBlank {
        task.description.lineSequence().firstOrNull()?.trim().orEmpty()
    }.ifBlank { task.description.trim() }
}

private fun entryDotColor(task: Task): Color = when {
    task.isNote -> TaskPulseColors.Outline
    task.priority == TaskPriority.CRITICAL -> TaskPriorityColors.Critical
    task.priority == TaskPriority.HIGH -> TaskPulseColors.Primary
    task.priority == TaskPriority.MEDIUM -> TaskPulseColors.Tertiary
    else -> TaskPulseColors.Secondary
}

private fun formatEntryTime(task: Task): String? {
    val due = task.dueAtMillis ?: return null
    if (TaskCalendarDates.isCalendarDueTime(due)) return null
    val zone = ZoneId.systemDefault()
    val start = Instant.ofEpochMilli(due).atZone(zone)
    val end = start.plus(Duration.ofHours(1))
    val startText = TimeFormatter.format(start).replace(".", "").trim()
    val endText = TimeFormatter.format(end).replace(".", "").trim()
    return if (task.priority == TaskPriority.HIGH || task.priority == TaskPriority.CRITICAL) {
        "$startText - $endText"
    } else {
        startText
    }
}
