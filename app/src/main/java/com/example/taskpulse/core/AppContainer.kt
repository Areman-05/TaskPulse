package com.example.taskpulse.core

import android.content.Context
import androidx.room.Room
import com.example.taskpulse.data.local.TaskPulseDatabase
import com.example.taskpulse.data.export.TaskSnapshotFileExporter
import com.example.taskpulse.data.repository.OfflineAutomationRuleRepository
import com.example.taskpulse.data.repository.OfflineAutomationSweepLogRepository
import com.example.taskpulse.data.repository.OfflineCategoryRepository
import com.example.taskpulse.data.repository.OfflineTaskRepository
import com.example.taskpulse.data.repository.SharedPreferencesThemeRepository
import com.example.taskpulse.data.repository.SharedPrefsAutomationSettingsRepository
import com.example.taskpulse.data.scheduler.WorkManagerTaskScheduler
import com.example.taskpulse.domain.automation.SimpleRuleEngine
import com.example.taskpulse.domain.model.AutomationRule
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.repository.AutomationSettingsRepository
import com.example.taskpulse.domain.scheduler.TaskScheduler
import com.example.taskpulse.domain.usecase.AppendAutomationSweepRunUseCase
import com.example.taskpulse.domain.usecase.CancelTaskReminderUseCase
import com.example.taskpulse.domain.usecase.CompleteTaskAndStopRemindersUseCase
import com.example.taskpulse.domain.usecase.CreateDefaultTaskUseCase
import com.example.taskpulse.domain.usecase.DeleteTasksUseCase
import com.example.taskpulse.domain.usecase.DeleteAutomationRuleUseCase
import com.example.taskpulse.domain.usecase.EvaluateAutomationRulesUseCase
import com.example.taskpulse.domain.usecase.EnsureDefaultCategoryUseCase
import com.example.taskpulse.domain.usecase.MarkTaskCompletedUseCase
import com.example.taskpulse.domain.usecase.MarkTaskFailedUseCase
import com.example.taskpulse.domain.usecase.MarkTaskInProgressUseCase
import com.example.taskpulse.domain.usecase.EnsureStarterAutomationRulesUseCase
import com.example.taskpulse.domain.usecase.GetAutomationSweepIntervalUseCase
import com.example.taskpulse.domain.usecase.GetAutomationRuleUseCase
import com.example.taskpulse.domain.usecase.GetEnabledAutomationRuleCountUseCase
import com.example.taskpulse.domain.usecase.GetTaskUseCase
import com.example.taskpulse.domain.usecase.ObserveAutomationRulesUseCase
import com.example.taskpulse.domain.usecase.LoadAutomationSweepHistoryUseCase
import com.example.taskpulse.domain.usecase.ObserveDailyProductivityUseCase
import com.example.taskpulse.domain.usecase.ObserveAllTasksUseCase
import com.example.taskpulse.domain.usecase.ObserveArchivedTasksUseCase
import com.example.taskpulse.domain.usecase.ObserveTasksUseCase
import com.example.taskpulse.domain.usecase.RestoreArchivedEntryUseCase
import com.example.taskpulse.domain.usecase.RunEntryLifecycleMaintenanceUseCase
import com.example.taskpulse.domain.usecase.ScheduleDependentRemindersUseCase
import com.example.taskpulse.domain.usecase.ScheduleRecurringTaskUseCase
import com.example.taskpulse.domain.usecase.ScheduleTaskReminderUseCase
import com.example.taskpulse.domain.usecase.SetAutomationRuleEnabledUseCase
import com.example.taskpulse.domain.usecase.SetAutomationSweepIntervalUseCase
import com.example.taskpulse.domain.usecase.RescheduleAutomationSweepUseCase
import com.example.taskpulse.domain.usecase.SnoozeTaskAndReminderUseCase
import com.example.taskpulse.domain.usecase.SnoozeTaskUseCase
import com.example.taskpulse.notification.NotificationCooldownStore
import com.example.taskpulse.domain.usecase.TriggerAutomationSweepNowUseCase
import com.example.taskpulse.domain.usecase.UpdateAutomationRuleDefinitionUseCase
import com.example.taskpulse.domain.usecase.UpsertAutomationRuleUseCase
import com.example.taskpulse.domain.usecase.UpdateTasksPriorityUseCase
import com.example.taskpulse.domain.usecase.UpsertTaskUseCase

class AppContainer(context: Context) {
    private val database: TaskPulseDatabase = Room.databaseBuilder(
        context.applicationContext,
        TaskPulseDatabase::class.java,
        "taskpulse.db"
    )
        .fallbackToDestructiveMigration(dropAllTables = true)
        .build()

    private val automationSweepLogRepository = OfflineAutomationSweepLogRepository(database.automationSweepLogDao())
    private val repository = OfflineTaskRepository(database.taskDao())
    private val categoryRepository = OfflineCategoryRepository(database.categoryDao())
    private val automationRepository = OfflineAutomationRuleRepository(database.automationDao())
    val automationSettingsRepository: AutomationSettingsRepository =
        SharedPrefsAutomationSettingsRepository(context.applicationContext)
    private val scheduler: TaskScheduler = WorkManagerTaskScheduler(context.applicationContext)
    val notificationCooldownStore = NotificationCooldownStore(context.applicationContext)

    val observeTasksUseCase = ObserveTasksUseCase(repository)
    val observeAllTasksUseCase = ObserveAllTasksUseCase(repository)
    val observeArchivedTasksUseCase = ObserveArchivedTasksUseCase(repository)
    val ensureDefaultCategoryUseCase = EnsureDefaultCategoryUseCase(categoryRepository)
    val observeAutomationRulesUseCase = ObserveAutomationRulesUseCase(automationRepository)
    val ensureStarterAutomationRulesUseCase = EnsureStarterAutomationRulesUseCase(automationRepository)
    val setAutomationRuleEnabledUseCase = SetAutomationRuleEnabledUseCase(automationRepository)
    val upsertAutomationRuleUseCase = UpsertAutomationRuleUseCase(automationRepository)
    val updateAutomationRuleDefinitionUseCase = UpdateAutomationRuleDefinitionUseCase(automationRepository)
    val deleteAutomationRuleUseCase = DeleteAutomationRuleUseCase(automationRepository)
    val getAutomationRuleUseCase = GetAutomationRuleUseCase(automationRepository)
    val getEnabledAutomationRuleCountUseCase = GetEnabledAutomationRuleCountUseCase(automationRepository)
    val triggerAutomationSweepNowUseCase = TriggerAutomationSweepNowUseCase(context.applicationContext)
    val getAutomationSweepIntervalUseCase = GetAutomationSweepIntervalUseCase(automationSettingsRepository)
    val setAutomationSweepIntervalUseCase = SetAutomationSweepIntervalUseCase(automationSettingsRepository)
    val rescheduleAutomationSweepUseCase =
        RescheduleAutomationSweepUseCase(context.applicationContext, automationSettingsRepository)
    val observeDailyProductivityUseCase = ObserveDailyProductivityUseCase(repository)
    val upsertTaskUseCase = UpsertTaskUseCase(repository)
    val createDefaultTaskUseCase = CreateDefaultTaskUseCase()
    val cancelTaskReminderUseCase = CancelTaskReminderUseCase(scheduler)
    val deleteTasksUseCase = DeleteTasksUseCase(repository, cancelTaskReminderUseCase)
    val updateTasksPriorityUseCase = UpdateTasksPriorityUseCase(repository)
    val scheduleTaskReminderUseCase = ScheduleTaskReminderUseCase(scheduler)
    val scheduleRecurringTaskUseCase = ScheduleRecurringTaskUseCase(scheduler)
    val scheduleDependentRemindersUseCase = ScheduleDependentRemindersUseCase(
        repository,
        scheduleTaskReminderUseCase,
        scheduleRecurringTaskUseCase
    )
    val markTaskCompletedUseCase = MarkTaskCompletedUseCase(repository)
    val markTaskInProgressUseCase = MarkTaskInProgressUseCase(repository)
    val markTaskFailedUseCase = MarkTaskFailedUseCase(repository)
    val evaluateAutomationRulesUseCase = EvaluateAutomationRulesUseCase(SimpleRuleEngine())
    val snoozeTaskUseCase = SnoozeTaskUseCase(repository)
    val getTaskUseCase = GetTaskUseCase(repository)
    val snoozeTaskAndReminderUseCase = SnoozeTaskAndReminderUseCase(
        snoozeTaskUseCase,
        getTaskUseCase,
        cancelTaskReminderUseCase,
        scheduleTaskReminderUseCase,
        notificationCooldownStore
    )
    val completeTaskAndStopRemindersUseCase = CompleteTaskAndStopRemindersUseCase(
        markTaskCompletedUseCase,
        cancelTaskReminderUseCase,
        scheduleDependentRemindersUseCase,
        notificationCooldownStore
    )
    val runEntryLifecycleMaintenanceUseCase = RunEntryLifecycleMaintenanceUseCase(
        repository,
        completeTaskAndStopRemindersUseCase
    )
    val restoreArchivedEntryUseCase = RestoreArchivedEntryUseCase(repository)
    val appendAutomationSweepRunUseCase = AppendAutomationSweepRunUseCase(automationSweepLogRepository)
    val loadAutomationSweepHistoryUseCase = LoadAutomationSweepHistoryUseCase(automationSweepLogRepository)
    val taskSnapshotFileExporter = TaskSnapshotFileExporter(repository, context.applicationContext.filesDir)
    val themeRepository = SharedPreferencesThemeRepository(context.applicationContext)

    suspend fun loadTaskSnapshot(): List<Task> = repository.listAllTasks()

    suspend fun loadAutomationRulesSnapshot(): List<AutomationRule> = automationRepository.listRules()

    fun getAutomationSweepIntervalHours(): Long = getAutomationSweepIntervalUseCase()
}
