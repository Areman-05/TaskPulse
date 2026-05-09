package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.AutomationSweepRun
import com.example.taskpulse.domain.repository.AutomationSweepLogRepository

class LoadAutomationSweepHistoryUseCase(
    private val repository: AutomationSweepLogRepository
) {
    suspend operator fun invoke(limit: Int = 8): List<AutomationSweepRun> =
        repository.recentRuns(limit)
}
