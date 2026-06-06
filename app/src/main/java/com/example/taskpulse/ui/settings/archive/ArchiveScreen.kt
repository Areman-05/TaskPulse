@file:OptIn(ExperimentalMaterial3Api::class)

package com.example.taskpulse.ui.settings.archive

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Description
import androidx.compose.material.icons.filled.Unarchive
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.R
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.isNote
import com.example.taskpulse.ui.theme.StitchThemeColors
import com.example.taskpulse.ui.theme.StitchTypography
import java.time.Duration
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter
import java.util.Locale

private val CardShape = RoundedCornerShape(12.dp)
private val CardShadowColor = Color(0x0D000000)

@Composable
fun ArchiveScreen(
    viewModel: ArchiveViewModel,
    onBack: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    if (state.confirmDeleteId != null) {
        AlertDialog(
            onDismissRequest = viewModel::dismissDeleteConfirm,
            title = { Text(stringResource(R.string.archive_delete_confirm_title)) },
            text = { Text(stringResource(R.string.archive_delete_confirm_body)) },
            confirmButton = {
                TextButton(onClick = viewModel::confirmDelete) {
                    Text(stringResource(R.string.archive_delete_confirm_action))
                }
            },
            dismissButton = {
                TextButton(onClick = viewModel::dismissDeleteConfirm) {
                    Text(stringResource(R.string.detail_cancel_edit))
                }
            }
        )
    }

    Box(modifier = Modifier.fillMaxSize()) {
        StitchArchiveBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            topBar = { StitchArchiveTopBar(onBack = onBack) }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 32.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp)
            ) {
                Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    Text(
                        text = stringResource(R.string.archive_screen_title),
                        style = StitchTypography.headlineMd.copy(
                            fontSize = 28.sp,
                            lineHeight = 36.sp,
                            fontWeight = FontWeight.W600
                        ),
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = stringResource(R.string.archive_screen_subtitle),
                        style = StitchTypography.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                if (state.entries.isEmpty()) {
                    Text(
                        text = stringResource(R.string.archive_empty),
                        style = StitchTypography.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.padding(top = 16.dp)
                    )
                } else {
                    Text(
                        text = stringResource(R.string.archive_limit_hint),
                        style = StitchTypography.labelLg,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                        state.entries.forEach { entry ->
                            if (entry.isNote) {
                                ArchiveSwipeToDeleteCard(
                                    entry = entry,
                                    onRequestDelete = { viewModel.requestDelete(entry.id) }
                                )
                            } else {
                                ArchiveSwipeToRestoreCard(
                                    entry = entry,
                                    onRestore = { viewModel.restore(entry.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun StitchArchiveBackground(modifier: Modifier = Modifier) {
    val pageBg = StitchThemeColors.pageBackground()
    val bronzeGlow = StitchThemeColors.calendarGradientBronze()
    val grayGlow = StitchThemeColors.calendarGradientGray()
    Canvas(modifier = modifier) {
        drawRect(color = pageBg)
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(bronzeGlow, Color.Transparent),
                center = Offset(0f, 0f),
                radius = size.width * 0.8f
            ),
            center = Offset(0f, 0f),
            radius = size.width * 0.8f
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(grayGlow, Color.Transparent),
                center = Offset(size.width, size.height),
                radius = size.width * 0.7f
            ),
            center = Offset(size.width, size.height),
            radius = size.width * 0.7f
        )
    }
}

@Composable
private fun StitchArchiveTopBar(onBack: () -> Unit) {
    Surface(
        color = StitchThemeColors.topBarSurface(),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    IconButton(onClick = onBack, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = stringResource(R.string.archive_back_cd),
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                    }
                    Text(
                        text = stringResource(R.string.app_name),
                        style = StitchTypography.headlineMd,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
                IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = stringResource(R.string.home_search_cd),
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant, thickness = 1.dp)
        }
    }
}

@Composable
private fun ArchiveSwipeToDeleteCard(
    entry: Task,
    onRequestDelete: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.EndToStart) {
                onRequestDelete()
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.45f }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.errorContainer)
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = false,
            enableDismissFromEndToStart = true,
            backgroundContent = { ArchiveDeleteSwipeBackground() },
            content = { ArchiveEntryCardContent(entry = entry) }
        )
    }
}

@Composable
private fun ArchiveSwipeToRestoreCard(
    entry: Task,
    onRestore: () -> Unit
) {
    val dismissState = rememberSwipeToDismissBoxState(
        confirmValueChange = { value ->
            if (value == SwipeToDismissBoxValue.StartToEnd) {
                onRestore()
                true
            } else {
                false
            }
        },
        positionalThreshold = { totalDistance -> totalDistance * 0.45f }
    )

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(CardShape)
            .background(MaterialTheme.colorScheme.primaryContainer)
    ) {
        SwipeToDismissBox(
            state = dismissState,
            enableDismissFromStartToEnd = true,
            enableDismissFromEndToStart = false,
            backgroundContent = { ArchiveRestoreSwipeBackground() },
            content = { ArchiveEntryCardContent(entry = entry) }
        )
    }
}

@Composable
private fun ArchiveDeleteSwipeBackground() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.error),
        contentAlignment = Alignment.CenterEnd
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Delete,
                contentDescription = stringResource(R.string.archive_swipe_delete_cd),
                tint = MaterialTheme.colorScheme.onError,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.archive_delete),
                style = StitchTypography.labelLg,
                color = MaterialTheme.colorScheme.onError
            )
        }
    }
}

@Composable
private fun ArchiveRestoreSwipeBackground() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primary),
        contentAlignment = Alignment.CenterStart
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.padding(horizontal = 24.dp)
        ) {
            Icon(
                imageVector = Icons.Filled.Unarchive,
                contentDescription = stringResource(R.string.archive_swipe_restore_cd),
                tint = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = stringResource(R.string.archive_restore),
                style = StitchTypography.labelLg,
                color = MaterialTheme.colorScheme.onPrimary
            )
        }
    }
}

@Composable
private fun ArchiveEntryCardContent(entry: Task) {
    val borderColor = StitchThemeColors.cardBorder()
    val archivedAt = entry.archivedAtMillis ?: entry.updatedAtMillis

    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .shadow(1.dp, CardShape, ambientColor = CardShadowColor, spotColor = CardShadowColor),
        shape = CardShape,
        color = StitchThemeColors.elevatedCardBackground(),
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Top
            ) {
                ArchiveEntryTypeBadge(entry = entry)
                Text(
                    text = formatArchivedRelativeTime(archivedAt),
                    style = StitchTypography.labelLg,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Text(
                text = entryTitle(entry),
                style = StitchTypography.headlineSm,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            if (entry.isNote) {
                val body = entryBody(entry)
                if (body.isNotBlank()) {
                    Text(
                        text = body,
                        style = StitchTypography.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        maxLines = 3,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            } else {
                ArchiveTaskLines(entry = entry)
            }
        }
    }
}

@Composable
private fun ArchiveEntryTypeBadge(entry: Task) {
    val isNote = entry.isNote
    val icon = if (isNote) Icons.Filled.Description else Icons.Filled.CheckCircle
    val label = if (isNote) {
        stringResource(R.string.archive_entry_note_type)
    } else {
        stringResource(R.string.archive_entry_task_type)
    }
    val tint = if (isNote) {
        MaterialTheme.colorScheme.secondary
    } else {
        MaterialTheme.colorScheme.primary
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = tint,
            modifier = Modifier.size(18.dp)
        )
        Text(
            text = label.uppercase(Locale.getDefault()),
            style = StitchTypography.labelLg.copy(letterSpacing = 1.sp),
            color = tint
        )
    }
}

@Composable
private fun ArchiveTaskLines(entry: Task) {
    val lines = taskDescriptionLines(entry)
    if (lines.isEmpty()) return

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        lines.forEach { line ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.outlineVariant)
                )
                Text(
                    text = line,
                    style = StitchTypography.bodyMd,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    textDecoration = TextDecoration.LineThrough,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
        }
    }
}

private fun entryTitle(entry: Task): String {
    if (!entry.isNote) {
        return entry.title.ifBlank {
            entry.description.lineSequence().firstOrNull()?.trim().orEmpty()
        }.ifBlank { entry.description.trim() }
    }
    return entry.title.ifBlank {
        entry.description.lineSequence().firstOrNull()?.trim().orEmpty()
    }.ifBlank { entry.description.trim() }
}

private fun entryBody(entry: Task): String {
    if (entry.title.isBlank()) {
        return entry.description.trim()
    }
    return entry.description.trim()
}

private fun taskDescriptionLines(entry: Task): List<String> {
    val fromDescription = entry.description
        .lineSequence()
        .map { it.trim() }
        .filter { it.isNotEmpty() }
        .take(4)
        .toList()
    if (fromDescription.isNotEmpty()) return fromDescription
    if (entry.title.isNotBlank()) return listOf(entry.title)
    return emptyList()
}

@Composable
private fun formatArchivedRelativeTime(millis: Long): String {
    val days = Duration.between(Instant.ofEpochMilli(millis), Instant.now()).toDays()
    return when {
        days <= 0L -> stringResource(R.string.archive_relative_today)
        days == 1L -> stringResource(R.string.archive_relative_yesterday)
        days < 7L -> stringResource(R.string.archive_relative_days, days.toInt())
        days < 14L -> stringResource(R.string.archive_relative_last_week)
        days < 30L -> stringResource(R.string.archive_relative_weeks, (days / 7).toInt())
        days < 60L -> stringResource(R.string.archive_relative_one_month)
        else -> DateTimeFormatter.ofPattern("d MMM yyyy", Locale("es", "ES"))
            .withZone(ZoneId.systemDefault())
            .format(Instant.ofEpochMilli(millis))
    }
}
