package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.TaskEntryType
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class CreateDefaultTaskUseCaseTest {

    private val useCase = CreateDefaultTaskUseCase()

    @Test
    fun `creates pending task with defaults`() {
        val task = useCase("Mi tarea", categoryId = 5L, nowMillis = 42L)

        assertEquals(0L, task.id)
        assertEquals(5L, task.categoryId)
        assertEquals("Mi tarea", task.title)
        assertEquals(TaskStatus.PENDING, task.status)
        assertEquals(TaskPriority.MEDIUM, task.priority)
        assertEquals(TaskEntryType.TASK, task.entryType)
        assertEquals(42L, task.createdAtMillis)
    }

    @Test
    fun `creates note when entry type is note`() {
        val note = useCase("Apunte", categoryId = 1L, nowMillis = 1L, entryType = TaskEntryType.NOTE)

        assertEquals(TaskEntryType.NOTE, note.entryType)
    }
}
