package com.example.taskpulse.domain.usecase

import android.content.Context
import com.example.taskpulse.worker.AutomationWorkScheduler

class RescheduleAutomationSweepUseCase(
    private val appContext: Context
) {
    operator fun invoke(hours: Long) {
        AutomationWorkScheduler.enqueue(appContext, repeatIntervalHours = hours)
    }
}
