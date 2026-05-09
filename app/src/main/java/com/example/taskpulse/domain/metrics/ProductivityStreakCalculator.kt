package com.example.taskpulse.domain.metrics

import com.example.taskpulse.domain.model.DailyProductivityPoint

private const val DAY_MS = 86_400_000L

/**
 * Consecutive calendar buckets (UTC-aligned like existing completion query) with ≥1 completion.
 */
object ProductivityStreakCalculator {

    fun currentStreak(
        productivityPoints: List<DailyProductivityPoint>,
        nowMillis: Long
    ): Int {
        val completedDays = productivityPoints
            .asSequence()
            .filter { it.completedCount > 0 }
            .map { it.dayStartMillis }
            .toSet()
        if (completedDays.isEmpty()) return 0
        var cursor = (nowMillis / DAY_MS) * DAY_MS
        var streak = 0
        while (completedDays.contains(cursor)) {
            streak++
            cursor -= DAY_MS
        }
        return streak
    }
}
