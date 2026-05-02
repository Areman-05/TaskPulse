package com.example.taskpulse.core

import android.content.Context
import androidx.room.Room
import com.example.taskpulse.data.local.TaskPulseDatabase
import com.example.taskpulse.data.repository.OfflineAutomationRuleRepository
import com.example.taskpulse.data.repository.OfflineCategoryRepository
import com.example.taskpulse.data.repository.OfflineTaskRepository
import com.example.taskpulse.data.scheduler.WorkManagerTaskScheduler
import com.example.taskpulse.domain.scheduler.TaskScheduler
import com.example.taskpulse.domain.usecase.CreateDefaultTaskUseCase
import com.example.taskpulse.domain.usecase.EnsureDefaultCategoryUseCase
import com.example.taskpulse.domain.usecase.MarkTaskCompletedUseCase
import com.example.taskpulse.domain.usecase.MarkTaskInProgressUseCase
import com.example.taskpulse.domain.usecase.EnsureStarterAutomationRulesUseCase
import com.example.taskpulse.domain.usecase.ObserveAutomationRulesUseCase
import com.example.taskpulse.domain.usecase.ObserveDailyProductivityUseCase
import com.example.taskpulse.domain.usecase.ObserveTasksUseCase
import com.example.taskpulse.domain.usecase.ScheduleRecurringTaskUseCase
import com.example.taskpulse.domain.usecase.ScheduleTaskReminderUseCase
import com.example.taskpulse.domain.usecase.SnoozeTaskUseCase
import com.example.taskpulse.domain.usecase.UpsertTaskUseCase

class AppContainer(context: Context) {
    private val database: TaskPulseDatabase = Room.databaseBuilder(
        context.applicationContext,
        TaskPulseDatabase::class.java,
        "taskpulse.db"
    )
        .fallbackToDestructiveMigration()
        .build()

    private val repository = OfflineTaskRepository(database.taskDao())
    private val categoryRepository = OfflineCategoryRepository(database.categoryDao())
    private val automationRepository = OfflineAutomationRuleRepository(database.automationDao())
    private val scheduler: TaskScheduler = WorkManagerTaskScheduler(context.applicationContext)

    val observeTasksUseCase = ObserveTasksUseCase(repository)
    val ensureDefaultCategoryUseCase = EnsureDefaultCategoryUseCase(categoryRepository)
    val observeAutomationRulesUseCase = ObserveAutomationRulesUseCase(automationRepository)
    val ensureStarterAutomationRulesUseCase = EnsureStarterAutomationRulesUseCase(automationRepository)
    val observeDailyProductivityUseCase = ObserveDailyProductivityUseCase(repository)
    val upsertTaskUseCase = UpsertTaskUseCase(repository)
    val createDefaultTaskUseCase = CreateDefaultTaskUseCase()
    val scheduleTaskReminderUseCase = ScheduleTaskReminderUseCase(scheduler)
    val scheduleRecurringTaskUseCase = ScheduleRecurringTaskUseCase(scheduler)
    val markTaskCompletedUseCase = MarkTaskCompletedUseCase(repository)
    val markTaskInProgressUseCase = MarkTaskInProgressUseCase(repository)
    val snoozeTaskUseCase = SnoozeTaskUseCase(repository)
}
