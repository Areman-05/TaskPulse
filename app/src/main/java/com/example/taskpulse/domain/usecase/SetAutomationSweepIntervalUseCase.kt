package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.repository.AutomationSettingsRepository

class SetAutomationSweepIntervalUseCase(
    private val settingsRepository: AutomationSettingsRepository
) {
    operator fun invoke(hours: Long) {
        settingsRepository.setSweepIntervalHours(hours)
    }
}
