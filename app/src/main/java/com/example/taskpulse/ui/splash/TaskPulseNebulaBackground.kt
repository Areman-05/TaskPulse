package com.example.taskpulse.ui.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import com.example.taskpulse.ui.theme.StitchThemeColors
import com.example.taskpulse.ui.theme.TaskPulseColors

/** Fondo nebula Stitch con foco central para el hero de splash. */
@Composable
fun TaskPulseNebulaBackground(modifier: Modifier = Modifier) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val infinite = rememberInfiniteTransition(label = "nebula")
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 20_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )
    val shimmer by infinite.animateFloat(
        initialValue = 0.85f,
        targetValue = 1.15f,
        animationSpec = infiniteRepeatable(
            animation = tween(6_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "shimmer"
    )

    val baseTop = StitchThemeColors.pageBackground()
    val baseMid = MaterialTheme.colorScheme.surfaceVariant
    val baseBottom = if (isDark) Color(0xFF1A1C1F) else Color(0xFFEBE4DC)
    val bronze = if (isDark) TaskPulseColors.BronzeLight else TaskPulseColors.Bronze
    val grayGlow = StitchThemeColors.calendarGradientGray()
    val primary = MaterialTheme.colorScheme.primary

    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(baseTop, baseMid, baseBottom),
                start = Offset(0f, 0f),
                end = Offset(size.width * 0.4f, size.height)
            )
        )

        val w = size.width
        val h = size.height
        val centerHero = Offset(w * 0.5f, h * (0.42f + drift * 0.008f))

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    primary.copy(alpha = if (isDark) 0.12f else 0.08f),
                    bronze.copy(alpha = if (isDark) 0.06f else 0.04f),
                    Color.Transparent
                ),
                center = centerHero,
                radius = w * 0.55f * shimmer
            ),
            radius = w * 0.55f * shimmer,
            center = centerHero
        )

        val bronzeCenter = Offset(
            x = w * (0.88f + drift * 0.025f),
            y = h * (0.12f - drift * 0.015f)
        )
        val grayCenter = Offset(
            x = w * (0.10f - drift * 0.02f),
            y = h * (0.82f + drift * 0.018f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    bronze.copy(alpha = if (isDark) 0.18f else 0.14f),
                    bronze.copy(alpha = if (isDark) 0.07f else 0.05f),
                    Color.Transparent
                ),
                center = bronzeCenter,
                radius = w * 0.52f
            ),
            radius = w * 0.52f,
            center = bronzeCenter
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(grayGlow, Color.Transparent),
                center = grayCenter,
                radius = w * 0.48f
            ),
            radius = w * 0.48f,
            center = grayCenter
        )

        drawRect(
            brush = Brush.verticalGradient(
                colors = listOf(
                    Color.Transparent,
                    if (isDark) Color.Black.copy(alpha = 0.18f) else Color.Black.copy(alpha = 0.04f),
                    if (isDark) Color.Black.copy(alpha = 0.35f) else Color.Black.copy(alpha = 0.08f)
                ),
                startY = h * 0.55f,
                endY = h
            )
        )
    }
}
