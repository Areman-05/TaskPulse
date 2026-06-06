package com.example.taskpulse.ui.splash

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import com.example.taskpulse.ui.theme.TaskPulseColors

/**
 * Nebula minimalista: gradiente suave gris + bronce, sin efectos recargados.
 */
@Composable
fun TaskPulseNebulaBackground(
    modifier: Modifier = Modifier,
    darkTheme: Boolean = isSystemInDarkTheme()
) {
    val infinite = rememberInfiniteTransition(label = "nebula")
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(durationMillis = 18_000, easing = LinearEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "drift"
    )

    val baseTop = if (darkTheme) TaskPulseColors.Gray900 else TaskPulseColors.Gray50
    val baseMid = if (darkTheme) TaskPulseColors.GraySurfaceDark else TaskPulseColors.Gray100
    val baseBottom = if (darkTheme) Color(0xFF1A1C1F) else Color(0xFFEBE4DC)
    val bronze = if (darkTheme) TaskPulseColors.BronzeLight else TaskPulseColors.Bronze

    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(baseTop, baseMid, baseBottom),
                start = Offset(0f, 0f),
                end = Offset(size.width * 0.35f, size.height)
            )
        )

        val w = size.width
        val h = size.height
        val bronzeCenter = Offset(
            x = w * (0.82f + drift * 0.03f),
            y = h * (0.16f - drift * 0.02f)
        )
        val grayCenter = Offset(
            x = w * (0.14f - drift * 0.025f),
            y = h * (0.78f + drift * 0.02f)
        )

        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    bronze.copy(alpha = if (darkTheme) 0.14f else 0.10f),
                    bronze.copy(alpha = if (darkTheme) 0.05f else 0.03f),
                    Color.Transparent
                ),
                center = bronzeCenter,
                radius = w * 0.55f
            ),
            radius = w * 0.55f,
            center = bronzeCenter
        )
        drawCircle(
            brush = Brush.radialGradient(
                colors = listOf(
                    TaskPulseColors.Gray700.copy(alpha = if (darkTheme) 0.22f else 0.08f),
                    Color.Transparent
                ),
                center = grayCenter,
                radius = w * 0.50f
            ),
            radius = w * 0.50f,
            center = grayCenter
        )
    }
}
