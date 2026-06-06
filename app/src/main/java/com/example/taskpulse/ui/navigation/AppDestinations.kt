package com.example.taskpulse.ui.navigation

import com.example.taskpulse.domain.model.TaskEntryType
import java.time.LocalDate

object AppDestinations {
    const val TASKS_ROUTE = "tasks"
    const val CALENDAR_ROUTE = "calendar"
    const val CREATE_ROUTE = "create?scheduleDate={scheduleDate}&entryType={entryType}"
    const val SETTINGS_ROUTE = "settings"
    const val ARCHIVE_ROUTE = "archive"
    const val ENTRY_DETAIL_ROUTE = "entry_detail/{entryId}"

    const val ARG_SCHEDULE_DATE = "scheduleDate"
    const val ARG_ENTRY_TYPE = "entryType"

    fun entryDetailRoute(entryId: Long): String = "entry_detail/$entryId"

    fun createRoute(
        scheduleDate: LocalDate? = null,
        entryType: TaskEntryType? = null
    ): String {
        val params = buildList {
            if (scheduleDate != null) add("$ARG_SCHEDULE_DATE=$scheduleDate")
            if (entryType != null) add("$ARG_ENTRY_TYPE=${entryType.name}")
        }
        return if (params.isEmpty()) "create" else "create?${params.joinToString("&")}"
    }
}
