package com.example.taskpulse.domain.sort

import com.example.taskpulse.domain.calendar.TaskCalendarDates
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.testutil.TaskTestFactory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class TodayTaskFilterTest {

    private val zone = ZoneId.systemDefault()

    @Test
    fun `pending task without due date counts for today`() {
        val task = TaskTestFactory.task(id = 1L, dueAtMillis = null)
        assertTrue(isTaskForToday(task))
    }

    @Test
    fun `task due today is included`() {
        val today = TaskCalendarDates.today()
        val task = TaskTestFactory.task(id = 1L, dueAtMillis = TaskTestFactory.dueOn(today))
        assertTrue(isTaskForToday(task))
    }

    @Test
    fun `overdue pending task is included`() {
        val yesterday = TaskCalendarDates.today().minusDays(1)
        val task = TaskTestFactory.task(id = 1L, dueAtMillis = TaskTestFactory.dueOn(yesterday))
        assertTrue(isTaskForToday(task))
    }

    @Test
    fun `future task is excluded`() {
        val tomorrow = TaskCalendarDates.today().plusDays(1)
        val task = TaskTestFactory.task(id = 1L, dueAtMillis = TaskTestFactory.dueOn(tomorrow))
        assertFalse(isTaskForToday(task))
    }

    @Test
    fun `completed task is never shown even if updated today`() {
        val task = TaskTestFactory.task(
            id = 1L,
            status = TaskStatus.COMPLETED,
            updatedAtMillis = System.currentTimeMillis()
        )
        assertFalse(isTaskForToday(task))
    }

    @Test
    fun `completed yesterday is excluded`() {
        val yesterday = LocalDate.now(zone).minusDays(1)
            .atStartOfDay(zone).toInstant().toEpochMilli()
        val task = TaskTestFactory.task(
            id = 1L,
            status = TaskStatus.COMPLETED,
            updatedAtMillis = yesterday
        )
        assertFalse(isTaskForToday(task))
    }
}
