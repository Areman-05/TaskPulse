package com.example.taskpulse.ui.settings

data class PendingTaskExport(
    val absolutePath: String,
    val mimeType: String
)

data class SettingsUiState(
    val pendingExport: PendingTaskExport? = null
)
