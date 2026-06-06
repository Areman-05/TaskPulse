package com.example.taskpulse.ui.settings

import android.content.Intent
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.FileProvider
import com.example.taskpulse.R
import com.example.taskpulse.ui.components.TaskPulsePrimaryButton
import com.example.taskpulse.ui.components.TaskPulseSecondaryButton
import com.example.taskpulse.ui.components.TaskPulseSectionCard
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import java.io.File

@Composable
fun SettingsPendingExportEffect(
    pendingExport: PendingTaskExport?,
    onConsumed: () -> Unit
) {
    val context = LocalContext.current
    LaunchedEffect(pendingExport) {
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
