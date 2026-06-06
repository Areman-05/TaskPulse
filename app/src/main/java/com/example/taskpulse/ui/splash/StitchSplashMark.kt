package com.example.taskpulse.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.Spring
import androidx.compose.animation.core.StartOffset
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.spring
import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.scale
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.unit.dp
import com.example.taskpulse.ui.theme.StitchThemeColors
import com.example.taskpulse.ui.theme.TaskPulseColors
import kotlin.math.cos
import kotlin.math.sin
import kotlinx.coroutines.delay

private val PulseWavePoints = floatArrayOf(
    0.00f, 0.50f,
    0.12f, 0.50f,
    0.18f, 0.50f,
    0.22f, 0.28f,
    0.26f, 0.82f,
    0.30f, 0.14f,
    0.34f, 0.58f,
    0.40f, 0.50f,
    0.55f, 0.50f,
    0.62f, 0.50f,
    0.66f, 0.30f,
    0.70f, 0.78f,
    0.74f, 0.18f,
    0.78f, 0.55f,
    0.84f, 0.50f,
    1.00f, 0.50f
)

/** Hero mark: halo, anillos, orbe glass, icono y trazo de pulso animado. */
@Composable
fun StitchSplashMark(
    modifier: Modifier = Modifier,
    visible: Boolean = true
) {
    var animateIn by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(60)
        animateIn = true
    }

    val iconScale by animateFloatAsState(
        targetValue = if (animateIn && visible) 1f else 0.45f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessMedium
        ),
        label = "splashIconScale"
    )
    val iconAlpha by animateFloatAsState(
        targetValue = if (animateIn && visible) 1f else 0f,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "splashIconAlpha"
    )
    val waveAlpha by animateFloatAsState(
        targetValue = if (animateIn && visible) 1f else 0f,
        animationSpec = tween(800, delayMillis = 220, easing = FastOutSlowInEasing),
        label = "waveAlpha"
    )

    val infinite = rememberInfiniteTransition(label = "splashPulse")
    val pulseA by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "pulseA"
    )
    val pulseB by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(560)
        ),
        label = "pulseB"
    )
    val pulseC by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart,
            initialStartOffset = StartOffset(1_120)
        ),
        label = "pulseC"
    )
    val orbit by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(
            animation = tween(12_000, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "orbit"
    )
    val breathe by infinite.animateFloat(
        initialValue = 1f,
        targetValue = 1.045f,
        animationSpec = infiniteRepeatable(
            animation = tween(2_600, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "breathe"
    )
    val wavePhase by infinite.animateFloat(
        initialValue = 0f,
        targetValue = 1f,
        animationSpec = infiniteRepeatable(
            animation = tween(1_800, easing = LinearEasing),
            repeatMode = RepeatMode.Restart
        ),
        label = "wavePhase"
    )

    val primary = MaterialTheme.colorScheme.primary
    val bronze = TaskPulseColors.Bronze
    val ringColor = bronze.copy(alpha = 0.6f)
    val innerFill = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.55f)

    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Box(
            modifier = Modifier.size(200.dp),
            contentAlignment = Alignment.Center
        ) {
            Canvas(
                modifier = Modifier
                    .size(200.dp)
                    .graphicsLayer { alpha = iconAlpha }
            ) {
                val c = center
                val dim = size.minDimension

                drawCircle(
                    brush = Brush.radialGradient(
                        colors = listOf(
                            primary.copy(alpha = 0.22f),
                            primary.copy(alpha = 0.08f),
                            Color.Transparent
                        ),
                        center = c,
                        radius = dim * 0.48f
                    ),
                    radius = dim * 0.48f,
                    center = c
                )

                listOf(pulseA, pulseB, pulseC).forEach { progress ->
                    val radius = dim * 0.30f + progress * dim * 0.26f
                    val alpha = (1f - progress) * 0.42f
                    drawCircle(
                        color = ringColor.copy(alpha = alpha),
                        radius = radius,
                        center = c,
                        style = Stroke(width = 2.8f, cap = StrokeCap.Round)
                    )
                }

                rotate(orbit) {
                    drawArc(
                        color = primary.copy(alpha = 0.18f),
                        startAngle = 0f,
                        sweepAngle = 110f,
                        useCenter = false,
                        topLeft = Offset(c.x - dim * 0.44f, c.y - dim * 0.44f),
                        size = androidx.compose.ui.geometry.Size(dim * 0.88f, dim * 0.88f),
                        style = Stroke(width = 2f, cap = StrokeCap.Round)
                    )
                    drawArc(
                        color = bronze.copy(alpha = 0.12f),
                        startAngle = 180f,
                        sweepAngle = 70f,
                        useCenter = false,
                        topLeft = Offset(c.x - dim * 0.44f, c.y - dim * 0.44f),
                        size = androidx.compose.ui.geometry.Size(dim * 0.88f, dim * 0.88f),
                        style = Stroke(width = 1.5f, cap = StrokeCap.Round)
                    )
                }
            }

            Surface(
                modifier = Modifier
                    .size(112.dp)
                    .scale(iconScale * breathe)
                    .graphicsLayer {
                        alpha = iconAlpha
                        transformOrigin = androidx.compose.ui.graphics.TransformOrigin.Center
                    }
                    .drawBehind {
                        drawCircle(
                            brush = Brush.radialGradient(
                                colors = listOf(
                                    primary.copy(alpha = 0.14f),
                                    Color.Transparent
                                ),
                                center = center,
                                radius = size.minDimension / 2f
                            )
                        )
                    },
                shape = CircleShape,
                color = StitchThemeColors.glassSurface(),
                border = BorderStroke(2.dp, primary.copy(alpha = 0.42f)),
                shadowElevation = 12.dp,
                tonalElevation = 0.dp
            ) {
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawSplashOrbCore(
                        center = center,
                        diameter = size.minDimension,
                        primary = primary,
                        innerFill = innerFill
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(20.dp))

        Canvas(
            modifier = Modifier
                .fillMaxWidth()
                .height(40.dp)
                .graphicsLayer { alpha = waveAlpha * 0.95f }
        ) {
            val w = size.width
            val h = size.height
            val path = Path()
            for (i in PulseWavePoints.indices step 2) {
                val x = PulseWavePoints[i] * w
                val y = PulseWavePoints[i + 1] * h
                if (i == 0) path.moveTo(x, y) else path.lineTo(x, y)
            }
            val measure = PathMeasure()
            measure.setPath(path, false)
            val length = measure.length
            val visibleLength = length * (0.35f + wavePhase * 0.65f)
            val start = (wavePhase * length * 0.15f).coerceAtMost(length - visibleLength)

            drawPath(
                path = path,
                color = primary.copy(alpha = 0.12f),
                style = Stroke(width = 3f, cap = StrokeCap.Round)
            )

            val trimPath = Path()
            measure.getSegment(start, start + visibleLength, trimPath, true)
            drawPath(
                path = trimPath,
                brush = Brush.horizontalGradient(
                    colors = listOf(
                        primary.copy(alpha = 0.35f),
                        bronze.copy(alpha = 0.95f),
                        primary.copy(alpha = 0.55f)
                    ),
                    startX = 0f,
                    endX = w
                ),
                style = Stroke(width = 3.5f, cap = StrokeCap.Round)
            )

            val dotOffset = measure.getPosition((start + visibleLength).coerceAtMost(length))
            drawCircle(
                brush = Brush.radialGradient(
                    colors = listOf(
                        bronze.copy(alpha = 0.9f),
                        bronze.copy(alpha = 0.2f),
                        Color.Transparent
                    ),
                    center = dotOffset,
                    radius = 14f
                ),
                radius = 14f,
                center = dotOffset
            )
            drawCircle(
                color = Color.White.copy(alpha = 0.95f),
                radius = 3.5f,
                center = dotOffset
            )
        }
    }
}

/** Anillo interior + tick dibujados en el mismo centro geométrico del orbe. */
private fun DrawScope.drawSplashOrbCore(
    center: Offset,
    diameter: Float,
    primary: Color,
    innerFill: Color
) {
    val innerRadius = diameter * 0.38f
    val ringRadius = diameter / 2f - 12.dp.toPx()
    val iconRadius = diameter * 0.22f
    val ringStroke = 1.dp.toPx()
    val iconStroke = (iconRadius * 0.16f).coerceAtLeast(2.dp.toPx())

    drawPath(
        path = octagonPath(center, innerRadius),
        color = innerFill
    )
    drawCircle(
        color = primary.copy(alpha = 0.12f),
        radius = ringRadius,
        center = center,
        style = Stroke(width = ringStroke)
    )
    drawCircle(
        color = primary,
        radius = iconRadius,
        center = center,
        style = Stroke(width = iconStroke, cap = StrokeCap.Round)
    )

    val check = Path().apply {
        moveTo(center.x - iconRadius * 0.42f, center.y + iconRadius * 0.02f)
        lineTo(center.x - iconRadius * 0.08f, center.y + iconRadius * 0.38f)
        lineTo(center.x + iconRadius * 0.48f, center.y - iconRadius * 0.35f)
    }
    drawPath(
        path = check,
        color = primary,
        style = Stroke(
            width = iconStroke,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}

private fun octagonPath(center: Offset, radius: Float): Path {
    val path = Path()
    repeat(8) { index ->
        val angle = (Math.PI / 4.0) * index - Math.PI / 2.0
        val x = center.x + radius * cos(angle).toFloat()
        val y = center.y + radius * sin(angle).toFloat()
        if (index == 0) path.moveTo(x, y) else path.lineTo(x, y)
    }
    path.close()
    return path
}
