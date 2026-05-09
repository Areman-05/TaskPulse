package com.example.taskpulse.domain.metrics

import com.example.taskpulse.domain.model.DailyProductivityPoint
import org.junit.Assert.assertEquals
import org.junit.Test

class ProductivityStreakCalculatorTest {

    @Test
    fun `counts consecutive completion days`() {
        val day = 86_400_000L
        val now = 5 * day + day / 2
        val points = listOf(
            DailyProductivityPoint(dayStartMillis = 5 * day, completedCount = 2),
            DailyProductivityPoint(dayStartMillis = 4 * day, completedCount = 1),
            DailyProductivityPoint(dayStartMillis = 3 * day, completedCount = 1)
        )
        assertEquals(3, ProductivityStreakCalculator.currentStreak(points, now))
    }

    @Test
    fun `streak breaks when most recent anchor day lacks completion bucket`() {
        val day = 86_400_000L
        val now = 7 * day
        val points = listOf(
            DailyProductivityPoint(dayStartMillis = 5 * day, completedCount = 1)
        )
        assertEquals(0, ProductivityStreakCalculator.currentStreak(points, now))
    }
}
