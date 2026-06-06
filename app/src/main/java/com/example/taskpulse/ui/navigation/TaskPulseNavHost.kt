package com.example.taskpulse.ui.navigation

import android.app.Activity
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.material.icons.outlined.CalendarToday
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.TaskAlt
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalView
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
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
import com.example.taskpulse.ui.theme.StitchTypography
import com.example.taskpulse.ui.theme.TaskPulseColors
import com.example.taskpulse.domain.model.TaskEntryType
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

    val view = LocalView.current
    val backgroundColor = MaterialTheme.colorScheme.background
    SideEffect {
        if (!view.isInEditMode) {
            val window = (view.context as Activity).window
            window.navigationBarColor = if (showBottomBar) {
                TaskPulseColors.SurfaceContainer.toArgb()
            } else {
                backgroundColor.toArgb()
            }
        }
    }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        bottomBar = {
            if (showBottomBar) {
                StitchBottomNavBar(
                    currentRoute = currentRoute,
                    onTasksClick = {
                        navController.navigate(AppDestinations.TASKS_ROUTE) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onCalendarClick = {
                        navController.navigate(AppDestinations.CALENDAR_ROUTE) {
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    },
                    onSettingsClick = {
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
                    onNavigateToCreateNote = {
                        navController.navigate(
                            AppDestinations.createRoute(entryType = TaskEntryType.NOTE)
                        )
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
                    },
                    navArgument(AppDestinations.ARG_ENTRY_TYPE) {
                        type = NavType.StringType
                        nullable = true
                        defaultValue = null
                    }
                )
            ) { backStackEntry ->
                val scheduleDateArg = backStackEntry.arguments
                    ?.getString(AppDestinations.ARG_SCHEDULE_DATE)
                    ?.takeIf { it.isNotBlank() }
                val entryTypeArg = backStackEntry.arguments
                    ?.getString(AppDestinations.ARG_ENTRY_TYPE)
                    ?.takeIf { it.isNotBlank() }
                val initialScheduleDate = scheduleDateArg?.let(LocalDate::parse)
                val initialEntryType = entryTypeArg?.let { name ->
                    runCatching { TaskEntryType.valueOf(name) }.getOrNull()
                } ?: TaskEntryType.TASK
                val vm: CreateTaskViewModel = viewModel(
                    factory = CreateTaskViewModel.Factory(
                        application = appContext as android.app.Application,
                        createDefaultTaskUseCase = container.createDefaultTaskUseCase,
                        upsertTaskUseCase = container.upsertTaskUseCase,
                        scheduleTaskReminderUseCase = container.scheduleTaskReminderUseCase,
                        initialScheduleDate = initialScheduleDate,
                        initialEntryType = initialEntryType
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
private fun StitchBottomNavBar(
    currentRoute: String,
    onTasksClick: () -> Unit,
    onCalendarClick: () -> Unit,
    onSettingsClick: () -> Unit
) {
    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 12.dp, topEnd = 12.dp),
        color = TaskPulseColors.SurfaceContainer,
        tonalElevation = 0.dp,
        shadowElevation = 0.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .navigationBarsPadding()
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(80.dp)
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.SpaceAround,
                verticalAlignment = Alignment.CenterVertically
            ) {
                StitchNavItem(
                    selected = currentRoute == AppDestinations.TASKS_ROUTE,
                    icon = Icons.Outlined.TaskAlt,
                    selectedIcon = Icons.Filled.TaskAlt,
                    label = stringResource(R.string.nav_tasks),
                    onClick = onTasksClick
                )
                StitchNavItem(
                    selected = currentRoute == AppDestinations.CALENDAR_ROUTE,
                    icon = Icons.Outlined.CalendarToday,
                    selectedIcon = Icons.Outlined.CalendarToday,
                    label = stringResource(R.string.nav_calendar),
                    onClick = onCalendarClick
                )
                StitchNavItem(
                    selected = currentRoute == AppDestinations.SETTINGS_ROUTE,
                    icon = Icons.Outlined.Settings,
                    selectedIcon = Icons.Outlined.Settings,
                    label = stringResource(R.string.nav_settings),
                    onClick = onSettingsClick
                )
            }
        }
    }
}

@Composable
private fun StitchNavItem(
    selected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    selectedIcon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val pressed by interactionSource.collectIsPressedAsState()
    val scale = if (pressed) 0.9f else 1f

    val iconBackground = when {
        selected -> TaskPulseColors.SecondaryContainer
        pressed -> TaskPulseColors.BronzeMuted
        else -> Color.Transparent
    }
    val contentColor = when {
        selected -> TaskPulseColors.Primary
        pressed -> TaskPulseColors.Bronze
        else -> TaskPulseColors.OnSurfaceVariant
    }

    Column(
        modifier = Modifier
            .scale(scale)
            .semantics {
                role = Role.Tab
                this.selected = selected
                contentDescription = label
            }
            .clickable(
                interactionSource = interactionSource,
                indication = null,
                onClick = onClick
            )
            .padding(horizontal = 4.dp, vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Pill solo detrás del icono (M3 / captura Stitch), no alrededor del label
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(50))
                .background(iconBackground)
                .padding(horizontal = 20.dp, vertical = 4.dp),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = if (selected) selectedIcon else icon,
                contentDescription = null,
                tint = contentColor,
                modifier = Modifier.size(24.dp)
            )
        }
        Text(
            text = label,
            style = StitchTypography.labelLg,
            color = when {
                selected -> TaskPulseColors.OnSecondaryContainer
                pressed -> TaskPulseColors.Bronze
                else -> TaskPulseColors.OnSurfaceVariant
            },
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
