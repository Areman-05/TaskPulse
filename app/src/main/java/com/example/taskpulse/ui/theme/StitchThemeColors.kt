package com.example.taskpulse.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance

/** Colores Stitch derivados del [MaterialTheme] activo (claro u oscuro). */
object StitchThemeColors {
    @Composable
    fun isDark(): Boolean = MaterialTheme.colorScheme.background.luminance() < 0.5f

    @Composable
    fun pageBackground(): Color = MaterialTheme.colorScheme.background

    @Composable
    fun topBarSurface(): Color = MaterialTheme.colorScheme.surface

    @Composable
    fun cardBackground(): Color = MaterialTheme.colorScheme.surfaceVariant

    @Composable
    fun cardBorder(): Color = MaterialTheme.colorScheme.outlineVariant

    @Composable
    fun elevatedCardBackground(): Color = MaterialTheme.colorScheme.surfaceContainerLowest

    @Composable
    fun glassSurface(): Color =
        if (isDark()) Color(0xCC303134) else TaskPulseColors.GlassSurface

    @Composable
    fun searchBarBackground(): Color = MaterialTheme.colorScheme.surfaceVariant

    @Composable
    fun rowHighlight(): Color = MaterialTheme.colorScheme.surfaceContainerHigh

    @Composable
    fun homeGradientEnd(): Color =
        if (isDark()) Color(0x6682511C) else Color(0x4DEDD2D2)

    @Composable
    fun calendarGradientBronze(): Color =
        TaskPulseColors.Bronze.copy(alpha = if (isDark()) 0.08f else 0.05f)

    @Composable
    fun calendarGradientGray(): Color =
        TaskPulseColors.Gray300.copy(alpha = if (isDark()) 0.15f else 0.2f)

    @Composable
    fun mutedAdjacentDay(): Color =
        MaterialTheme.colorScheme.outline.copy(alpha = if (isDark()) 0.7f else 0.55f)

    @Composable
    fun secondaryContainerMuted(): Color =
        MaterialTheme.colorScheme.secondaryContainer.copy(
            alpha = if (isDark()) 0.6f else 0.5f
        )
}
