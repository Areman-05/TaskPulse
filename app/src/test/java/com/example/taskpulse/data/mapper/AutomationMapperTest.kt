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

    @Test
    fun `toDomain preserves nullable threshold`() {
        val rule = AutomationRule(
            id = 77L,
            name = "no-threshold",
            enabled = false,
            trigger = AutomationTrigger.TASK_NOT_COMPLETED,
            action = AutomationAction.SEND_NOTIFICATION,
            thresholdDays = null
        )

        val mapped = rule.toEntity().toDomain()

        assertEquals(null, mapped.thresholdDays)
    }

    @Test
    fun `toDomain preserves id for existing persisted rules`() {
        val rule = AutomationRule(
            id = 1234L,
            name = "existing",
            enabled = true,
            trigger = AutomationTrigger.TASK_STALE_DAYS,
            action = AutomationAction.MARK_AS_IN_PROGRESS,
            thresholdDays = 4
        )

        val mapped = rule.toEntity().toDomain()

        assertEquals(1234L, mapped.id)
    }
}
