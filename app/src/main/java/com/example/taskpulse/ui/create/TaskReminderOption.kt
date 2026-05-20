package com.example.taskpulse.ui.create

import androidx.annotation.StringRes
import com.example.taskpulse.R
import kotlin.math.abs

data class TaskReminderOption(
    val minutes: Int,
    @StringRes val labelRes: Int
)

val TaskReminderIntervals: List<TaskReminderOption> = listOf(
    TaskReminderOption(15, R.string.create_reminder_15min),
    TaskReminderOption(30, R.string.create_reminder_30min),
    TaskReminderOption(60, R.string.create_reminder_1h),
    TaskReminderOption(120, R.string.create_reminder_2h),
    TaskReminderOption(24 * 60, R.string.create_reminder_1d)
)

fun closestReminderMinutes(dueAtMillis: Long?, createdAtMillis: Long): Int {
    if (dueAtMillis == null) return 30
    val diffMinutes = ((dueAtMillis - createdAtMillis) / 60_000L).toInt().coerceAtLeast(1)
    return TaskReminderIntervals
        .minByOrNull { abs(it.minutes - diffMinutes) }
        ?.minutes
        ?: 30
}

