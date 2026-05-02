package com.example.taskpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.viewmodel.compose.viewModel
import kotlinx.coroutines.launch
import com.example.taskpulse.core.AppContainer
import com.example.taskpulse.ui.home.HomeScreen
import com.example.taskpulse.ui.home.HomeViewModel
import com.example.taskpulse.ui.theme.TaskPulseTheme

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container = AppContainer(applicationContext)
        lifecycleScope.launch {
            container.ensureStarterAutomationRulesUseCase()
        }
        enableEdgeToEdge()
        setContent {
            TaskPulseTheme {
                val vm: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(
                        observeTasksUseCase = container.observeTasksUseCase,
                        observeDailyProductivityUseCase = container.observeDailyProductivityUseCase,
                        createDefaultTaskUseCase = container.createDefaultTaskUseCase,
                        upsertTaskUseCase = container.upsertTaskUseCase,
                        markTaskCompletedUseCase = container.markTaskCompletedUseCase,
                        scheduleTaskReminderUseCase = container.scheduleTaskReminderUseCase
                    )
                )
                HomeScreen(viewModel = vm)
            }
        }
    }
}