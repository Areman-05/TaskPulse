package com.example.taskpulse.ui.settings

import android.content.Intent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.core.content.FileProvider
import com.example.taskpulse.R
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
