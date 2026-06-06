package com.example.taskpulse.ui

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.taskpulse.core.AppContainer
import com.example.taskpulse.ui.navigation.TaskPulseNavHost
import com.example.taskpulse.ui.splash.SplashViewModel
import com.example.taskpulse.ui.splash.TaskPulseSplashScreen

@Composable
fun TaskPulseAppRoot(
    container: AppContainer,
    splashFinishedExternal: Boolean = false,
    onSplashFinished: () -> Unit = {},
    onSplashFirstFrame: () -> Unit = {}
) {
    var showSplash by rememberSaveable { mutableStateOf(!splashFinishedExternal) }

    Box(modifier = Modifier.fillMaxSize()) {
        AnimatedVisibility(
            visible = !showSplash,
            enter = fadeIn(animationSpec = tween(400)),
            exit = fadeOut(animationSpec = tween(200))
        ) {
            TaskPulseNavHost(container = container)
        }

        if (showSplash) {
            val splashViewModel: SplashViewModel = viewModel(
                factory = SplashViewModel.Factory(container.runAppBootstrapUseCase)
            )
            val splashState by splashViewModel.uiState.collectAsStateWithLifecycle()

            LaunchedEffect(Unit) {
                splashViewModel.start()
            }

            LaunchedEffect(splashState.finished) {
                if (splashState.finished) {
                    showSplash = false
                    onSplashFinished()
                }
            }

            TaskPulseSplashScreen(
                state = splashState,
                onFirstFrame = onSplashFirstFrame
            )
        }
    }
}
