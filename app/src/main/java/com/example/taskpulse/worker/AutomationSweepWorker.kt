package com.example.taskpulse.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.taskpulse.core.requireAppContainer
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AutomationSweepWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        applicationContext.requireAppContainer().runAutomationSweepUseCase()
        Result.success()
    }
}
