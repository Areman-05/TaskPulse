package com.example.taskpulse

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.taskpulse.core.AppContainer
import com.example.taskpulse.ui.navigation.TaskPulseNavHost
import com.example.taskpulse.worker.AutomationInitialWork
import com.example.taskpulse.worker.AutomationWorkScheduler
import com.example.taskpulse.ui.theme.TaskPulseTheme

class MainActivity : ComponentActivity() {
    private lateinit var container: AppContainer

    private val postNotificationsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) {
                postNotificationsPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
            }
        }
        container = AppContainer(applicationContext)
        lifecycleScope.launch {
            container.ensureDefaultCategoryUseCase()
            container.ensureStarterAutomationRulesUseCase()
            AutomationWorkScheduler.enqueue(applicationContext)
            AutomationInitialWork.enqueueOnce(applicationContext)
        }
        enableEdgeToEdge()
        setContent {
            TaskPulseTheme {
                TaskPulseNavHost(container)
            }
        }
    }
}