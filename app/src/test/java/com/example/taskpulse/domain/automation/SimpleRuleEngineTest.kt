package com.example.taskpulse.domain.automation

import com.example.taskpulse.domain.model.AutomationAction
import com.example.taskpulse.domain.model.AutomationRule
import com.example.taskpulse.domain.model.AutomationTrigger
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import org.junit.Assert.assertEquals
import org.junit.Test

class SimpleRuleEngineTest {
    private val engine = SimpleRuleEngine()

    @Test
    fun `matches overdue task when rule is not completed trigger`() {
        val now = 1_000_000L
        val rule = AutomationRule(
            id = 1L,
            name = "overdue reminder",
            enabled = true,
            trigger = AutomationTrigger.TASK_NOT_COMPLETED,
            action = AutomationAction.SEND_NOTIFICATION
        )
        val task = sampleTask(
            id = 10L,
            status = TaskStatus.PENDING,
            dueAtMillis = now - 1_000L,
            updatedAtMillis = now
        )

        val result = engine.evaluate(listOf(rule), listOf(task), now)

        assertEquals(1, result.size)
        assertEquals(1L, result.first().ruleId)
        assertEquals(10L, result.first().taskId)
    }

    @Test
    fun `does not match overdue trigger when task is completed`() {
        val now = 1_000_000L
        val rule = AutomationRule(
            id = 2L,
            name = "overdue completed guard",
            enabled = true,
            trigger = AutomationTrigger.TASK_NOT_COMPLETED,
            action = AutomationAction.SEND_NOTIFICATION
        )
        val task = sampleTask(
            id = 11L,
            status = TaskStatus.COMPLETED,
            dueAtMillis = now - 5_000L,
            updatedAtMillis = now
        )

        val result = engine.evaluate(listOf(rule), listOf(task), now)

        assertEquals(0, result.size)
    }

    private fun sampleTask(
        id: Long,
        status: TaskStatus,
        dueAtMillis: Long?,
        updatedAtMillis: Long
    ): Task = Task(
        id = id,
        categoryId = 1L,
        title = "task-$id",
        description = "",
        status = status,
        priority = TaskPriority.MEDIUM,
        dueAtMillis = dueAtMillis,
        recurrence = null,
        createdAtMillis = updatedAtMillis,
        updatedAtMillis = updatedAtMillis
    )
}
