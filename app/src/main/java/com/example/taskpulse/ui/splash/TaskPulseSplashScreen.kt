package com.example.taskpulse.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskpulse.R
import com.example.taskpulse.ui.theme.TaskPulseColors
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

const val SPLASH_DURATION_MS = 3000

@Composable
fun TaskPulseSplashScreen(
    onFinished: () -> Unit,
    onFirstFrame: () -> Unit = {}
) {
    var progress by remember { mutableFloatStateOf(0f) }

    LaunchedEffect(Unit) {
        onFirstFrame()
        animate(
            initialValue = 0f,
            targetValue = 1f,
            animationSpec = tween(durationMillis = SPLASH_DURATION_MS, easing = FastOutSlowInEasing)
        ) { value, _ ->
            progress = value
        }
        onFinished()
    }

    val p = progress
    val fadeOut = ((p - 0.9f) / 0.1f).coerceIn(0f, 1f)
    val contentAlpha = 1f - fadeOut

    val infinite = rememberInfiniteTransition(label = "splash")
    val breathe by infinite.animateFloat(
        initialValue = 0.94f,
        targetValue = 1.06f,
        animationSpec = infiniteRepeatable(tween(900, easing = FastOutSlowInEasing), RepeatMode.Reverse),
        label = "breathe"
    )

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TaskPulseColors.Black),
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val cx = size.width / 2f
            val cy = size.height / 2f - size.height * 0.08f
            val logoSize = size.minDimension * 0.36f
            val gridAlpha = 0.1f * contentAlpha
            val step = logoSize / 5f
            var gx = cx - logoSize
            while (gx <= cx + logoSize) {
                drawLine(
                    Color.White.copy(alpha = gridAlpha),
                    Offset(gx, cy - logoSize),
                    Offset(gx, cy + logoSize),
                    strokeWidth = 1f
                )
                gx += step
            }
            var gy = cy - logoSize
            while (gy <= cy + logoSize) {
                drawLine(
                    Color.White.copy(alpha = gridAlpha),
                    Offset(cx - logoSize, gy),
                    Offset(cx + logoSize, gy),
                    strokeWidth = 1f
                )
                gy += step
            }
        }

        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer {
                scaleX = breathe * (0.88f + 0.12f * p)
                scaleY = breathe * (0.88f + 0.12f * p)
                alpha = contentAlpha
            }
        ) {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.size(200.dp)) {
                TaskPulseLogoMark(progress = p, modifier = Modifier.fillMaxSize())
                val monogramAlpha = ((p - 0.12f) / 0.35f).coerceIn(0f, 1f)
                Text(
                    text = "TP",
                    color = TaskPulseColors.White.copy(alpha = monogramAlpha),
                    fontSize = 52.sp,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = (-2).sp
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            val titleAlpha = ((p - 0.2f) / 0.45f).coerceIn(0f, 1f)
            Text(
                text = stringResource(R.string.splash_brand_name),
                color = TaskPulseColors.White.copy(alpha = titleAlpha),
                fontSize = 26.sp,
                fontWeight = FontWeight.W300,
                letterSpacing = 7.sp
            )
            Text(
                text = stringResource(R.string.splash_tagline),
                color = TaskPulseColors.Celestial.copy(alpha = titleAlpha),
                fontSize = 11.sp,
                fontWeight = FontWeight.W600,
                letterSpacing = 4.sp,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        LinearProgressIndicator(
            progress = { p },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 56.dp)
                .height(3.dp)
                .graphicsLayer { alpha = contentAlpha },
            color = TaskPulseColors.Celestial,
            trackColor = Color.White.copy(alpha = 0.25f),
        )
    }
}

@Composable
private fun TaskPulseLogoMark(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val spin = rememberInfiniteTransition(label = "logoSpin")
    val rotation by spin.animateFloat(
        initialValue = 0f,
        targetValue = 360f,
        animationSpec = infiniteRepeatable(tween(7000, easing = LinearEasing)),
        label = "rotation"
    )

    Canvas(modifier = modifier) {
        val cx = size.width / 2f
        val cy = size.height / 2f
        val r = size.minDimension * 0.44f

        drawCircle(
            color = Color.White.copy(alpha = 0.15f),
            radius = r * 1.08f,
            center = Offset(cx, cy),
            style = Stroke(width = 2f)
        )

        rotate(rotation + progress * 90f, pivot = Offset(cx, cy)) {
            val ticks = 32
            val visibleTicks = (ticks * (0.3f + 0.7f * progress)).toInt().coerceIn(1, ticks)
            for (i in 0 until visibleTicks) {
                val angle = (i / ticks.toFloat()) * 360f
                val rad = Math.toRadians(angle.toDouble())
                val inner = r * 0.86f
                val outer = r * 1.04f
                drawLine(
                    color = Color.White.copy(alpha = 0.85f),
                    start = Offset(
                        cx + (cos(rad) * inner).toFloat(),
                        cy + (sin(rad) * inner).toFloat()
                    ),
                    end = Offset(
                        cx + (cos(rad) * outer).toFloat(),
                        cy + (sin(rad) * outer).toFloat()
                    ),
                    strokeWidth = 3f,
                    cap = StrokeCap.Round
                )
            }
        }

        val ringScale = 0.5f + 0.5f * progress
        drawCircle(
            color = TaskPulseColors.Celestial,
            radius = r * 0.74f * ringScale,
            center = Offset(cx, cy),
            style = Stroke(width = 4f)
        )
        drawCircle(
            color = Color.White.copy(alpha = 0.95f),
            radius = r * 0.5f * ringScale,
            center = Offset(cx, cy),
            style = Stroke(width = 2.5f)
        )

        val pulse = buildPulsePath(cx, cy, r * 0.58f)
        val measure = PathMeasure()
        measure.setPath(pulse, false)
        val segment = Path()
        measure.getSegment(
            0f,
            measure.length * progress.coerceIn(0.08f, 1f),
            segment,
            true
        )
        drawPath(
            segment,
            TaskPulseColors.Celestial,
            style = Stroke(width = 5f, cap = StrokeCap.Round)
        )

        drawCircle(
            color = Color.White,
            radius = 8f + 3f * sin(progress * PI.toFloat() * 3f),
            center = Offset(cx, cy)
        )
    }
}

private fun buildPulsePath(cx: Float, cy: Float, width: Float): Path {
    val path = Path()
    val left = cx - width
    val right = cx + width
    path.moveTo(left, cy)
    path.lineTo(cx - width * 0.5f, cy)
    path.lineTo(cx - width * 0.32f, cy - width * 0.28f)
    path.lineTo(cx - width * 0.14f, cy + width * 0.32f)
    path.lineTo(cx + width * 0.08f, cy - width * 0.1f)
    path.lineTo(cx + width * 0.28f, cy + width * 0.35f)
    path.lineTo(cx + width * 0.48f, cy - width * 0.2f)
    path.lineTo(cx + width * 0.65f, cy)
    path.lineTo(right, cy)
    return path
}
