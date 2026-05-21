package com.example.taskpulse.core

import android.content.Context
import androidx.room.Room
import com.example.taskpulse.data.export.TaskSnapshotFileExporter
import com.example.taskpulse.data.local.TaskPulseDatabase
import com.example.taskpulse.data.repository.OfflineAutomationRuleRepository
import com.example.taskpulse.data.repository.OfflineAutomationSweepLogRepository
import com.example.taskpulse.data.repository.OfflineCategoryRepository
import com.example.taskpulse.data.repository.OfflineTaskRepository
import com.example.taskpulse.data.repository.SharedPreferencesThemeRepository
import com.example.taskpulse.data.repository.SharedPrefsAutomationSettingsRepository
import com.example.taskpulse.data.scheduler.WorkManagerTaskScheduler
import com.example.taskpulse.domain.automation.SimpleRuleEngine
import com.example.taskpulse.domain.repository.AutomationSettingsRepository
import com.example.taskpulse.domain.repository.ThemeRepository
import com.example.taskpulse.domain.scheduler.TaskScheduler
import com.example.taskpulse.domain.usecase.AppendAutomationSweepRunUseCase
import com.example.taskpulse.domain.usecase.CancelTaskReminderUseCase
import com.example.taskpulse.domain.usecase.CompleteTaskAndStopRemindersUseCase
import com.example.taskpulse.domain.usecase.CountPendingTasksUseCase
import com.example.taskpulse.domain.usecase.CreateDefaultTaskUseCase
import com.example.taskpulse.domain.usecase.DeleteTasksUseCase
import com.example.taskpulse.domain.usecase.EnsureDefaultCategoryUseCase
import com.example.taskpulse.domain.usecase.EnsureStarterAutomationRulesUseCase
import com.example.taskpulse.domain.usecase.EvaluateAutomationRulesUseCase
import com.example.taskpulse.domain.usecase.GetAutomationSweepIntervalUseCase
import com.example.taskpulse.domain.usecase.GetTaskUseCase
import com.example.taskpulse.domain.usecase.LoadAutomationSweepHistoryUseCase
import com.example.taskpulse.domain.usecase.MarkTaskCompletedUseCase
import com.example.taskpulse.domain.usecase.MarkTaskFailedUseCase
import com.example.taskpulse.domain.usecase.MarkTaskInProgressUseCase
import com.example.taskpulse.domain.usecase.ObserveAllTasksUseCase
import com.example.taskpulse.domain.usecase.ObserveArchivedTasksUseCase
import com.example.taskpulse.domain.usecase.ObserveTasksUseCase
import com.example.taskpulse.domain.usecase.RescheduleAutomationSweepUseCase
import com.example.taskpulse.domain.usecase.RestoreArchivedEntryUseCase
import com.example.taskpulse.domain.usecase.RunAutomationSweepUseCase
import com.example.taskpulse.domain.usecase.RunEntryLifecycleMaintenanceUseCase
import com.example.taskpulse.domain.usecase.ScheduleDependentRemindersUseCase
import com.example.taskpulse.domain.usecase.ScheduleRecurringTaskUseCase
import com.example.taskpulse.domain.usecase.ScheduleTaskReminderUseCase
import com.example.taskpulse.domain.usecase.SetAutomationSweepIntervalUseCase
import com.example.taskpulse.domain.usecase.SnoozeTaskAndReminderUseCase
import com.example.taskpulse.domain.usecase.SnoozeTaskUseCase
import com.example.taskpulse.domain.usecase.TriggerAutomationSweepNowUseCase
import com.example.taskpulse.domain.usecase.UpdateTasksPriorityUseCase
import com.example.taskpulse.domain.usecase.UpsertTaskUseCase
import com.example.taskpulse.notification.NotificationCooldownStore
import com.example.taskpulse.notification.TaskNotificationHelper

/**
 * Composition root manual: persistencia, casos de uso y servicios compartidos.
 * Instancia única en [com.example.taskpulse.TaskPulseApp].
 */
class AppContainer(context: Context) {
    private val appContext = context.applicationContext

    private val database: TaskPulseDatabase = Room.databaseBuilder(
        appContext,
        TaskPulseDatabase::class.java,
        "taskpulse.db"
    )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    private val taskRepository = OfflineTaskRepository(database.taskDao())
    private val categoryRepository = OfflineCategoryRepository(database.categoryDao())
    private val automationRuleRepository = OfflineAutomationRuleRepository(database.automationDao())
    private val automationSweepLogRepository =
        OfflineAutomationSweepLogRepository(database.automationSweepLogDao())

    val automationSettingsRepository: AutomationSettingsRepository =
        SharedPrefsAutomationSettingsRepository(appContext)

    private val scheduler: TaskScheduler = WorkManagerTaskScheduler(appContext)
    val notificationCooldownStore = NotificationCooldownStore(appContext)
    private val notificationHelper = TaskNotificationHelper(appContext, notificationCooldownStore)

    val themeRepository: ThemeRepository = SharedPreferencesThemeRepository(appContext)

    val observeTasksUseCase = ObserveTasksUseCase(taskRepository)
    val observeAllTasksUseCase = ObserveAllTasksUseCase(taskRepository)
    val observeArchivedTasksUseCase = ObserveArchivedTasksUseCase(taskRepository)
    val upsertTaskUseCase = UpsertTaskUseCase(taskRepository)
    val createDefaultTaskUseCase = CreateDefaultTaskUseCase()
    val updateTasksPriorityUseCase = UpdateTasksPriorityUseCase(taskRepository)
    val getTaskUseCase = GetTaskUseCase(taskRepository)
    val restoreArchivedEntryUseCase = RestoreArchivedEntryUseCase(taskRepository)
    val countPendingTasksUseCase = CountPendingTasksUseCase(taskRepository)

    val cancelTaskReminderUseCase = CancelTaskReminderUseCase(scheduler)
    val scheduleTaskReminderUseCase = ScheduleTaskReminderUseCase(scheduler)
    val scheduleRecurringTaskUseCase = ScheduleRecurringTaskUseCase(scheduler)

    val markTaskInProgressUseCase = MarkTaskInProgressUseCase(taskRepository)
    val markTaskFailedUseCase = MarkTaskFailedUseCase(taskRepository)

    val deleteTasksUseCase = DeleteTasksUseCase(taskRepository, cancelTaskReminderUseCase)

    val scheduleDependentRemindersUseCase = ScheduleDependentRemindersUseCase(
        taskRepository,
        scheduleTaskReminderUseCase,
        scheduleRecurringTaskUseCase
    )

    val completeTaskAndStopRemindersUseCase = CompleteTaskAndStopRemindersUseCase(
        MarkTaskCompletedUseCase(taskRepository),
        cancelTaskReminderUseCase,
        scheduleDependentRemindersUseCase,
        notificationCooldownStore
    )

    val snoozeTaskAndReminderUseCase = SnoozeTaskAndReminderUseCase(
        SnoozeTaskUseCase(taskRepository),
        getTaskUseCase,
        cancelTaskReminderUseCase,
        scheduleTaskReminderUseCase,
        notificationCooldownStore
    )

    val runEntryLifecycleMaintenanceUseCase = RunEntryLifecycleMaintenanceUseCase(
        taskRepository,
        completeTaskAndStopRemindersUseCase
    )

    val runAutomationSweepUseCase = RunAutomationSweepUseCase(
        taskRepository,
        automationRuleRepository,
        runEntryLifecycleMaintenanceUseCase,
        EvaluateAutomationRulesUseCase(SimpleRuleEngine()),
        markTaskInProgressUseCase,
        markTaskFailedUseCase,
        AppendAutomationSweepRunUseCase(automationSweepLogRepository),
        notificationHelper
    )

    val ensureDefaultCategoryUseCase = EnsureDefaultCategoryUseCase(categoryRepository)
    val ensureStarterAutomationRulesUseCase =
        EnsureStarterAutomationRulesUseCase(automationRuleRepository)
    val triggerAutomationSweepNowUseCase = TriggerAutomationSweepNowUseCase(appContext)
    val getAutomationSweepIntervalUseCase =
        GetAutomationSweepIntervalUseCase(automationSettingsRepository)
    val setAutomationSweepIntervalUseCase =
        SetAutomationSweepIntervalUseCase(automationSettingsRepository)
    val rescheduleAutomationSweepUseCase =
        RescheduleAutomationSweepUseCase(appContext, automationSettingsRepository)
    val loadAutomationSweepHistoryUseCase =
        LoadAutomationSweepHistoryUseCase(automationSweepLogRepository)

    val taskSnapshotFileExporter =
        TaskSnapshotFileExporter(taskRepository, appContext.filesDir)

    fun getAutomationSweepIntervalHours(): Long = getAutomationSweepIntervalUseCase()
}
