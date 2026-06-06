package com.example.taskpulse.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskpulse.R
import com.example.taskpulse.ui.theme.TaskPulseColors

const val SPLASH_DURATION_MS = 2500

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

    val contentAlpha = 1f - ((progress - 0.88f) / 0.12f).coerceIn(0f, 1f)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(TaskPulseColors.Gray100),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.graphicsLayer { alpha = contentAlpha }
        ) {
            Box(
                modifier = Modifier
                    .size(88.dp)
                    .clip(CircleShape)
                    .background(TaskPulseColors.Bronze),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "TP",
                    color = TaskPulseColors.White,
                    fontSize = 32.sp,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            val titleAlpha = ((progress - 0.15f) / 0.4f).coerceIn(0f, 1f)
            Text(
                text = stringResource(R.string.splash_brand_name),
                color = TaskPulseColors.Gray900.copy(alpha = titleAlpha),
                style = MaterialTheme.typography.headlineSmall
            )
            Text(
                text = stringResource(R.string.splash_tagline),
                color = TaskPulseColors.Gray700.copy(alpha = titleAlpha * 0.9f),
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.padding(top = 6.dp)
            )
        }

        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 48.dp, vertical = 56.dp)
                .height(3.dp)
                .graphicsLayer { alpha = contentAlpha },
            color = TaskPulseColors.Bronze,
            trackColor = TaskPulseColors.Gray300.copy(alpha = 0.6f),
        )
    }
}
