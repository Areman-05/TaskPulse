package com.example.taskpulse.ui.settings

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Archive
import androidx.compose.material.icons.outlined.Backup
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.DarkMode
import androidx.compose.material.icons.outlined.DataObject
import androidx.compose.material.icons.outlined.Menu
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.TableChart
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.taskpulse.R
import com.example.taskpulse.domain.model.AppThemeMode
import com.example.taskpulse.ui.components.StitchToggle
import com.example.taskpulse.ui.theme.StitchThemeColors
import com.example.taskpulse.ui.theme.StitchTypography

private val CardShape = RoundedCornerShape(12.dp)

@Composable
fun SettingsScreen(
    viewModel: SettingsViewModel,
    onOpenArchive: () -> Unit
) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle(initialValue = AppThemeMode.LIGHT)
    val isDarkMode = themeMode == AppThemeMode.DARK

    SettingsPendingExportEffect(
        pendingExport = state.pendingExport,
        onConsumed = viewModel::consumePendingExport
    )

    Box(modifier = Modifier.fillMaxSize()) {
        StitchSettingsBackground(modifier = Modifier.fillMaxSize())

        Scaffold(
            containerColor = Color.Transparent,
            topBar = { StitchSettingsTopBar() }
        ) { innerPadding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(innerPadding)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = 16.dp)
                    .padding(top = 16.dp, bottom = 112.dp)
            ) {
                Text(
                    text = stringResource(R.string.settings_screen_heading),
                    style = StitchTypography.headlineMd.copy(
                        fontSize = 28.sp,
                        lineHeight = 36.sp,
                        fontWeight = FontWeight.W600
                    ),
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = stringResource(R.string.settings_screen_subtitle),
                    style = StitchTypography.bodyMd,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.padding(top = 8.dp, bottom = 24.dp)
                )

                Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    StitchSettingsSection(title = stringResource(R.string.settings_appearance_title)) {
                        StitchSettingsToggleRow(
                            icon = Icons.Outlined.DarkMode,
                            title = stringResource(R.string.settings_dark_mode_title),
                            subtitle = stringResource(R.string.settings_dark_mode_subtitle),
                            checked = isDarkMode,
                            onCheckedChange = viewModel::setDarkModeEnabled
                        )
                    }

                    StitchSettingsSection(title = stringResource(R.string.settings_data_section)) {
                        StitchSettingsNavRow(
                            icon = Icons.Outlined.Archive,
                            title = stringResource(R.string.settings_archive_row_title),
                            subtitle = stringResource(R.string.settings_archive_row_subtitle),
                            onClick = onOpenArchive,
                            showDivider = false
                        )
                    }

                    StitchSettingsSection(title = stringResource(R.string.settings_export_section_title)) {
                        StitchSettingsActionRow(
                            icon = Icons.Outlined.DataObject,
                            title = stringResource(R.string.settings_export_json),
                            onClick = viewModel::exportJsonSnapshot,
                            iconTint = MaterialTheme.colorScheme.primary
                        )
                        StitchSettingsDivider()
                        StitchSettingsActionRow(
                            icon = Icons.Outlined.TableChart,
                            title = stringResource(R.string.settings_export_csv),
                            onClick = viewModel::exportCsvSnapshot,
                            iconTint = MaterialTheme.colorScheme.primary
                        )
                        StitchSettingsDivider()
                        StitchSettingsActionRow(
                            icon = Icons.Outlined.Backup,
                            title = stringResource(R.string.settings_export_backup),
                            onClick = viewModel::exportDatabaseBackup,
                            iconTint = MaterialTheme.colorScheme.primary,
                            showDivider = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun StitchSettingsBackground(modifier: Modifier = Modifier) {
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
private fun StitchSettingsTopBar() {
    Surface(
        color = MaterialTheme.colorScheme.surface,
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
                    IconButton(onClick = { }, modifier = Modifier.size(40.dp)) {
                        Icon(
                            imageVector = Icons.Outlined.Menu,
                            contentDescription = stringResource(R.string.home_menu_cd),
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
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
private fun StitchSettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    val cardColor = MaterialTheme.colorScheme.surfaceVariant
    val borderColor = StitchThemeColors.cardBorder()

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = CardShape,
        color = cardColor,
        border = BorderStroke(1.dp, borderColor),
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column {
            Text(
                text = title.uppercase(),
                style = StitchTypography.labelLg,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp)
            )
            HorizontalDivider(color = borderColor, thickness = 1.dp)
            content()
        }
    }
}

@Composable
private fun StitchSettingsToggleRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(
                if (pressed) StitchThemeColors.rowHighlight()
                else Color.Transparent
            )
            .clickable { onCheckedChange(!checked) }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.weight(1f)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(24.dp)
            )
            Column {
                Text(
                    text = title,
                    style = StitchTypography.bodyLg,
                    color = MaterialTheme.colorScheme.onSurface
                )
                Text(
                    text = subtitle,
                    style = StitchTypography.bodyMd,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        StitchToggle(checked = checked, onCheckedChange = onCheckedChange)
    }
}

@Composable
private fun StitchSettingsNavRow(
    icon: ImageVector,
    title: String,
    subtitle: String,
    onClick: () -> Unit,
    showDivider: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (pressed) StitchThemeColors.rowHighlight()
                    else Color.Transparent
                )
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(16.dp),
                modifier = Modifier.weight(1f)
            ) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
                Column {
                    Text(
                        text = title,
                        style = StitchTypography.bodyLg,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                    Text(
                        text = subtitle,
                        style = StitchTypography.bodyMd,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            Icon(
                imageVector = Icons.Outlined.ChevronRight,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.size(20.dp)
            )
        }
        if (showDivider) StitchSettingsDivider()
    }
}

@Composable
private fun StitchSettingsActionRow(
    icon: ImageVector,
    title: String,
    onClick: () -> Unit,
    iconTint: Color,
    showDivider: Boolean = true
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()

    Column {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    if (pressed) StitchThemeColors.rowHighlight()
                    else Color.Transparent
                )
                .clickable(interactionSource = interactionSource, indication = null, onClick = onClick)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = iconTint,
                modifier = Modifier.size(24.dp)
            )
            Text(
                text = title,
                style = StitchTypography.bodyLg,
                color = MaterialTheme.colorScheme.onSurface
            )
        }
        if (showDivider) StitchSettingsDivider()
    }
}

@Composable
private fun StitchSettingsDivider() {
    HorizontalDivider(color = StitchThemeColors.cardBorder(), thickness = 1.dp)
}
