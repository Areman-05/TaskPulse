package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.repository.AutomationSweepLogRepository

class AppendAutomationSweepRunUseCase(
    private val repository: AutomationSweepLogRepository
) {
    suspend operator fun invoke(matchCount: Int, ranAtMillis: Long = System.currentTimeMillis()) {
        repository.recordRun(matchCount, ranAtMillis)
    }
}
