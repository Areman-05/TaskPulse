package com.example.taskpulse.domain.usecase

import android.content.Context
import com.example.taskpulse.domain.repository.AutomationSettingsRepository
import com.example.taskpulse.worker.AutomationWorkScheduler

class RescheduleAutomationSweepUseCase(
    private val appContext: Context,
    private val automationSettings: AutomationSettingsRepository
) {
    operator fun invoke(hours: Long) {
        AutomationWorkScheduler.enqueue(appContext, repeatIntervalHours = hours, settings = automationSettings)
    }
}
