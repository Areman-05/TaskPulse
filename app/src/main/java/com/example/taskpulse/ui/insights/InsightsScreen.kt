package com.example.taskpulse.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taskpulse.R
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val InsightDayFormatter =
    DateTimeFormatter.ofPattern("EEE dd/MM").withZone(ZoneId.systemDefault())

@Composable
fun InsightsScreen(viewModel: InsightsViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

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
                .fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Automatizaciones activas", style = MaterialTheme.typography.titleMedium)
                    Text("${state.enabledAutomationCount} habilitadas de ${state.automationRules.size}")
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
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Text("Completadas recientes", style = MaterialTheme.typography.titleMedium)
                    if (state.productivityTrend.isEmpty()) {
                        Text("Sin datos suficientes aún.")
                    } else {
                        state.productivityTrend.forEach { point ->
                            val label =
                                InsightDayFormatter.format(Instant.ofEpochMilli(point.dayStartMillis))
                            Text("$label • ${point.completedCount} completadas")
                        }
                    }
                }
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Text("Reglas config.", style = MaterialTheme.typography.titleMedium)
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
                    Button(onClick = viewModel::saveDraftRule) {
                        Text(stringResource(R.string.insights_save_rule))
                    }
                    TextButton(onClick = viewModel::clearDraft) {
                        Text(stringResource(R.string.insights_clear_rule_draft))
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
                            TextButton(onClick = { viewModel.deleteRule(rule.id) }) {
                                Text(stringResource(R.string.insights_delete_rule))
                            }
                        }
                    }
                }
            }
        }
    }
}
