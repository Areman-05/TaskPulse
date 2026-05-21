package com.example.taskpulse

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import com.example.taskpulse.TaskPulseApp
import com.example.taskpulse.ui.TaskPulseAppRoot
import com.example.taskpulse.worker.AutomationInitialWork
import com.example.taskpulse.worker.AutomationWorkScheduler
import com.example.taskpulse.ui.theme.TaskPulseThemeRoot

class MainActivity : ComponentActivity() {
    private lateinit var container: com.example.taskpulse.core.AppContainer

    private val postNotificationsPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (!granted) {
                Toast.makeText(
                    this,
                    getString(R.string.post_notifications_denied_hint),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        var keepSystemSplash = true
        splashScreen.setKeepOnScreenCondition { keepSystemSplash }
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
        container = (application as TaskPulseApp).container
        lifecycleScope.launch {
            container.ensureDefaultCategoryUseCase()
            container.ensureStarterAutomationRulesUseCase()
            container.runEntryLifecycleMaintenanceUseCase()
            AutomationWorkScheduler.enqueue(
                context = applicationContext,
                repeatIntervalHours = container.getAutomationSweepIntervalHours(),
                settings = container.automationSettingsRepository
            )
            AutomationInitialWork.enqueueOnce(applicationContext)
        }
        enableEdgeToEdge()
        setContent {
            TaskPulseThemeRoot(container) {
                TaskPulseAppRoot(
                    container = container,
                    onSplashFirstFrame = { keepSystemSplash = false },
                    onSplashFinished = { }
                )
            }
        }
    }
}