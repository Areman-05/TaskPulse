package com.example.taskpulse.ui.settings

import com.example.taskpulse.domain.model.AutomationSweepRun

data class PendingTaskExport(
    val absolutePath: String,
    val mimeType: String
)

enum class SettingsIntervalFeedback {
    Invalid,
    Saved
}

data class SettingsUiState(
    val isMaintenanceRunning: Boolean = false,
    val sweepIntervalHours: String = "1",
    val intervalFeedback: SettingsIntervalFeedback? = null,
    val sweepUnmeteredOnly: Boolean = false,
    val sweepRequiresCharging: Boolean = false,
    val recentSweepRuns: List<AutomationSweepRun> = emptyList(),
    val pendingExport: PendingTaskExport? = null
)
