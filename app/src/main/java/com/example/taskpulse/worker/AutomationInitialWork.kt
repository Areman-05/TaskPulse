package com.example.taskpulse.worker

import android.content.Context
import androidx.work.ExistingWorkPolicy
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Enqueues a single delayed automation sweep so freshly seeded rules run soon after install.
 */
object AutomationInitialWork {
    fun enqueueOnce(context: Context) {
        val request = OneTimeWorkRequestBuilder<AutomationSweepWorker>()
            .setInitialDelay(10, TimeUnit.SECONDS)
            .addTag(WorkerKeys.TAG_AUTOMATION_INITIAL)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WorkerKeys.UNIQUE_WORK_AUTOMATION_INITIAL,
            ExistingWorkPolicy.KEEP,
            request
        )
    }
}
