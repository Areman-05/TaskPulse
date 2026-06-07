package com.example.taskpulse.domain.sort

import com.example.taskpulse.domain.calendar.TaskCalendarDates
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.testutil.TaskTestFactory
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class HomeEntryListSortTest {

    @Test
    fun `search filters by title and description`() {
        val tasks = listOf(
            TaskTestFactory.task(id = 1L, title = "Comprar leche"),
            TaskTestFactory.task(id = 2L, title = "Otra", description = "leche descremada")
        )

        val result = filterAndPartitionHomeEntries(
            tasks = tasks,
            query = "leche",
            sortField = TaskSortField.PRIORITY,
            sortOrder = TaskSortOrder.NEWEST_FIRST
        )

        assertEquals(2, result.tasks.size)
        assertTrue(result.notes.isEmpty())
    }

    @Test
    fun `partitions tasks and notes`() {
        val tasks = listOf(
            TaskTestFactory.task(id = 1L),
            TaskTestFactory.note(id = 2L)
        )

        val result = filterAndPartitionHomeEntries(
            tasks = tasks,
            query = "",
            sortField = TaskSortField.PRIORITY,
            sortOrder = TaskSortOrder.NEWEST_FIRST
        )

        assertEquals(1, result.tasks.size)
        assertEquals(1, result.notes.size)
        assertEquals(2L, result.notes.first().id)
    }

    @Test
    fun `sorts tasks by priority critical first`() {
        val tasks = listOf(
            TaskTestFactory.task(id = 1L, priority = TaskPriority.LOW),
            TaskTestFactory.task(id = 2L, priority = TaskPriority.CRITICAL),
            TaskTestFactory.task(id = 3L, priority = TaskPriority.HIGH)
        )

        val result = filterAndPartitionHomeEntries(
            tasks = tasks,
            query = "",
            sortField = TaskSortField.PRIORITY,
            sortOrder = TaskSortOrder.NEWEST_FIRST
        )

        assertEquals(listOf(2L, 3L, 1L), result.tasks.map { it.id })
    }

    @Test
    fun `today filter excludes completed and future tasks`() {
        val today = TaskCalendarDates.today()
        val tasks = listOf(
            TaskTestFactory.task(id = 1L, dueAtMillis = TaskTestFactory.dueOn(today)),
            TaskTestFactory.task(id = 2L, dueAtMillis = TaskTestFactory.dueOn(today.plusDays(1))),
            TaskTestFactory.task(
                id = 3L,
                status = com.example.taskpulse.domain.model.TaskStatus.COMPLETED,
                updatedAtMillis = System.currentTimeMillis()
            ),
            TaskTestFactory.note(id = 4L)
        )

        val result = filterAndPartitionHomeEntries(
            tasks = tasks,
            query = "",
            sortField = TaskSortField.PRIORITY,
            sortOrder = TaskSortOrder.NEWEST_FIRST
        )

        assertEquals(listOf(1L), result.tasks.map { it.id })
        assertEquals(1, result.notes.size)
    }

    @Test
    fun `sorts by title alphabetically`() {
        val tasks = listOf(
            TaskTestFactory.task(id = 1L, title = "Zebra"),
            TaskTestFactory.task(id = 2L, title = "Alpha")
        )

        val result = filterAndPartitionHomeEntries(
            tasks = tasks,
            query = "",
            sortField = TaskSortField.TITLE,
            sortOrder = TaskSortOrder.OLDEST_FIRST
        )

        assertEquals(listOf(2L, 1L), result.tasks.map { it.id })
    }
}
