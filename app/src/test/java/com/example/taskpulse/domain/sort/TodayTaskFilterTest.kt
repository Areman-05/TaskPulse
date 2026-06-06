package com.example.taskpulse.domain.sort

import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.testutil.TaskTestFactory
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import java.time.LocalDate
import java.time.ZoneId

class TodayTaskFilterTest {

    private val zone = ZoneId.of("Europe/Madrid")

    @Test
    fun `pending task without due date counts for today`() {
        val task = TaskTestFactory.task(id = 1L, dueAtMillis = null)
        assertTrue(isTaskForToday(task, zone))
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
        assertFalse(isTaskForToday(task, zone))
    }

    @Test
    fun `completed today is included`() {
        val now = System.currentTimeMillis()
        val task = TaskTestFactory.task(
            id = 1L,
            status = TaskStatus.COMPLETED,
            updatedAtMillis = now
        )
        assertTrue(isTaskForToday(task, zone))
    }
}
