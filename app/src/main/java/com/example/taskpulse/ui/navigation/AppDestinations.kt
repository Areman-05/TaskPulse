package com.example.taskpulse.ui.navigation

import java.time.LocalDate

object AppDestinations {
    const val TASKS_ROUTE = "tasks"
    const val CALENDAR_ROUTE = "calendar"
    const val CREATE_ROUTE = "create?scheduleDate={scheduleDate}"
    const val SETTINGS_ROUTE = "settings"
    const val ARCHIVE_ROUTE = "archive"
    const val ENTRY_DETAIL_ROUTE = "entry_detail/{entryId}"

    const val ARG_SCHEDULE_DATE = "scheduleDate"

    fun entryDetailRoute(entryId: Long): String = "entry_detail/$entryId"

    fun createRoute(scheduleDate: LocalDate? = null): String =
        if (scheduleDate != null) {
            "create?scheduleDate=$scheduleDate"
        } else {
            "create"
        }
}
