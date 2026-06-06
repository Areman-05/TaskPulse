package com.example.taskpulse.ui.splash

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.taskpulse.R
import com.example.taskpulse.domain.usecase.SplashBootstrapPhase
import com.example.taskpulse.ui.theme.StitchTypography
import com.example.taskpulse.ui.theme.TaskPulseColors
import kotlin.math.roundToInt
import kotlinx.coroutines.delay

@Composable
fun TaskPulseSplashScreen(
    state: SplashUiState,
    onFirstFrame: () -> Unit = {},
    modifier: Modifier = Modifier
) {
    LaunchedEffect(Unit) { onFirstFrame() }

    var revealText by remember { mutableStateOf(false) }
    var revealProgress by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        delay(100)
        revealText = true
        revealProgress = true
    }

    val contentAlpha by animateFloatAsState(
        targetValue = if (state.finished) 0f else 1f,
        animationSpec = tween(if (state.finished) 380 else 0),
        label = "splashFadeOut"
    )
    val textAlpha by animateFloatAsState(
        targetValue = if (revealText && !state.finished) 1f else 0f,
        animationSpec = tween(720, easing = FastOutSlowInEasing),
        label = "textFadeIn"
    )
    val textOffset by animateFloatAsState(
        targetValue = if (revealText && !state.finished) 0f else 22f,
        animationSpec = tween(720, easing = FastOutSlowInEasing),
        label = "textSlideIn"
    )
    val progressPanelAlpha by animateFloatAsState(
        targetValue = if (revealProgress && !state.finished) 1f else 0f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "progressFadeIn"
    )
    val progressPanelOffset by animateFloatAsState(
        targetValue = if (revealProgress && !state.finished) 0f else 24f,
        animationSpec = tween(500, easing = FastOutSlowInEasing),
        label = "progressSlideIn"
    )

    val progress = state.progress.coerceIn(0f, 1f)
    val percent = (progress * 100f).roundToInt()
    val primary = MaterialTheme.colorScheme.primary

    Box(modifier = modifier.fillMaxSize()) {
        TaskPulseNebulaBackground(modifier = Modifier.fillMaxSize())

        Column(
            modifier = Modifier
                .fillMaxSize()
                .graphicsLayer { alpha = contentAlpha }
                .padding(horizontal = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(modifier = Modifier.offset(y = (-28).dp)) {
                StitchSplashMark(
                    modifier = Modifier.fillMaxWidth(),
                    visible = !state.finished
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Column(
                modifier = Modifier.graphicsLayer {
                    alpha = textAlpha
                    translationY = textOffset
                },
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.splash_brand_name),
                    style = StitchTypography.headlineMd.copy(
                        fontSize = 38.sp,
                        lineHeight = 44.sp,
                        fontWeight = FontWeight.W600,
                        letterSpacing = (-0.5).sp
                    ),
                    color = MaterialTheme.colorScheme.onBackground,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(14.dp))

                Box(contentAlignment = Alignment.Center) {
                    Spacer(
                        modifier = Modifier
                            .width(56.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(primary.copy(alpha = 0.18f))
                    )
                    Spacer(
                        modifier = Modifier
                            .width(56.dp)
                            .height(4.dp)
                            .clip(RoundedCornerShape(999.dp))
                            .background(
                                Brush.horizontalGradient(
                                    colors = listOf(
                                        primary.copy(alpha = 0.35f),
                                        TaskPulseColors.Bronze,
                                        primary.copy(alpha = 0.35f)
                                    )
                                )
                            )
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = stringResource(R.string.splash_tagline),
                    style = StitchTypography.bodyLg,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.9f),
                    textAlign = TextAlign.Center
                )
            }
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .fillMaxWidth()
                .padding(horizontal = 40.dp, vertical = 52.dp)
                .graphicsLayer {
                    alpha = contentAlpha * progressPanelAlpha
                    translationY = progressPanelOffset
                },
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = splashPhaseLabel(state.phase),
                    style = StitchTypography.labelLg,
                    color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.72f)
                )
                Text(
                    text = "$percent%",
                    style = StitchTypography.labelLg.copy(fontWeight = FontWeight.W500),
                    color = primary.copy(alpha = 0.85f)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            StitchSplashProgressBar(progress = progress)
        }
    }
}

@Composable
private fun StitchSplashProgressBar(
    progress: Float,
    modifier: Modifier = Modifier
) {
    val primary = MaterialTheme.colorScheme.primary
    val bronze = TaskPulseColors.Bronze
    val animatedProgress by animateFloatAsState(
        targetValue = progress,
        animationSpec = tween(520, easing = FastOutSlowInEasing),
        label = "progressFill"
    )

    Box(
        modifier = modifier
            .fillMaxWidth()
            .height(3.dp)
            .clip(RoundedCornerShape(999.dp))
            .background(primary.copy(alpha = 0.12f))
    ) {
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .fillMaxWidth(animatedProgress.coerceIn(0f, 1f))
                .clip(RoundedCornerShape(999.dp))
                .background(
                    Brush.horizontalGradient(
                        colors = listOf(
                            primary.copy(alpha = 0.55f),
                            bronze.copy(alpha = 0.92f)
                        )
                    )
                )
        )
    }
}

@Composable
private fun splashPhaseLabel(phase: SplashBootstrapPhase): String = when (phase) {
    SplashBootstrapPhase.Database -> stringResource(R.string.splash_phase_database)
    SplashBootstrapPhase.Data -> stringResource(R.string.splash_phase_data)
    SplashBootstrapPhase.Files -> stringResource(R.string.splash_phase_files)
    SplashBootstrapPhase.Services -> stringResource(R.string.splash_phase_services)
    SplashBootstrapPhase.Ready -> stringResource(R.string.splash_phase_ready)
}
