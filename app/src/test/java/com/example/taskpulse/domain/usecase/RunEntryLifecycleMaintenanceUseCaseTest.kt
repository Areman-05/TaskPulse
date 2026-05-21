package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.lifecycle.EntryLifecyclePolicy
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.testutil.TaskTestFactory
import com.example.taskpulse.testutil.FakeTaskRepository
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test
import java.time.LocalDate

class RunEntryLifecycleMaintenanceUseCaseTest {

    private val today = LocalDate.of(2026, 6, 10)
    private val completeTask = mockk<CompleteTaskAndStopRemindersUseCase>(relaxed = true)

    @Test
    fun `auto completes overdue task with calendar date`() = runTest {
        val overdueDay = today.minusDays(1)
        val repo = FakeTaskRepository(
            listOf(
                TaskTestFactory.task(
                    id = 1L,
                    dueAtMillis = TaskTestFactory.dueOn(overdueDay),
                    status = TaskStatus.PENDING
                )
            )
        )
        val useCase = RunEntryLifecycleMaintenanceUseCase(repo, completeTask)

        val result = useCase(nowMillis = 0L, today = today)

        assertEquals(1, result.autoCompleted)
        coVerify(exactly = 1) { completeTask(1L, 0L) }
    }

    @Test
    fun `does not auto complete note entries`() = runTest {
        val repo = FakeTaskRepository(
            listOf(
                TaskTestFactory.note(
                    id = 2L,
                    dueAtMillis = TaskTestFactory.dueOn(today.minusDays(5))
                )
            )
        )
        val useCase = RunEntryLifecycleMaintenanceUseCase(repo, completeTask)

        val result = useCase(nowMillis = 0L, today = today)

        assertEquals(0, result.autoCompleted)
        coVerify(exactly = 0) { completeTask(any(), any()) }
    }

    @Test
    fun `archives task two days after due date`() = runTest {
        val dueDay = today.minusDays(EntryLifecyclePolicy.ARCHIVE_DAYS_AFTER_DUE)
        val repo = FakeTaskRepository(
            listOf(
                TaskTestFactory.task(
                    id = 3L,
                    dueAtMillis = TaskTestFactory.dueOn(dueDay),
                    status = TaskStatus.COMPLETED
                )
            )
        )
        val useCase = RunEntryLifecycleMaintenanceUseCase(repo, completeTask)

        val result = useCase(nowMillis = 100L, today = today)

        assertEquals(1, result.archived)
        assertEquals(100L, repo.getTask(3L)?.archivedAtMillis)
    }

    @Test
    fun `archives undated entry after fourteen days`() = runTest {
        val zone = java.time.ZoneId.systemDefault()
        val created = today.minusDays(EntryLifecyclePolicy.ARCHIVE_UNDATED_AFTER_DAYS)
            .atStartOfDay(zone)
            .toInstant()
            .toEpochMilli()
        val repo = FakeTaskRepository(
            listOf(
                TaskTestFactory.task(
                    id = 4L,
                    dueAtMillis = null,
                    createdAtMillis = created,
                    updatedAtMillis = created
                )
            )
        )
        val useCase = RunEntryLifecycleMaintenanceUseCase(repo, completeTask)

        val result = useCase(nowMillis = 200L, today = today)

        assertEquals(1, result.archived)
    }

    @Test
    fun `skips already archived entries`() = runTest {
        val repo = FakeTaskRepository(
            listOf(
                TaskTestFactory.task(
                    id = 5L,
                    archivedAtMillis = 50L
                )
            )
        )
        val useCase = RunEntryLifecycleMaintenanceUseCase(repo, completeTask)

        val result = useCase(today = today)

        assertEquals(0, result.autoCompleted)
        assertEquals(0, result.archived)
    }
}
