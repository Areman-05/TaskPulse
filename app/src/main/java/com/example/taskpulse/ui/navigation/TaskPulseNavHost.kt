package com.example.taskpulse.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Analytics
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarDefaults
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.taskpulse.R
import com.example.taskpulse.core.AppContainer
import com.example.taskpulse.ui.create.CreateTaskScreen
import com.example.taskpulse.ui.create.CreateTaskViewModel
import com.example.taskpulse.ui.home.HomeScreen
import com.example.taskpulse.ui.home.HomeViewModel
import com.example.taskpulse.ui.insights.InsightsScreen
import com.example.taskpulse.ui.insights.InsightsViewModel
import com.example.taskpulse.ui.settings.SettingsScreen
import com.example.taskpulse.ui.settings.SettingsViewModel

@Composable
fun TaskPulseNavHost(container: AppContainer) {
    val appContext = LocalContext.current.applicationContext
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppDestinations.TASKS_ROUTE

    val showBottomBar = currentRoute in listOf(
        AppDestinations.TASKS_ROUTE,
        AppDestinations.INSIGHTS_ROUTE,
        AppDestinations.SETTINGS_ROUTE
    )

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                NavigationBar(
                    containerColor = MaterialTheme.colorScheme.surface,
                    tonalElevation = 0.dp,
                    windowInsets = NavigationBarDefaults.windowInsets
                ) {
                    NavigationBarItem(
                        icon = { Icon(Icons.Outlined.Home, contentDescription = null) },
                        label = {
                            Text(
                                stringResource(R.string.nav_tasks),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = navItemColors(),
                        selected = currentRoute == AppDestinations.TASKS_ROUTE,
                        onClick = {
                            navController.navigate(AppDestinations.TASKS_ROUTE) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Outlined.Analytics, contentDescription = null) },
                        label = {
                            Text(
                                stringResource(R.string.nav_insights),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = navItemColors(),
                        selected = currentRoute == AppDestinations.INSIGHTS_ROUTE,
                        onClick = {
                            navController.navigate(AppDestinations.INSIGHTS_ROUTE) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                    NavigationBarItem(
                        icon = { Icon(Icons.Outlined.Settings, contentDescription = null) },
                        label = {
                            Text(
                                stringResource(R.string.nav_settings),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = navItemColors(),
                        selected = currentRoute == AppDestinations.SETTINGS_ROUTE,
                        onClick = {
                            navController.navigate(AppDestinations.SETTINGS_ROUTE) {
                                popUpTo(navController.graph.findStartDestination().id) {
                                    saveState = true
                                }
                                launchSingleTop = true
                                restoreState = true
                            }
                        }
                    )
                }
            }
        }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = AppDestinations.TASKS_ROUTE,
            modifier = Modifier.padding(innerPadding)
        ) {
            composable(AppDestinations.TASKS_ROUTE) {
                val vm: HomeViewModel = viewModel(
                    factory = HomeViewModel.Factory(
                        observeTasksUseCase = container.observeTasksUseCase,
                        application = appContext as android.app.Application,
                        deleteTasksUseCase = container.deleteTasksUseCase,
                        updateTasksPriorityUseCase = container.updateTasksPriorityUseCase,
                        completeTaskAndStopRemindersUseCase = container.completeTaskAndStopRemindersUseCase
                    )
                )
                HomeScreen(
                    viewModel = vm,
                    onNavigateToCreate = {
                        navController.navigate(AppDestinations.CREATE_ROUTE)
                    }
                )
            }
            composable(AppDestinations.CREATE_ROUTE) {
                val vm: CreateTaskViewModel = viewModel(
                    factory = CreateTaskViewModel.Factory(
                        application = appContext as android.app.Application,
                        createDefaultTaskUseCase = container.createDefaultTaskUseCase,
                        upsertTaskUseCase = container.upsertTaskUseCase,
                        scheduleTaskReminderUseCase = container.scheduleTaskReminderUseCase
                    )
                )
                CreateTaskScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppDestinations.INSIGHTS_ROUTE) {
                val vm: InsightsViewModel = viewModel(
                    factory = InsightsViewModel.Factory(
                        observeDailyProductivityUseCase = container.observeDailyProductivityUseCase,
                        observeAutomationRulesUseCase = container.observeAutomationRulesUseCase,
                        setAutomationRuleEnabledUseCase = container.setAutomationRuleEnabledUseCase,
                        triggerAutomationSweepNowUseCase = container.triggerAutomationSweepNowUseCase,
                        upsertAutomationRuleUseCase = container.upsertAutomationRuleUseCase,
                        updateAutomationRuleDefinitionUseCase = container.updateAutomationRuleDefinitionUseCase,
                        deleteAutomationRuleUseCase = container.deleteAutomationRuleUseCase,
                        getAutomationRuleUseCase = container.getAutomationRuleUseCase,
                        getEnabledAutomationRuleCountUseCase = container.getEnabledAutomationRuleCountUseCase,
                        getAutomationSweepIntervalUseCase = container.getAutomationSweepIntervalUseCase,
                        setAutomationSweepIntervalUseCase = container.setAutomationSweepIntervalUseCase,
                        rescheduleAutomationSweepUseCase = container.rescheduleAutomationSweepUseCase,
                        automationSettingsRepository = container.automationSettingsRepository,
                        loadAutomationSweepHistoryUseCase = container.loadAutomationSweepHistoryUseCase,
                        taskSnapshotFileExporter = container.taskSnapshotFileExporter,
                        roomDatabaseFile = appContext.getDatabasePath("taskpulse.db")
                    )
                )
                InsightsScreen(viewModel = vm)
            }
            composable(AppDestinations.SETTINGS_ROUTE) {
                val vm: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(
                        themeRepository = container.themeRepository
                    )
                )
                SettingsScreen(viewModel = vm)
            }
        }
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = MaterialTheme.colorScheme.surfaceVariant,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
)
