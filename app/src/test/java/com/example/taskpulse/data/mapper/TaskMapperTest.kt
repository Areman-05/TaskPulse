package com.example.taskpulse.data.mapper

import com.example.taskpulse.domain.model.RecurrenceUnit
import com.example.taskpulse.domain.model.TaskEntryType
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskRecurrence
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.testutil.TaskTestFactory
import org.junit.Assert.assertEquals
import org.junit.Test

class TaskMapperTest {

    @Test
    fun `round trip preserves task fields`() {
        val original = TaskTestFactory.task(
            id = 7L,
            title = "Mapper",
            priority = TaskPriority.HIGH,
            status = TaskStatus.IN_PROGRESS,
            entryType = TaskEntryType.TASK
        ).copy(
            description = "detalle",
            recurrence = TaskRecurrence(2, RecurrenceUnit.WEEK),
            blockedByTaskId = 3L,
            archivedAtMillis = null
        )

        val mapped = original.toEntity().toDomain()

        assertEquals(original.id, mapped.id)
        assertEquals(original.title, mapped.title)
        assertEquals(original.priority, mapped.priority)
        assertEquals(original.recurrence, mapped.recurrence)
        assertEquals(original.blockedByTaskId, mapped.blockedByTaskId)
    }

    @Test
    fun `round trip preserves note entry type`() {
        val note = TaskTestFactory.note(id = 9L, title = "Nota mapper")

        val mapped = note.toEntity().toDomain()

        assertEquals(TaskEntryType.NOTE, mapped.entryType)
        assertEquals("Nota mapper", mapped.title)
    }
}
