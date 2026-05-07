package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.repository.AutomationSettingsRepository

class GetAutomationSweepIntervalUseCase(
    private val settingsRepository: AutomationSettingsRepository
) {
    operator fun invoke(): Long = settingsRepository.getSweepIntervalHours()
}
