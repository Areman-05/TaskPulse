package com.example.taskpulse.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Info
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.example.taskpulse.core.AppContainer
import com.example.taskpulse.ui.home.HomeScreen
import com.example.taskpulse.ui.home.HomeViewModel
import com.example.taskpulse.ui.insights.InsightsScreen
import com.example.taskpulse.ui.insights.InsightsViewModel
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun TaskPulseNavHost(container: AppContainer) {
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppDestinations.TASKS_ROUTE

    Scaffold(
        bottomBar = {
            NavigationBar {
                NavigationBarItem(
                    icon = { Icon(Icons.Filled.Home, contentDescription = null) },
                    label = { Text("Tareas") },
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
                    icon = { Icon(Icons.Filled.Info, contentDescription = null) },
                    label = { Text("Insights") },
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
                        observeDailyProductivityUseCase = container.observeDailyProductivityUseCase,
                        createDefaultTaskUseCase = container.createDefaultTaskUseCase,
                        upsertTaskUseCase = container.upsertTaskUseCase,
                        markTaskCompletedUseCase = container.markTaskCompletedUseCase,
                        scheduleTaskReminderUseCase = container.scheduleTaskReminderUseCase
                    )
                )
                HomeScreen(viewModel = vm)
            }
            composable(AppDestinations.INSIGHTS_ROUTE) {
                val vm: InsightsViewModel = viewModel(
                    factory = InsightsViewModel.Factory(
                        observeDailyProductivityUseCase = container.observeDailyProductivityUseCase,
                        observeAutomationRulesUseCase = container.observeAutomationRulesUseCase
                    )
                )
                InsightsScreen(viewModel = vm)
            }
        }
    }
}
