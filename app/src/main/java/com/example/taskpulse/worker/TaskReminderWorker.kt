package com.example.taskpulse.worker

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters

class TaskReminderWorker(
    appContext: Context,
    workerParams: WorkerParameters
) : CoroutineWorker(appContext, workerParams) {
    override suspend fun doWork(): Result {
        // The reminder and automation flow will be expanded in later commits.
        return Result.success()
    }
}
