package com.example.taskpulse.ui.navigation

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CalendarMonth
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
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.example.taskpulse.R
import com.example.taskpulse.core.AppContainer
import com.example.taskpulse.ui.calendar.CalendarScreen
import com.example.taskpulse.ui.calendar.CalendarViewModel
import com.example.taskpulse.ui.create.CreateTaskScreen
import com.example.taskpulse.ui.create.CreateTaskViewModel
import com.example.taskpulse.ui.detail.EntryDetailScreen
import com.example.taskpulse.ui.detail.EntryDetailViewModel
import com.example.taskpulse.ui.home.HomeScreen
import com.example.taskpulse.ui.home.HomeViewModel
import com.example.taskpulse.ui.settings.SettingsScreen
import com.example.taskpulse.ui.settings.SettingsViewModel
import com.example.taskpulse.ui.settings.archive.ArchiveScreen
import com.example.taskpulse.ui.settings.archive.ArchiveViewModel
import java.time.LocalDate

@Composable
fun TaskPulseNavHost(container: AppContainer) {
    val appContext = LocalContext.current.applicationContext
    val navController = rememberNavController()
    val navBackStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = navBackStackEntry?.destination?.route ?: AppDestinations.TASKS_ROUTE

    val showBottomBar = currentRoute in listOf(
        AppDestinations.TASKS_ROUTE,
        AppDestinations.CALENDAR_ROUTE,
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
                        icon = { Icon(Icons.Outlined.CalendarMonth, contentDescription = null) },
                        label = {
                            Text(
                                stringResource(R.string.nav_calendar),
                                style = MaterialTheme.typography.labelSmall
                            )
                        },
                        colors = navItemColors(),
                        selected = currentRoute == AppDestinations.CALENDAR_ROUTE,
                        onClick = {
                            navController.navigate(AppDestinations.CALENDAR_ROUTE) {
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
                        navController.navigate(AppDestinations.createRoute())
                    },
                    onOpenEntryDetail = { entryId ->
                        navController.navigate(AppDestinations.entryDetailRoute(entryId))
                    }
                )
            }
            composable(
                route = AppDestinations.ENTRY_DETAIL_ROUTE,
                arguments = listOf(
                    navArgument("entryId") { type = NavType.LongType }
                )
            ) { backStackEntry ->
                val entryId = backStackEntry.arguments?.getLong("entryId") ?: return@composable
                val vm: EntryDetailViewModel = viewModel(
                    factory = EntryDetailViewModel.Factory(
                        entryId = entryId,
                        observeTasksUseCase = container.observeTasksUseCase,
                        upsertTaskUseCase = container.upsertTaskUseCase,
                        scheduleTaskReminderUseCase = container.scheduleTaskReminderUseCase,
                        cancelTaskReminderUseCase = container.cancelTaskReminderUseCase,
                        completeTaskAndStopRemindersUseCase = container.completeTaskAndStopRemindersUseCase
                    )
                )
                EntryDetailScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppDestinations.CALENDAR_ROUTE) {
                val vm: CalendarViewModel = viewModel(
                    factory = CalendarViewModel.Factory(
                        observeAllTasksUseCase = container.observeAllTasksUseCase
                    )
                )
                CalendarScreen(
                    viewModel = vm,
                    onOpenEntry = { entryId ->
                        navController.navigate(AppDestinations.entryDetailRoute(entryId))
                    },
                    onNavigateToCreate = { scheduleDate ->
                        navController.navigate(AppDestinations.createRoute(scheduleDate))
                    }
                )
            }
            composable(
                route = AppDestinations.CREATE_ROUTE,
                arguments = listOf(
                    navArgument(AppDestinations.ARG_SCHEDULE_DATE) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val scheduleDateArg = backStackEntry.arguments
                    ?.getString(AppDestinations.ARG_SCHEDULE_DATE)
                    ?.takeIf { it.isNotBlank() }
                val initialScheduleDate = scheduleDateArg?.let(LocalDate::parse)
                val vm: CreateTaskViewModel = viewModel(
                    factory = CreateTaskViewModel.Factory(
                        application = appContext as android.app.Application,
                        createDefaultTaskUseCase = container.createDefaultTaskUseCase,
                        upsertTaskUseCase = container.upsertTaskUseCase,
                        scheduleTaskReminderUseCase = container.scheduleTaskReminderUseCase,
                        initialScheduleDate = initialScheduleDate
                    )
                )
                CreateTaskScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }
            composable(AppDestinations.SETTINGS_ROUTE) {
                val vm: SettingsViewModel = viewModel(
                    factory = SettingsViewModel.Factory(
                        themeRepository = container.themeRepository,
                        taskSnapshotFileExporter = container.taskSnapshotFileExporter,
                        roomDatabaseFile = appContext.getDatabasePath("taskpulse.db")
                    )
                )
                SettingsScreen(
                    viewModel = vm,
                    onOpenArchive = {
                        navController.navigate(AppDestinations.ARCHIVE_ROUTE)
                    }
                )
            }
            composable(AppDestinations.ARCHIVE_ROUTE) {
                val vm: ArchiveViewModel = viewModel(
                    factory = ArchiveViewModel.Factory(
                        observeArchivedTasksUseCase = container.observeArchivedTasksUseCase,
                        restoreArchivedEntryUseCase = container.restoreArchivedEntryUseCase,
                        deleteTasksUseCase = container.deleteTasksUseCase,
                        cancelTaskReminderUseCase = container.cancelTaskReminderUseCase
                    )
                )
                ArchiveScreen(
                    viewModel = vm,
                    onBack = { navController.popBackStack() }
                )
            }
        }
    }
}

@Composable
private fun navItemColors() = NavigationBarItemDefaults.colors(
    selectedIconColor = MaterialTheme.colorScheme.primary,
    selectedTextColor = MaterialTheme.colorScheme.primary,
    indicatorColor = MaterialTheme.colorScheme.primaryContainer,
    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
    unselectedTextColor = MaterialTheme.colorScheme.onSurfaceVariant
)
