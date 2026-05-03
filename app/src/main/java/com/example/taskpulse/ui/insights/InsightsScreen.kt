package com.example.taskpulse.ui.insights

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
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
                text = "Insights",
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
                    state.automationRules.forEach { rule ->
                        val statusLabel = if (rule.enabled) "ON" else "OFF"
                        Text(
                            "- ${rule.name} [$statusLabel]",
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
