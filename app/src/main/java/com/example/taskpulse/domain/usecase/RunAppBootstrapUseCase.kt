package com.example.taskpulse.domain.usecase

import android.content.Context
import com.example.taskpulse.worker.AutomationInitialWork
import com.example.taskpulse.worker.AutomationWorkScheduler

enum class SplashBootstrapPhase {
    Database,
    Data,
    Services,
    Ready
}

/**
 * Arranque en frío: categorías, reglas, mantenimiento y workers.
 * Se ejecuta durante la splash para mostrar progreso real.
 */
class RunAppBootstrapUseCase(
    private val appContext: Context,
    private val ensureDefaultCategoryUseCase: EnsureDefaultCategoryUseCase,
    private val ensureStarterAutomationRulesUseCase: EnsureStarterAutomationRulesUseCase,
    private val runEntryLifecycleMaintenanceUseCase: RunEntryLifecycleMaintenanceUseCase,
    private val getAutomationSweepIntervalUseCase: GetAutomationSweepIntervalUseCase,
    private val automationSettingsRepository: com.example.taskpulse.domain.repository.AutomationSettingsRepository
) {
    suspend operator fun invoke(
        onProgress: suspend (SplashBootstrapPhase, Float) -> Unit
    ) {
        onProgress(SplashBootstrapPhase.Database, 0.08f)
        ensureDefaultCategoryUseCase()

        onProgress(SplashBootstrapPhase.Data, 0.42f)
        ensureStarterAutomationRulesUseCase()
        runEntryLifecycleMaintenanceUseCase()

        onProgress(SplashBootstrapPhase.Services, 0.78f)
        AutomationWorkScheduler.enqueue(
            context = appContext,
            repeatIntervalHours = getAutomationSweepIntervalUseCase(),
            settings = automationSettingsRepository
        )
        AutomationInitialWork.enqueueOnce(appContext)

        onProgress(SplashBootstrapPhase.Ready, 1f)
    }
}
