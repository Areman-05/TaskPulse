package com.example.taskpulse.ui.settings

import android.content.Intent
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.taskpulse.R
import com.example.taskpulse.domain.model.AutomationSweepRun
import com.example.taskpulse.ui.components.TaskPulseAccentButton
import com.example.taskpulse.ui.components.TaskPulsePrimaryButton
import com.example.taskpulse.ui.components.TaskPulseSecondaryButton
import com.example.taskpulse.ui.components.TaskPulseSectionCard
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val MaintenanceHistoryFormatter =
    DateTimeFormatter.ofPattern("d MMM yyyy, HH:mm").withZone(ZoneId.systemDefault())

private val HistoryRowShape = RoundedCornerShape(10.dp)

@Composable
fun SettingsPendingExportEffect(
    pendingExport: PendingTaskExport?,
    onConsumed: () -> Unit
) {
    val context = LocalContext.current
    androidx.compose.runtime.LaunchedEffect(pendingExport) {
        val payload = pendingExport ?: return@LaunchedEffect
        shareExport(context, payload.absolutePath, payload.mimeType)
        onConsumed()
    }
}

private fun shareExport(context: android.content.Context, path: String, mimeType: String) {
    val file = File(path)
    val uri = FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", file)
    val intent = Intent(Intent.ACTION_SEND).apply {
        type = mimeType
        putExtra(Intent.EXTRA_STREAM, uri)
        addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
    }
    context.startActivity(
        Intent.createChooser(intent, context.getString(R.string.settings_export_share_title))
    )
}

@Composable
fun SettingsMaintenanceSection(
    state: SettingsUiState,
    viewModel: SettingsViewModel
) {
    TaskPulseSectionCard {
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Text(
                text = stringResource(R.string.settings_maintenance_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.settings_maintenance_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TaskPulseAccentButton(
                text = if (state.isMaintenanceRunning) {
                    stringResource(R.string.settings_maintenance_running)
                } else {
                    stringResource(R.string.settings_maintenance_run_now)
                },
                onClick = viewModel::runMaintenanceNow,
                enabled = !state.isMaintenanceRunning
            )
            SettingsToggleRow(
                label = stringResource(R.string.settings_sweep_unmetered_label),
                checked = state.sweepUnmeteredOnly,
                onCheckedChange = viewModel::onSweepUnmeteredChange
            )
            SettingsToggleRow(
                label = stringResource(R.string.settings_sweep_charging_label),
                checked = state.sweepRequiresCharging,
                onCheckedChange = viewModel::onSweepRequiresChargingChange
            )
            OutlinedTextField(
                value = state.sweepIntervalHours,
                onValueChange = viewModel::onSweepIntervalChange,
                label = { Text(stringResource(R.string.settings_sweep_interval_label)) },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                shape = RoundedCornerShape(10.dp)
            )
            TaskPulseSecondaryButton(
                text = stringResource(R.string.settings_save_interval),
                onClick = viewModel::saveSweepInterval
            )
            state.saveIntervalMessage?.let { key ->
                val message = when (key) {
                    "settings_interval_invalid" -> stringResource(R.string.settings_interval_invalid)
                    "settings_interval_saved" -> stringResource(R.string.settings_interval_saved)
                    else -> key
                }
                Text(
                    text = message,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.tertiary
                )
            }
        }
    }
}

@Composable
private fun SettingsToggleRow(
    label: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f).padding(end = 12.dp)
        )
        Switch(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
fun SettingsMaintenanceHistorySection(
    state: SettingsUiState,
    viewModel: SettingsViewModel
) {
    TaskPulseSectionCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.settings_maintenance_history_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.settings_maintenance_history_subtitle),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            if (state.recentSweepRuns.isEmpty()) {
                MaintenanceHistoryRow(
                    dateLabel = stringResource(R.string.settings_maintenance_history_empty),
                    detail = stringResource(R.string.settings_maintenance_history_empty_hint)
                )
            } else {
                state.recentSweepRuns.forEach { run ->
                    MaintenanceHistoryRow(
                        dateLabel = MaintenanceHistoryFormatter.format(
                            Instant.ofEpochMilli(run.ranAtMillis)
                        ),
                        detail = maintenanceHistoryDetail(run)
                    )
                }
            }
            TextButton(onClick = viewModel::refreshSweepHistory) {
                Text(stringResource(R.string.settings_maintenance_history_refresh))
            }
        }
    }
}

@Composable
private fun MaintenanceHistoryRow(
    dateLabel: String,
    detail: String
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = HistoryRowShape,
        color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.28f),
        border = BorderStroke(
            1.dp,
            MaterialTheme.colorScheme.outline.copy(alpha = 0.28f)
        )
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 10.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = dateLabel,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Text(
                text = detail,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun maintenanceHistoryDetail(run: AutomationSweepRun): String {
    return if (run.triggeredMatchCount <= 0) {
        stringResource(R.string.settings_maintenance_history_line_idle)
    } else {
        stringResource(R.string.settings_maintenance_history_line_actions, run.triggeredMatchCount)
    }
}

@Composable
fun SettingsExportSection(viewModel: SettingsViewModel) {
    TaskPulseSectionCard {
        Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(
                text = stringResource(R.string.settings_export_section_title),
                style = MaterialTheme.typography.titleMedium
            )
            Text(
                text = stringResource(R.string.settings_export_hint),
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            TaskPulsePrimaryButton(
                text = stringResource(R.string.settings_export_json),
                onClick = viewModel::exportJsonSnapshot
            )
            TaskPulseSecondaryButton(
                text = stringResource(R.string.settings_export_csv),
                onClick = viewModel::exportCsvSnapshot
            )
            TaskPulseSecondaryButton(
                text = stringResource(R.string.settings_export_backup),
                onClick = viewModel::exportDatabaseBackup
            )
        }
    }
}
