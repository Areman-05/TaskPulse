package com.example.taskpulse.core

import android.content.Context
import androidx.room.Room
import com.example.taskpulse.data.local.TaskPulseDatabase
import com.example.taskpulse.data.repository.OfflineTaskRepository
import com.example.taskpulse.data.scheduler.WorkManagerTaskScheduler
import com.example.taskpulse.domain.scheduler.TaskScheduler
import com.example.taskpulse.domain.usecase.CreateDefaultTaskUseCase
import com.example.taskpulse.domain.usecase.ObserveTasksUseCase
import com.example.taskpulse.domain.usecase.ScheduleTaskReminderUseCase
import com.example.taskpulse.domain.usecase.UpsertTaskUseCase

class AppContainer(context: Context) {
    private val database: TaskPulseDatabase = Room.databaseBuilder(
        context.applicationContext,
        TaskPulseDatabase::class.java,
        "taskpulse.db"
    ).build()

    private val repository = OfflineTaskRepository(database.taskDao())
    private val scheduler: TaskScheduler = WorkManagerTaskScheduler(context.applicationContext)

    val observeTasksUseCase = ObserveTasksUseCase(repository)
    val upsertTaskUseCase = UpsertTaskUseCase(repository)
    val createDefaultTaskUseCase = CreateDefaultTaskUseCase()
    val scheduleTaskReminderUseCase = ScheduleTaskReminderUseCase(scheduler)
}
