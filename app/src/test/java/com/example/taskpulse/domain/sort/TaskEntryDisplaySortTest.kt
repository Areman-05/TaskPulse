package com.example.taskpulse.domain.sort

import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.testutil.TaskTestFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskEntryDisplaySortTest {

    @Test
    fun `sortedByDisplayPriority orders critical before low`() {
        val tasks = listOf(
            TaskTestFactory.task(id = 1L, priority = TaskPriority.LOW),
            TaskTestFactory.task(id = 2L, priority = TaskPriority.CRITICAL)
        )

        val sorted = tasks.sortedByDisplayPriority()

        assertEquals(2L, sorted.first().id)
    }

    @Test
    fun `sortedTasksThenNotes places notes after tasks`() {
        val mixed = listOf(
            TaskTestFactory.note(id = 10L),
            TaskTestFactory.task(id = 1L, priority = TaskPriority.HIGH),
            TaskTestFactory.task(id = 2L, priority = TaskPriority.LOW)
        )

        val sorted = mixed.sortedTasksThenNotes()

        assertEquals(listOf(1L, 2L, 10L), sorted.map { it.id })
    }
}
