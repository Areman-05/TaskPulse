package com.example.taskpulse.domain.usecase

import android.content.Context
import com.example.taskpulse.worker.AutomationWorkScheduler

class TriggerAutomationSweepNowUseCase(
    private val appContext: Context
) {
    operator fun invoke() {
        AutomationWorkScheduler.enqueueNow(appContext)
    }
}
