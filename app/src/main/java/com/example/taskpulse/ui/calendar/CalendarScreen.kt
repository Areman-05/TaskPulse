@file:OptIn(androidx.compose.material3.ExperimentalMaterial3Api::class)

package com.example.taskpulse.ui.calendar

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
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowLeft
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.R
import com.example.taskpulse.domain.calendar.TaskCalendarDates
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.model.isNote
import com.example.taskpulse.ui.components.TaskPulseScrollableColumn
import com.example.taskpulse.ui.theme.EntryPriorityDot
import com.example.taskpulse.ui.theme.EntryPriorityLabel
import java.time.LocalDate
import java.time.YearMonth
import java.time.format.TextStyle
import java.util.Locale

private val CardShape = RoundedCornerShape(12.dp)
private val MonthShape = RoundedCornerShape(16.dp)

@Composable
fun CalendarScreen(
    viewModel: CalendarViewModel,
    onOpenEntry: (Long) -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Column(modifier = Modifier.fillMaxSize()) {
            TopAppBar(
                title = {
                    Text(
                        stringResource(R.string.calendar_title),
                        style = MaterialTheme.typography.titleLarge
                    )
                },
                actions = {
                    IconButton(onClick = viewModel::goToToday) {
                        Icon(
                            Icons.Outlined.Today,
                            contentDescription = stringResource(R.string.calendar_today_cd)
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background.copy(alpha = 0.92f)
                )
            )

            TaskPulseScrollableColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 20.dp),
                showAmbientGrid = false,
                showScrollbar = false,
                contentPaddingBottom = 24.dp
            ) {
                CalendarMonthHeader(
                    month = state.visibleMonth,
                    onPrevious = viewModel::previousMonth,
                    onNext = viewModel::nextMonth,
                    onTitleClick = viewModel::showMonthYearPicker
                )
                Spacer(modifier = Modifier.height(12.dp))
                CalendarMonthGrid(
                    month = state.visibleMonth,
                    selectedDate = state.selectedDate,
                    datesWithEntries = state.datesWithEntries,
                    onSelectDate = viewModel::selectDate
                )
                Spacer(modifier = Modifier.height(20.dp))
                Text(
                    text = stringResource(
                        R.string.calendar_day_heading,
                        TaskCalendarDates.formatDayLabel(state.selectedDate)
                    ),
                    style = MaterialTheme.typography.titleMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Spacer(modifier = Modifier.height(10.dp))
                if (state.selectedDayEntries.isEmpty()) {
                    Text(
                        text = stringResource(R.string.calendar_empty_day),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.fillMaxWidth(),
                        textAlign = TextAlign.Center
                    )
                } else {
                    Text(
                        text = stringResource(
                            R.string.calendar_day_count,
                            state.selectedDayEntries.size
                        ),
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    state.selectedDayEntries.forEach { task ->
                        CalendarEntryCard(
                            task = task,
                            onClick = { onOpenEntry(task.id) }
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
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
private fun CalendarMonthHeader(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit,
    onTitleClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MonthShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 4.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = onPrevious) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowLeft,
                    contentDescription = stringResource(R.string.calendar_prev_month_cd)
                )
            }
            Text(
                text = TaskCalendarDates.formatMonthYear(month),
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier
                    .clickable(onClick = onTitleClick)
                    .padding(horizontal = 12.dp, vertical = 8.dp)
            )
            IconButton(onClick = onNext) {
                Icon(
                    Icons.AutoMirrored.Filled.KeyboardArrowRight,
                    contentDescription = stringResource(R.string.calendar_next_month_cd)
                )
            }
        }
    }
}

@Composable
private fun CalendarMonthGrid(
    month: YearMonth,
    selectedDate: LocalDate,
    datesWithEntries: Set<LocalDate>,
    onSelectDate: (LocalDate) -> Unit
) {
    val locale = Locale("es", "ES")
    val weekdayLabels = remember {
        (1..7).map { day ->
            java.time.DayOfWeek.of(day)
                .getDisplayName(TextStyle.SHORT_STANDALONE, locale)
                .take(1)
                .uppercase(locale)
        }
    }
    val cells = remember(month) { buildMonthCells(month) }
    val today = remember { TaskCalendarDates.today() }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MonthShape,
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.88f),
        border = androidx.compose.foundation.BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.3f)
        )
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(modifier = Modifier.fillMaxWidth()) {
                weekdayLabels.forEach { label ->
                    Text(
                        text = label,
                        modifier = Modifier.weight(1f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
            cells.chunked(7).forEach { week ->
                Row(modifier = Modifier.fillMaxWidth()) {
                    week.forEach { date ->
                        CalendarDayCell(
                            date = date,
                            isSelected = date == selectedDate,
                            hasEntries = date != null && date in datesWithEntries,
                            isToday = date == today,
                            onClick = { if (date != null) onSelectDate(date) },
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
            }
        }
    }
}

private fun buildMonthCells(month: YearMonth): List<LocalDate?> {
    val first = month.atDay(1)
    val offset = first.dayOfWeek.value - 1
    val cells = mutableListOf<LocalDate?>()
    repeat(offset) { cells.add(null) }
    for (day in 1..month.lengthOfMonth()) {
        cells.add(month.atDay(day))
    }
    while (cells.size % 7 != 0) {
        cells.add(null)
    }
    return cells
}

@Composable
private fun CalendarDayCell(
    date: LocalDate?,
    isSelected: Boolean,
    hasEntries: Boolean,
    isToday: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val bg = when {
        date == null -> Color.Transparent
        isSelected -> MaterialTheme.colorScheme.tertiary
        isToday -> MaterialTheme.colorScheme.tertiary.copy(alpha = 0.22f)
        else -> Color.Transparent
    }
    val textColor = when {
        date == null -> Color.Transparent
        isSelected -> MaterialTheme.colorScheme.onTertiary
        else -> MaterialTheme.colorScheme.onSurface
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .padding(2.dp)
            .clip(CircleShape)
            .background(bg)
            .then(
                if (date != null) {
                    Modifier.clickable(onClick = onClick)
                } else {
                    Modifier
                }
            ),
        contentAlignment = Alignment.Center
    ) {
        if (date != null) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = date.dayOfMonth.toString(),
                    style = MaterialTheme.typography.bodyMedium,
                    color = textColor,
                    fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal
                )
                if (hasEntries) {
                    Box(
                        modifier = Modifier
                            .padding(top = 2.dp)
                            .size(5.dp)
                            .clip(CircleShape)
                            .background(
                                if (isSelected) {
                                    MaterialTheme.colorScheme.onTertiary
                                } else {
                                    MaterialTheme.colorScheme.tertiary
                                }
                            )
                    )
                }
            }
        }
    }
}

@Composable
private fun CalendarEntryCard(
    task: Task,
    onClick: () -> Unit
) {
    val borderColor = when {
        task.isNote -> MaterialTheme.colorScheme.outline.copy(alpha = 0.45f)
        task.status == TaskStatus.COMPLETED -> MaterialTheme.colorScheme.outline.copy(alpha = 0.55f)
        else -> MaterialTheme.colorScheme.tertiary
    }
    val title = if (task.isNote) {
        task.title.ifBlank { task.description.lineSequence().firstOrNull().orEmpty() }
    } else {
        task.title
    }

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .border(1.5.dp, borderColor, CardShape)
            .clickable(onClick = onClick),
        color = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
        shape = CardShape
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            EntryPriorityDot(task = task)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = if (task.isNote) {
                        stringResource(R.string.create_entry_note)
                    } else {
                        stringResource(R.string.create_entry_task)
                    },
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = title,
                    style = MaterialTheme.typography.titleSmall,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            EntryPriorityLabel(task = task)
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
                    Text(
                        text = selectedYear.toString(),
                        style = MaterialTheme.typography.titleLarge
                    )
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
                                    if (selected) {
                                        MaterialTheme.colorScheme.tertiary.copy(alpha = 0.35f)
                                    } else {
                                        MaterialTheme.colorScheme.surface
                                    }
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
