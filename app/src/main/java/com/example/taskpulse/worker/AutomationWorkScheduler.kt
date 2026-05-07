package com.example.taskpulse.worker

import android.content.Context
import androidx.work.Constraints
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import java.util.concurrent.TimeUnit

/**
 * Schedules a lightweight automation sweep that evaluates persisted rules against local tasks.
 */
object AutomationWorkScheduler {
    fun enqueue(context: Context, repeatIntervalHours: Long) {
        val clampedInterval = repeatIntervalHours.coerceAtLeast(1L)
        val request = PeriodicWorkRequestBuilder<AutomationSweepWorker>(clampedInterval, TimeUnit.HOURS)
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

    fun enqueueNow(context: Context) {
        val request = OneTimeWorkRequestBuilder<AutomationSweepWorker>()
            .addTag(WorkerKeys.TAG_AUTOMATION_INITIAL)
            .build()

        WorkManager.getInstance(context).enqueueUniqueWork(
            WorkerKeys.UNIQUE_WORK_AUTOMATION_INITIAL,
            ExistingWorkPolicy.REPLACE,
            request
        )
    }
}
