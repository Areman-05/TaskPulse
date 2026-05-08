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

    @Test
    fun `matches stale trigger when task exceeds threshold days`() {
        val now = 20L * DAY_MS
        val rule = AutomationRule(
            id = 3L,
            name = "stale rule",
            enabled = true,
            trigger = AutomationTrigger.TASK_STALE_DAYS,
            action = AutomationAction.MARK_AS_IN_PROGRESS,
            thresholdDays = 5
        )
        val task = sampleTask(
            id = 12L,
            status = TaskStatus.PENDING,
            dueAtMillis = null,
            updatedAtMillis = now - 6L * DAY_MS
        )

        val result = engine.evaluate(listOf(rule), listOf(task), now)

        assertEquals(1, result.size)
        assertEquals(3L, result.first().ruleId)
    }

    @Test
    fun `matches failed trigger only for failed tasks`() {
        val now = 5_000L
        val rule = AutomationRule(
            id = 4L,
            name = "failed watcher",
            enabled = true,
            trigger = AutomationTrigger.TASK_FAILED,
            action = AutomationAction.SEND_NOTIFICATION
        )
        val failed = sampleTask(
            id = 13L,
            status = TaskStatus.FAILED,
            dueAtMillis = null,
            updatedAtMillis = now
        )
        val active = sampleTask(
            id = 14L,
            status = TaskStatus.IN_PROGRESS,
            dueAtMillis = null,
            updatedAtMillis = now
        )

        val result = engine.evaluate(listOf(rule), listOf(failed, active), now)

        assertEquals(1, result.size)
        assertEquals(13L, result.first().taskId)
    }

    @Test
    fun `ignores disabled rules during evaluation`() {
        val now = 1_000_000L
        val disabledRule = AutomationRule(
            id = 5L,
            name = "disabled overdue",
            enabled = false,
            trigger = AutomationTrigger.TASK_NOT_COMPLETED,
            action = AutomationAction.SEND_NOTIFICATION
        )
        val task = sampleTask(
            id = 20L,
            status = TaskStatus.PENDING,
            dueAtMillis = now - 1_000L,
            updatedAtMillis = now
        )

        val result = engine.evaluate(listOf(disabledRule), listOf(task), now)

        assertEquals(0, result.size)
    }

    @Test
    fun `stale trigger requires threshold to produce matches`() {
        val now = 20L * DAY_MS
        val invalidRule = AutomationRule(
            id = 6L,
            name = "stale without threshold",
            enabled = true,
            trigger = AutomationTrigger.TASK_STALE_DAYS,
            action = AutomationAction.MARK_AS_IN_PROGRESS,
            thresholdDays = null
        )
        val task = sampleTask(
            id = 21L,
            status = TaskStatus.PENDING,
            dueAtMillis = null,
            updatedAtMillis = now - 30L * DAY_MS
        )

        val result = engine.evaluate(listOf(invalidRule), listOf(task), now)

        assertEquals(0, result.size)
    }

    @Test
    fun `multiple rules can match the same task independently`() {
        val now = 100L * DAY_MS
        val overdue = AutomationRule(
            id = 7L,
            name = "overdue",
            enabled = true,
            trigger = AutomationTrigger.TASK_NOT_COMPLETED,
            action = AutomationAction.SEND_NOTIFICATION
        )
        val stale = AutomationRule(
            id = 8L,
            name = "stale",
            enabled = true,
            trigger = AutomationTrigger.TASK_STALE_DAYS,
            action = AutomationAction.MARK_AS_IN_PROGRESS,
            thresholdDays = 2
        )
        val task = sampleTask(
            id = 50L,
            status = TaskStatus.PENDING,
            dueAtMillis = now - 1_000L,
            updatedAtMillis = now - 5L * DAY_MS
        )

        val result = engine.evaluate(listOf(overdue, stale), listOf(task), now)

        assertEquals(2, result.size)
    }

    @Test
    fun `overdue trigger requires due date to be set`() {
        val now = 100_000L
        val rule = AutomationRule(
            id = 9L,
            name = "overdue without due",
            enabled = true,
            trigger = AutomationTrigger.TASK_NOT_COMPLETED,
            action = AutomationAction.SEND_NOTIFICATION
        )
        val task = sampleTask(
            id = 51L,
            status = TaskStatus.PENDING,
            dueAtMillis = null,
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

    private companion object {
        const val DAY_MS = 24L * 60L * 60L * 1000L
    }
}
