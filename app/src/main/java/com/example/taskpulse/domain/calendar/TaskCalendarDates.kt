package com.example.taskpulse.domain.calendar

import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.isTaskItem
import java.time.Instant
import java.time.LocalDate
import java.time.YearMonth
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val zone: ZoneId get() = ZoneId.systemDefault()

object TaskCalendarDates {
    private val monthYearFormatter =
        DateTimeFormatter.ofPattern("MMMM yyyy", Locale("es", "ES"))

    fun today(): LocalDate = LocalDate.now(zone)

    fun defaultDueMillis(date: LocalDate): Long =
        date.atTime(9, 0).atZone(zone).toInstant().toEpochMilli()

    fun toLocalDate(millis: Long): LocalDate =
        Instant.ofEpochMilli(millis).atZone(zone).toLocalDate()

    fun millisFromPickerUtcDay(utcDayMillis: Long): Long =
        defaultDueMillis(
            Instant.ofEpochMilli(utcDayMillis).atZone(zone).toLocalDate()
        )

    fun isOnCalendarDay(task: Task, day: LocalDate): Boolean {
        val due = task.dueAtMillis ?: return false
        return toLocalDate(due) == day
    }

    fun hasCalendarDate(task: Task): Boolean = task.dueAtMillis != null

    fun reminderFireAtMillis(dueAtMillis: Long, offsetMinutes: Int, hasCalendarDate: Boolean): Long =
        if (hasCalendarDate) {
            dueAtMillis - offsetMinutes * 60_000L
        } else {
            dueAtMillis
        }

    fun formatMonthYear(yearMonth: YearMonth): String =
        yearMonth.atDay(1).format(monthYearFormatter).replaceFirstChar { it.titlecase(Locale("es", "ES")) }

    fun formatDayLabel(date: LocalDate): String =
        date.format(DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES")))

    fun isCalendarDueTime(millis: Long): Boolean {
        val zoned = Instant.ofEpochMilli(millis).atZone(zone)
        return zoned.hour == 9 && zoned.minute == 0
    }
}

/** Aviso programado (no solo fecha en calendario a las 9:00). */
fun taskReminderEnabled(task: Task): Boolean {
    val due = task.dueAtMillis ?: return false
    if (!task.isTaskItem) return false
    return !TaskCalendarDates.isCalendarDueTime(due)
}
