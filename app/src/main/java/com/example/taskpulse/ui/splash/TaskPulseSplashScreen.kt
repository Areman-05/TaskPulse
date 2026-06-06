package com.example.taskpulse.ui.splash

import androidx.compose.foundation.background
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.taskpulse.R
import com.example.taskpulse.domain.usecase.SplashBootstrapPhase
import com.example.taskpulse.ui.theme.TaskPulseColors
import kotlin.math.roundToInt

@Composable
fun TaskPulseSplashScreen(
    state: SplashUiState,
    onFirstFrame: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { onFirstFrame() }

    val contentAlpha by animateFloatAsState(
        targetValue = if (state.finished) 0f else 1f,
        animationSpec = tween(if (state.finished) 320 else 0),
        label = "splashFadeOut"
    )
    val titleAlpha by animateFloatAsState(
        targetValue = 1f,
        animationSpec = tween(600),
        label = "titleFadeIn"
    )

    val progress = state.progress.coerceIn(0f, 1f)
    val percent = (progress * 100f).roundToInt()

    Box(modifier = modifier.fillMaxSize()) {
        TaskPulseNebulaBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = contentAlpha }
                .padding(horizontal = 40.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = stringResource(R.string.splash_brand_name),
                style = MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = MaterialTheme.colorScheme.onBackground.copy(alpha = titleAlpha),
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(10.dp))

            Spacer(
                modifier = Modifier
                    .width(40.dp)
                    .height(3.dp)
                    .clip(RoundedCornerShape(2.dp))
                    .background(TaskPulseColors.Bronze.copy(alpha = 0.92f * titleAlpha))
            )

            Spacer(modifier = Modifier.height(14.dp))

            Text(
                text = stringResource(R.string.splash_tagline),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.85f * titleAlpha),
                textAlign = TextAlign.Center
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .graphicsLayer { alpha = contentAlpha }
                .padding(horizontal = 32.dp, vertical = 52.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 12.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = splashPhaseLabel(state.phase),
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "$percent%",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.75f)
                )
            }
            LinearProgressIndicator(
                progress = { progress },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(3.dp)
                    .clip(RoundedCornerShape(1.5.dp)),
                color = TaskPulseColors.Bronze,
                trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.28f),
                strokeCap = StrokeCap.Round
            )
        }
    }
}

@Composable
private fun splashPhaseLabel(phase: SplashBootstrapPhase): String = when (phase) {
    SplashBootstrapPhase.Database -> stringResource(R.string.splash_phase_database)
    SplashBootstrapPhase.Data -> stringResource(R.string.splash_phase_data)
    SplashBootstrapPhase.Services -> stringResource(R.string.splash_phase_services)
    SplashBootstrapPhase.Ready -> stringResource(R.string.splash_phase_ready)
}
