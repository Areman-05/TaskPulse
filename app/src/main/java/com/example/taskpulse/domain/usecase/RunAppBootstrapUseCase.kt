package com.example.taskpulse.domain.usecase

import android.content.Context
import com.example.taskpulse.data.export.TaskSnapshotFileExporter
import com.example.taskpulse.domain.repository.AutomationRuleRepository
import com.example.taskpulse.domain.repository.TaskRepository
import com.example.taskpulse.worker.AutomationInitialWork
import com.example.taskpulse.worker.AutomationWorkScheduler

enum class SplashBootstrapPhase {
    Database,
    Data,
    Files,
    Services,
    Ready
}

/**
 * Arranque en frío: categorías, datos locales, mantenimiento, exportaciones y workers.
 * Se ejecuta durante la splash para mostrar progreso real.
 */
class RunAppBootstrapUseCase(
    private val appContext: Context,
    private val ensureDefaultCategoryUseCase: EnsureDefaultCategoryUseCase,
    private val ensureStarterAutomationRulesUseCase: EnsureStarterAutomationRulesUseCase,
    private val runEntryLifecycleMaintenanceUseCase: RunEntryLifecycleMaintenanceUseCase,
    private val getAutomationSweepIntervalUseCase: GetAutomationSweepIntervalUseCase,
    private val automationSettingsRepository: com.example.taskpulse.domain.repository.AutomationSettingsRepository,
    private val taskRepository: TaskRepository,
    private val automationRuleRepository: AutomationRuleRepository,
    private val taskSnapshotFileExporter: TaskSnapshotFileExporter
) {
    suspend operator fun invoke(
        onProgress: suspend (SplashBootstrapPhase, Float) -> Unit
    ) {
        onProgress(SplashBootstrapPhase.Database, 0.06f)
        ensureDefaultCategoryUseCase()

        onProgress(SplashBootstrapPhase.Data, 0.18f)
        taskRepository.listTasks()
        taskRepository.countPendingTasks()

        onProgress(SplashBootstrapPhase.Data, 0.34f)
        taskRepository.listAllTasks()
        taskRepository.countArchived()

        onProgress(SplashBootstrapPhase.Data, 0.48f)
        ensureStarterAutomationRulesUseCase()
        automationRuleRepository.listRules()
        automationRuleRepository.countEnabledRules()

        onProgress(SplashBootstrapPhase.Data, 0.62f)
        runEntryLifecycleMaintenanceUseCase()

        onProgress(SplashBootstrapPhase.Files, 0.78f)
        taskSnapshotFileExporter.warmUp()

        onProgress(SplashBootstrapPhase.Services, 0.90f)
        AutomationWorkScheduler.enqueue(
            context = appContext,
            repeatIntervalHours = getAutomationSweepIntervalUseCase(),
            settings = automationSettingsRepository
        )
        AutomationInitialWork.enqueueOnce(appContext)

        onProgress(SplashBootstrapPhase.Ready, 1f)
    }
}
