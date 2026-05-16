package com.example.taskpulse.ui.components

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
import androidx.compose.ui.graphics.Color

/**
 * Rejilla sutil animada (misma idea que la splash).
 */
@Composable
fun TaskPulseAmbientGrid(
    modifier: Modifier = Modifier
) {
    val infinite = rememberInfiniteTransition(label = "ambientGrid")
    val drift by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(14000, easing = LinearEasing), RepeatMode.Restart),
        label = "drift"
    )
    val lineColor = MaterialTheme.colorScheme.onSurface

    Canvas(modifier = modifier) {
        val step = size.minDimension / 14f
        val offset = drift * step
        val alpha = 0.06f
        var x = -step + offset
        while (x < size.width + step) {
            drawLine(
                color = lineColor.copy(alpha = alpha),
                start = Offset(x, 0f),
                end = Offset(x, size.height),
                strokeWidth = 1f
            )
            x += step
        }
        var y = -step + offset * 0.7f
        while (y < size.height + step) {
            drawLine(
                color = lineColor.copy(alpha = alpha * 0.85f),
                start = Offset(0f, y),
                end = Offset(size.width, y),
                strokeWidth = 1f
            )
            y += step
        }
    }
}
