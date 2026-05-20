package com.example.taskpulse.ui.calendar

import com.example.taskpulse.domain.model.Task
import java.time.LocalDate
import java.time.YearMonth

data class CalendarUiState(
    val visibleMonth: YearMonth = YearMonth.now(),
    val selectedDate: LocalDate = LocalDate.now(),
    val datesWithEntries: Set<LocalDate> = emptySet(),
    val selectedDayTasks: List<Task> = emptyList(),
    val selectedDayNotes: List<Task> = emptyList(),
    val showMonthYearPicker: Boolean = false
)
