package com.example.taskpulse.domain.usecase

import com.example.taskpulse.testutil.FakeTaskRepository
import com.example.taskpulse.testutil.FakeTaskScheduler
import com.example.taskpulse.testutil.TaskTestFactory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class DeleteTasksUseCaseTest {

    @Test
    fun `deletes tasks and cancels reminders`() = runTest {
        val repo = FakeTaskRepository(
            listOf(
                TaskTestFactory.task(id = 1L),
                TaskTestFactory.task(id = 2L)
            )
        )
        val scheduler = FakeTaskScheduler()
        val useCase = DeleteTasksUseCase(repo, CancelTaskReminderUseCase(scheduler))

        useCase(listOf(1L))

        assertNull(repo.getTask(1L))
        assertTrue(repo.getTask(2L) != null)
        assertEquals(listOf(1L), scheduler.cancelledReminders)
    }

    @Test
    fun `ignores empty id list`() = runTest {
        val repo = FakeTaskRepository(listOf(TaskTestFactory.task(id = 1L)))
        val scheduler = FakeTaskScheduler()
        val useCase = DeleteTasksUseCase(repo, CancelTaskReminderUseCase(scheduler))

        useCase(emptyList())

        assertTrue(scheduler.cancelledReminders.isEmpty())
        assertTrue(repo.getTask(1L) != null)
    }
}
