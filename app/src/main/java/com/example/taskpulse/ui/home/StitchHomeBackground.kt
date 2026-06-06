package com.example.taskpulse.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import com.example.taskpulse.ui.theme.StitchThemeColors

/** Fondo Stitch: gradiente nebula según tema claro/oscuro. */
@Composable
fun StitchHomeBackground(modifier: Modifier = Modifier) {
    val start = StitchThemeColors.pageBackground()
    val end = StitchThemeColors.homeGradientEnd()

    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(start, end),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
        )
    }
}
