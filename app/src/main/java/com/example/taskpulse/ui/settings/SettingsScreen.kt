package com.example.taskpulse.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.example.taskpulse.R
import com.example.taskpulse.ui.components.TaskPulsePrimaryButton
import com.example.taskpulse.ui.components.TaskPulseScrollableColumn
import com.example.taskpulse.ui.components.TaskPulseSecondaryButton
import com.example.taskpulse.ui.components.TaskPulseSectionCard

private val CardShape = RoundedCornerShape(8.dp)

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            Column(
                modifier = Modifier.padding(horizontal = 20.dp, vertical = 16.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_screen_title).uppercase(),
                    style = MaterialTheme.typography.labelLarge,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = stringResource(R.string.settings_screen_heading),
                    style = MaterialTheme.typography.headlineSmall
                )
            }
        }
    ) { innerPadding ->
        TaskPulseScrollableColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(start = 20.dp, end = 8.dp),
            showAmbientGrid = false,
            contentPaddingBottom = 32.dp
        ) {
            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TaskPulseSectionCard {
                    Text(
                        text = stringResource(R.string.settings_appearance_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.settings_appearance_subtitle),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 4.dp, bottom = 12.dp)
                    )
                    TaskPulsePrimaryButton(
                        text = stringResource(R.string.settings_theme_light),
                        onClick = viewModel::setLightMode
                    )
                    TaskPulseSecondaryButton(
                        text = stringResource(R.string.settings_theme_dark),
                        onClick = viewModel::setDarkMode
                    )
                }

                TaskPulseSectionCard {
                    Text(
                        text = stringResource(R.string.settings_more_title),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.settings_more_subtitle),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 8.dp)
                    )
                }

                Card(
                    modifier = Modifier.fillMaxWidth(),
                    shape = CardShape,
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                    ),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.outline.copy(alpha = 0.3f))
                ) {
                    Text(
                        text = stringResource(R.string.settings_insights_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(14.dp)
                    )
                }
            }
        }
    }
}
