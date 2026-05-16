package com.example.taskpulse.ui.home

import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.example.taskpulse.domain.model.DailyProductivityPoint

@Composable
fun HomeMiniPulseChart(
    points: List<DailyProductivityPoint>,
    modifier: Modifier = Modifier
) {
    var drawProgress by remember(points) { mutableStateOf(0f) }
    LaunchedEffect(points) {
        drawProgress = 0f
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(900)
        ) { value, _ ->
            drawProgress = value
        }
    }

    val animatedProgress by animateFloatAsState(drawProgress, tween(300), label = "chart")
    val primary = MaterialTheme.colorScheme.onSurface
    val accent = MaterialTheme.colorScheme.tertiary

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
    ) {
        if (points.isEmpty()) return@Canvas
        val max = points.maxOf { it.completedCount }.coerceAtLeast(1)
        val barWidth = size.width / (points.size * 2f)
        points.forEachIndexed { index, point ->
            val fraction = point.completedCount.toFloat() / max
            val barHeight = size.height * fraction * animatedProgress
            val x = barWidth + index * barWidth * 2f
            drawRoundRect(
                color = if (index == points.lastIndex) accent else primary.copy(alpha = 0.35f),
                topLeft = Offset(x, size.height - barHeight),
                size = Size(barWidth, barHeight.coerceAtLeast(4f)),
                cornerRadius = CornerRadius(4f, 4f)
            )
        }

        val path = Path()
        points.forEachIndexed { index, point ->
            val fraction = point.completedCount.toFloat() / max
            val x = barWidth + index * barWidth * 2f + barWidth / 2f
            val y = size.height - size.height * fraction * animatedProgress
            if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
        }
        val measure = PathMeasure()
        measure.setPath(path, false)
        val segment = Path()
        measure.getSegment(0f, measure.length * animatedProgress, segment, true)
        drawPath(
            segment,
            accent,
            style = Stroke(width = 2f, cap = StrokeCap.Round)
        )
    }
}
