package com.example.taskpulse.ui.splash

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.taskpulse.domain.usecase.RunAppBootstrapUseCase
import com.example.taskpulse.domain.usecase.SplashBootstrapPhase
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

/** Tiempo mínimo visible para que la barra se lea con calma. */
const val SPLASH_MIN_DISPLAY_MS = 1800L

/** Margen extra para tests instrumentados. */
const val SPLASH_TEST_ADVANCE_MS = 3200L

private const val PHASE_HOLD_MS = 240L
private const val PROGRESS_TICK_MS = 16L

data class SplashUiState(
    val progress: Float = 0f,
    val phase: SplashBootstrapPhase = SplashBootstrapPhase.Database,
    val finished: Boolean = false
)

class SplashViewModel(
    private val runAppBootstrapUseCase: RunAppBootstrapUseCase
) : ViewModel() {

    private val _uiState = MutableStateFlow(SplashUiState())
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private var started = false

    fun start() {
        if (started) return
        started = true
        viewModelScope.launch {
            val startedAt = System.currentTimeMillis()
            runAppBootstrapUseCase { phase, targetProgress ->
                _uiState.update { it.copy(phase = phase) }
                animateProgressTo(targetProgress)
                if (targetProgress < 1f) {
                    delay(PHASE_HOLD_MS)
                }
            }
            val elapsed = System.currentTimeMillis() - startedAt
            if (elapsed < SPLASH_MIN_DISPLAY_MS) {
                delay(SPLASH_MIN_DISPLAY_MS - elapsed)
            }
            _uiState.update {
                it.copy(progress = 1f, phase = SplashBootstrapPhase.Ready, finished = true)
            }
        }
    }

    private suspend fun animateProgressTo(target: Float) {
        while (_uiState.value.progress < target - 0.004f) {
            delay(PROGRESS_TICK_MS)
            _uiState.update { state ->
                val gap = target - state.progress
                val step = (gap * 0.14f).coerceIn(0.003f, 0.055f)
                state.copy(progress = (state.progress + step).coerceAtMost(target))
            }
        }
        _uiState.update { it.copy(progress = target) }
    }

    class Factory(
        private val runAppBootstrapUseCase: RunAppBootstrapUseCase
    ) : ViewModelProvider.Factory {
        @Suppress("UNCHECKED_CAST")
        override fun <T : ViewModel> create(modelClass: Class<T>): T {
            return SplashViewModel(runAppBootstrapUseCase) as T
        }
    }
}
