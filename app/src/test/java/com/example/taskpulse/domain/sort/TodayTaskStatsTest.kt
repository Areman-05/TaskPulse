package com.example.taskpulse.domain.sort

import com.example.taskpulse.domain.calendar.TaskCalendarDates
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.testutil.TaskTestFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class TodayTaskStatsTest {

    @Test
    fun `completion percent uses today workload only`() {
        val today = TaskCalendarDates.today()
        val tomorrow = today.plusDays(1)
        val tasks = listOf(
            TaskTestFactory.task(id = 1L, dueAtMillis = TaskTestFactory.dueOn(today)),
            TaskTestFactory.task(
                id = 2L,
                status = TaskStatus.COMPLETED,
                dueAtMillis = TaskTestFactory.dueOn(today),
                updatedAtMillis = System.currentTimeMillis()
            ),
            TaskTestFactory.task(id = 3L, dueAtMillis = TaskTestFactory.dueOn(tomorrow))
        )

        val stats = computeTodayTaskStats(tasks)

        assertEquals(1, stats.activeTodayCount)
        assertEquals(1, stats.completedTodayCount)
        assertEquals(50, stats.completionPercent)
    }

    @Test
    fun `next upcoming ignores completed and past due`() {
        val today = TaskCalendarDates.today()
        val futureDue = System.currentTimeMillis() + 3_600_000L
        val tasks = listOf(
            TaskTestFactory.task(id = 1L, dueAtMillis = TaskTestFactory.dueOn(today.minusDays(1))),
            TaskTestFactory.task(id = 2L, dueAtMillis = futureDue),
            TaskTestFactory.task(
                id = 3L,
                status = TaskStatus.COMPLETED,
                dueAtMillis = futureDue,
                updatedAtMillis = System.currentTimeMillis()
            )
        )

        val next = findNextUpcomingTask(tasks)

        assertTrue(next?.id == 2L)
    }

    @Test
    fun `next upcoming is null when nothing scheduled ahead`() {
        val today = TaskCalendarDates.today()
        val tasks = listOf(
            TaskTestFactory.task(id = 1L, dueAtMillis = TaskTestFactory.dueOn(today))
        )

        assertNull(findNextUpcomingTask(tasks))
    }
}
