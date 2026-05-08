package com.example.taskpulse.data.mapper

import com.example.taskpulse.domain.model.AutomationAction
import com.example.taskpulse.domain.model.AutomationRule
import com.example.taskpulse.domain.model.AutomationTrigger
import org.junit.Assert.assertEquals
import org.junit.Test

class AutomationMapperTest {
    @Test
    fun `toEntity keeps trigger and action values`() {
        val rule = AutomationRule(
            id = 99L,
            name = "rule",
            enabled = true,
            trigger = AutomationTrigger.TASK_FAILED,
            action = AutomationAction.MARK_AS_FAILED,
            thresholdDays = 3
        )

        val entity = rule.toEntity()

        assertEquals(AutomationTrigger.TASK_FAILED, entity.trigger)
        assertEquals(AutomationAction.MARK_AS_FAILED, entity.action)
    }
}
