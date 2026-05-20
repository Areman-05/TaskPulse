package com.example.taskpulse.ui.calendar

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.domain.calendar.TaskCalendarDates
import com.example.taskpulse.domain.sort.sortedTasksThenNotes
import com.example.taskpulse.domain.usecase.ObserveTasksUseCase
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

class CalendarViewModel(
    observeTasksUseCase: ObserveTasksUseCase
) : ViewModel() {
    private val visibleMonth = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow(TaskCalendarDates.today())
    private val showMonthYearPicker = MutableStateFlow(false)

    val uiState: StateFlow<CalendarUiState> = combine(
        observeTasksUseCase(),
        visibleMonth,
        selectedDate,
        showMonthYearPicker
    ) { tasks, month, selected, pickerVisible ->
        val scheduled = tasks.filter(TaskCalendarDates::hasCalendarDate)
        val datesWithEntries = scheduled
            .map { TaskCalendarDates.toLocalDate(it.dueAtMillis!!) }
            .toSet()
        val selectedDayEntries = scheduled
            .filter { TaskCalendarDates.isOnCalendarDay(it, selected) }
            .sortedTasksThenNotes()
        CalendarUiState(
            visibleMonth = month,
            selectedDate = selected,
            datesWithEntries = datesWithEntries,
            selectedDayEntries = selectedDayEntries,
            showMonthYearPicker = pickerVisible
        )
    }.distinctUntilChanged().stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = CalendarUiState()
    )

    fun selectDate(date: LocalDate) {
        selectedDate.value = date
        val month = YearMonth.from(date)
        if (visibleMonth.value != month) {
            visibleMonth.value = month
        }
    }

    fun previousMonth() {
        visibleMonth.update { it.minusMonths(1) }
    }

    fun nextMonth() {
        visibleMonth.update { it.plusMonths(1) }
    }

    fun goToToday() {
        val today = TaskCalendarDates.today()
        selectedDate.value = today
        visibleMonth.value = YearMonth.from(today)
    }

    fun showMonthYearPicker() {
        showMonthYearPicker.value = true
    }

    fun dismissMonthYearPicker() {
        showMonthYearPicker.value = false
    }

    fun applyMonthYear(month: Int, year: Int) {
        visibleMonth.value = YearMonth.of(year, month)
        val day = selectedDate.value.dayOfMonth.coerceAtMost(visibleMonth.value.lengthOfMonth())
        selectedDate.value = LocalDate.of(year, month, day)
        dismissMonthYearPicker()
    }

    class Factory(
        private val observeTasksUseCase: ObserveTasksUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return CalendarViewModel(observeTasksUseCase) as T
        }
    }
}
