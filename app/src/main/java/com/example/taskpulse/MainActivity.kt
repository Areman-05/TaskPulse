package com.example.taskpulse

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.taskpulse.core.AppContainer
import com.example.taskpulse.ui.navigation.TaskPulseNavHost
import com.example.taskpulse.ui.theme.TaskPulseTheme

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        container = AppContainer(applicationContext)
        lifecycleScope.launch {
            container.ensureDefaultCategoryUseCase()
            container.ensureStarterAutomationRulesUseCase()
        }
        enableEdgeToEdge()
        setContent {
            TaskPulseTheme {
                TaskPulseNavHost(container)
            }
        }
    }
}