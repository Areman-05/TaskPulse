package com.example.taskpulse.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val DayLabelFormatter =
    DateTimeFormatter.ofPattern("dd/MM").withZone(ZoneId.systemDefault())

@Composable
fun HomeScreen(
    viewModel: HomeViewModel
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        topBar = {
            Text(
                text = "TaskPulse",
                style = MaterialTheme.typography.headlineSmall,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Button(
                onClick = viewModel::createQuickTask,
                enabled = !state.isCreating,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (state.isCreating) "Creando..." else "Crear tarea rápida")
            }

            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Pendientes: ${state.pendingCount}")
                    Text("Completadas: ${state.completedCount}")
                }
            }

            if (state.productivityWeek.isNotEmpty()) {
                Card(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        Text(
                            text = "Completadas (últimos 7 días)",
                            style = MaterialTheme.typography.titleSmall
                        )
                        state.productivityWeek.forEach { point ->
                            val label = DayLabelFormatter.format(Instant.ofEpochMilli(point.dayStartMillis))
                            Text(
                                text = "$label • ${point.completedCount} completadas",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                    }
                }
            }

            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("Filtros", style = MaterialTheme.typography.titleSmall)
                androidx.compose.foundation.layout.Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    TextButton(onClick = { viewModel.selectFilter(HomeTaskFilter.ALL) }) { Text("Todas") }
                    TextButton(onClick = { viewModel.selectFilter(HomeTaskFilter.PENDING) }) { Text("Pendientes") }
                    TextButton(onClick = { viewModel.selectFilter(HomeTaskFilter.COMPLETED) }) { Text("Completadas") }
                }
            }

            LazyColumn(
                contentPadding = PaddingValues(bottom = 24.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(state.filteredTasks, key = { it.id }) { task ->
                    Card(modifier = Modifier.fillMaxWidth()) {
                        Column(modifier = Modifier.padding(12.dp)) {
                            Text(text = task.title, style = MaterialTheme.typography.titleMedium)
                            Text(text = task.status.name, style = MaterialTheme.typography.bodyMedium)
                            if (task.status != com.example.taskpulse.domain.model.TaskStatus.COMPLETED) {
                                TextButton(onClick = { viewModel.markCompleted(task.id) }) {
                                    Text("Marcar completada")
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}
