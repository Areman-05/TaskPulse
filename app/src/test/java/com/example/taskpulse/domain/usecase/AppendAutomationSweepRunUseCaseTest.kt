package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.repository.AutomationSweepLogRepository
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class AppendAutomationSweepRunUseCaseTest {

    @Test
    fun `records sweep run with match count`() = runTest {
        val repo = InMemorySweepLogRepository()
        val useCase = AppendAutomationSweepRunUseCase(repo)

        useCase(matchCount = 3, ranAtMillis = 500L)

        assertEquals(1, repo.runs.size)
        assertEquals(3, repo.runs.first().triggeredMatchCount)
        assertEquals(500L, repo.runs.first().ranAtMillis)
    }

    private class InMemorySweepLogRepository : AutomationSweepLogRepository {
        val runs = mutableListOf<com.example.taskpulse.domain.model.AutomationSweepRun>()

        override suspend fun recordRun(triggeredMatchCount: Int, ranAtMillis: Long) {
            runs.add(
                com.example.taskpulse.domain.model.AutomationSweepRun(
                    id = runs.size.toLong() + 1,
                    ranAtMillis = ranAtMillis,
                    triggeredMatchCount = triggeredMatchCount
                )
            )
        }

        override suspend fun recentRuns(limit: Int) = runs.takeLast(limit)
    }
}
