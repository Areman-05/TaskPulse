package com.example.taskpulse.ui.insights

import android.content.Intent
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.taskpulse.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.domain.model.AutomationAction
import com.example.taskpulse.domain.model.AutomationTrigger
import java.io.File
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val InsightDayFormatter =
    DateTimeFormatter.ofPattern("EEE dd/MM HH:mm").withZone(ZoneId.systemDefault())

@Composable
fun InsightsScreen(viewModel: InsightsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val context = LocalContext.current

    LaunchedEffect(state.pendingExport) {
        val payload = state.pendingExport ?: return@LaunchedEffect
        shareExport(context, payload.absolutePath, payload.mimeType)
        viewModel.consumePendingExport()
    }

    if (state.rulePendingDeleteId != null) {
        AlertDialog(
            onDismissRequest = viewModel::cancelPendingDeleteRule,
            title = { Text(stringResource(R.string.insights_delete_dialog_title)) },
            text = { Text(stringResource(R.string.insights_delete_dialog_body)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmPendingDeleteRule) {
                    Text(stringResource(R.string.insights_delete_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::cancelPendingDeleteRule) {
                    Text(stringResource(R.string.insights_delete_cancel))
                }
            }
        )
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Text(
                text = stringResource(R.string.insights_screen_title),
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(horizontal = 16.dp)
                .fillMaxSize()
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(stringResource(R.string.insights_automation_active_title), style = MaterialTheme.typography.titleMedium)
                    Text(stringResource(R.string.insights_automation_rule_count, state.enabledAutomationCount, state.automationRules.size))
                    Text(
                        text = stringResource(R.string.insights_automation_sweep_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Button(
                        onClick = viewModel::runSweepNow,
                        enabled = !state.isSweepRunning
                    ) {
                        Text(
                            if (state.isSweepRunning) {
                                stringResource(R.string.insights_run_sweep_running)
                            } else {
                                stringResource(R.string.insights_run_sweep_now)
                            }
                        )
                    }

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.insights_sweep_unmetered_label))
                        Switch(
                            checked = state.sweepUnmeteredOnly,
                            onCheckedChange = viewModel::onSweepUnmeteredChange
                        )
                    }
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(stringResource(R.string.insights_sweep_charging_label))
                        Switch(
                            checked = state.sweepRequiresCharging,
                            onCheckedChange = viewModel::onSweepRequiresChargingChange
                        )
                    }

                    OutlinedTextField(
                        value = state.sweepIntervalHours,
                        onValueChange = viewModel::onSweepIntervalChange,
                        label = { Text(stringResource(R.string.insights_sweep_interval_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    Button(onClick = viewModel::saveSweepInterval) {
                        Text(stringResource(R.string.insights_save_interval))
                    }
                    state.saveIntervalMessage?.let { key ->
                        val message = when (key) {
                            "insights_interval_invalid" ->
                                stringResource(R.string.insights_interval_invalid)
                            "insights_interval_saved" ->
                                stringResource(R.string.insights_interval_saved)
                            else -> key
                        }
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.primary
                        )
                    }
                }
            }

            if (state.recentSweepRuns.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(
                        modifier = Modifier.padding(12.dp),
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        Text(stringResource(R.string.insights_sweep_history_title), style = MaterialTheme.typography.titleMedium)
                        state.recentSweepRuns.forEach { run ->
                            val label = InsightDayFormatter.format(Instant.ofEpochMilli(run.ranAtMillis))
                            Text(
                                stringResource(R.string.insights_sweep_history_line, label, run.triggeredMatchCount),
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        TextButton(onClick = viewModel::refreshSweepHistory) {
                            Text(stringResource(R.string.insights_sweep_history_refresh))
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text(stringResource(R.string.insights_recent_completions_title), style = MaterialTheme.typography.titleMedium)
                    if (state.productivityTrend.isEmpty()) {
                        Text(stringResource(R.string.insights_no_productivity_yet))
                    } else {
                        ProductivityBarChart(points = state.productivityTrend)
                        state.productivityTrend.forEach { point ->
                            val label =
                                InsightDayFormatter.format(Instant.ofEpochMilli(point.dayStartMillis))
                            Text("$label • ${point.completedCount}")
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.insights_export_section_title), style = MaterialTheme.typography.titleMedium)
                    Button(onClick = viewModel::exportJsonSnapshot) {
                        Text(stringResource(R.string.insights_export_json))
                    }
                    Button(onClick = viewModel::exportCsvSnapshot) {
                        Text(stringResource(R.string.insights_export_csv))
                    }
                    Button(onClick = viewModel::exportDatabaseBackup) {
                        Text(stringResource(R.string.insights_export_backup))
                    }
                    Text(
                        text = stringResource(R.string.insights_export_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text(stringResource(R.string.insights_rules_section_title), style = MaterialTheme.typography.titleMedium)
                    OutlinedTextField(
                        value = state.draftRuleName,
                        onValueChange = viewModel::onDraftNameChange,
                        label = { Text(stringResource(R.string.insights_rule_name_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )
                    OutlinedTextField(
                        value = state.draftThresholdDays,
                        onValueChange = viewModel::onDraftThresholdChange,
                        label = { Text(stringResource(R.string.insights_rule_threshold_label)) },
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true
                    )

                    EnumDropdown(
                        label = stringResource(R.string.insights_trigger_pick_label),
                        selected = state.draftTrigger,
                        options = AutomationTrigger.entries,
                        optionLabel = { it.name },
                        onSelected = viewModel::onDraftTriggerChange
                    )
                    EnumDropdown(
                        label = stringResource(R.string.insights_action_pick_label),
                        selected = state.draftAction,
                        options = AutomationAction.entries,
                        optionLabel = { it.name },
                        onSelected = viewModel::onDraftActionChange
                    )

                    Button(onClick = viewModel::saveDraftRule) {
                        Text(stringResource(R.string.insights_save_rule))
                    }
                    TextButton(onClick = viewModel::clearDraft) {
                        Text(stringResource(R.string.insights_clear_rule_draft))
                    }
                    state.draftValidationError?.let { key ->
                        val message = when (key) {
                            "insights_error_name_required" ->
                                stringResource(R.string.insights_error_name_required)
                            "insights_error_stale_needs_threshold" ->
                                stringResource(R.string.insights_error_stale_needs_threshold)
                            else -> key
                        }
                        Text(
                            text = message,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.error
                        )
                    }

                    state.automationRules.forEach { rule ->
                        val statusLabel = if (rule.enabled) "ON" else "OFF"
                        Column {
                            Text(
                                "- ${rule.name} [$statusLabel]",
                                style = MaterialTheme.typography.bodyMedium
                            )
                            TextButton(onClick = { viewModel.toggleRule(rule.id, rule.enabled) }) {
                                Text(
                                    if (rule.enabled) {
                                        stringResource(R.string.insights_disable_rule)
                                    } else {
                                        stringResource(R.string.insights_enable_rule)
                                    }
                                )
                            }
                            TextButton(onClick = { viewModel.beginEdit(rule) }) {
                                Text(stringResource(R.string.insights_edit_rule))
                            }
                            TextButton(onClick = { viewModel.requestDeleteRule(rule.id) }) {
                                Text(stringResource(R.string.insights_delete_rule))
                            }
                        }
                    }
                }
            }
        }
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
    context.startActivity(Intent.createChooser(intent, context.getString(R.string.insights_share_chooser_title)))
}

@Composable
private fun <T : Enum<T>> EnumDropdown(
    label: String,
    selected: T,
    options: List<T>,
    optionLabel: (T) -> String,
    onSelected: (T) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    Box(modifier = Modifier.fillMaxWidth()) {
        OutlinedTextField(
            modifier = Modifier.fillMaxWidth(),
            readOnly = true,
            value = optionLabel(selected),
            onValueChange = {},
            label = { Text(label) },
            trailingIcon = {
                IconButton(onClick = { expanded = true }) {
                    Icon(Icons.Default.ArrowDropDown, contentDescription = null)
                }
            }
        )
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clickable { expanded = true }
        )
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(optionLabel(option)) },
                    onClick = {
                        onSelected(option)
                        expanded = false
                    }
                )
            }
        }
    }
}
