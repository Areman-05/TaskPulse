package com.example.taskpulse.domain.calendar

import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.testutil.TaskTestFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate

class TaskCalendarDatesTest {

    @Test
    fun `defaultDueMillis uses nine am on calendar day`() {
        val day = LocalDate.of(2026, 6, 15)
        val millis = TaskCalendarDates.defaultDueMillis(day)

        assertTrue(TaskCalendarDates.isCalendarDueTime(millis))
    }

    @Test
    fun `isOnCalendarDay matches same local day`() {
        val day = LocalDate.of(2026, 6, 15)
        val task = TaskTestFactory.task(dueAtMillis = TaskCalendarDates.defaultDueMillis(day))

        assertTrue(TaskCalendarDates.isOnCalendarDay(task, day))
        assertFalse(TaskCalendarDates.isOnCalendarDay(task, day.plusDays(1)))
    }

    @Test
    fun `reminderFireAtMillis subtracts offset when task has calendar date`() {
        val due = TaskCalendarDates.defaultDueMillis(LocalDate.of(2026, 6, 1))
        val fire = TaskCalendarDates.reminderFireAtMillis(due, offsetMinutes = 30, hasCalendarDate = true)

        assertEquals(due - 30 * 60_000L, fire)
    }

    @Test
    fun `taskReminderEnabled is false for calendar-only due time`() {
        val day = LocalDate.of(2026, 7, 1)
        val task = TaskTestFactory.task(
            dueAtMillis = TaskCalendarDates.defaultDueMillis(day),
            status = TaskStatus.PENDING
        )

        assertFalse(taskReminderEnabled(task))
    }

    @Test
    fun `taskReminderEnabled is true when due is not nine am slot`() {
        val task = TaskTestFactory.task(
            dueAtMillis = System.currentTimeMillis() + 60_000L,
            status = TaskStatus.PENDING
        )

        assertTrue(taskReminderEnabled(task))
    }

    @Test
    fun `taskReminderEnabled is false for notes`() {
        val note = TaskTestFactory.note(dueAtMillis = System.currentTimeMillis())

        assertFalse(taskReminderEnabled(note))
    }
}
