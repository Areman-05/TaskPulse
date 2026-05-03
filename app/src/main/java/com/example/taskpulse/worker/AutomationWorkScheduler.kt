package com.example.taskpulse.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.NetworkType
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules a lightweight automation sweep that evaluates persisted rules against local tasks.
 */
object AutomationWorkScheduler {
    fun enqueue(context: Context) {
        val request = PeriodicWorkRequestBuilder<AutomationSweepWorker>(1, TimeUnit.HOURS)
            .setConstraints(
                Constraints.Builder()
                    .setRequiredNetworkType(NetworkType.NOT_REQUIRED)
                    .build()
            )
            .addTag(WorkerKeys.TAG_AUTOMATION_SWEEP)
            .build()

        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WorkerKeys.UNIQUE_WORK_AUTOMATION_SWEEP,
            ExistingPeriodicWorkPolicy.KEEP,
            request
        )
    }
}
