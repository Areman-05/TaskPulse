package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.testutil.FakeTaskRepository
import com.example.taskpulse.testutil.TaskTestFactory
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class CountPendingTasksUseCaseTest {

    @Test
    fun `counts non completed non archived tasks`() = runTest {
        val repo = FakeTaskRepository(
            listOf(
                TaskTestFactory.task(id = 1L, status = TaskStatus.PENDING),
                TaskTestFactory.task(id = 2L, status = TaskStatus.COMPLETED),
                TaskTestFactory.task(id = 3L, archivedAtMillis = 99L),
                TaskTestFactory.task(id = 4L, status = TaskStatus.IN_PROGRESS)
            )
        )
        val useCase = CountPendingTasksUseCase(repo)

        assertEquals(2, useCase())
    }
}
