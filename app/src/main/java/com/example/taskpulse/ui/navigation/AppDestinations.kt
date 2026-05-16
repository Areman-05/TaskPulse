package com.example.taskpulse.ui.navigation

object AppDestinations {
    const val TASKS_ROUTE = "tasks"
    const val CREATE_ROUTE = "create"
    const val INSIGHTS_ROUTE = "insights"
    const val SETTINGS_ROUTE = "settings"
    const val ENTRY_DETAIL_ROUTE = "entry_detail/{entryId}"

    fun entryDetailRoute(entryId: Long): String = "entry_detail/$entryId"
}
