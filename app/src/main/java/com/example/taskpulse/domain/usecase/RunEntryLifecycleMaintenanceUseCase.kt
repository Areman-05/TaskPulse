package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.calendar.TaskCalendarDates
import com.example.taskpulse.domain.lifecycle.EntryLifecyclePolicy
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.model.isTaskItem
import com.example.taskpulse.domain.repository.TaskRepository
import java.time.temporal.ChronoUnit

data class EntryLifecycleResult(
    val autoCompleted: Int = 0,
    val archived: Int = 0,
    val evictedFromArchive: Int = 0
)

class RunEntryLifecycleMaintenanceUseCase(
    private val repository: TaskRepository,
    private val completeTaskAndStopRemindersUseCase: CompleteTaskAndStopRemindersUseCase
) {
    suspend operator fun invoke(nowMillis: Long = System.currentTimeMillis()): EntryLifecycleResult {
        val today = TaskCalendarDates.today()
        var autoCompleted = 0
        var archived = 0
        val all = repository.listAllTasks()
        for (task in all) {
            if (task.archivedAtMillis != null) continue
            if (task.isTaskItem && shouldAutoComplete(task, today)) {
                if (task.status != TaskStatus.COMPLETED) {
                    completeTaskAndStopRemindersUseCase(task.id, nowMillis)
                    autoCompleted++
                }
            }
            if (shouldArchive(task, today, nowMillis)) {
                val before = repository.countArchived()
                repository.archiveTask(task.id, nowMillis)
                archived++
                if (before >= EntryLifecyclePolicy.MAX_ARCHIVED_ENTRIES) {
                    // La más antigua fue sustituida al archivar la nueva.
                }
            }
        }

        return EntryLifecycleResult(
            autoCompleted = autoCompleted,
            archived = archived,
            evictedFromArchive = 0
        )
    }

    private fun shouldAutoComplete(task: Task, today: java.time.LocalDate): Boolean {
        val due = task.dueAtMillis ?: return false
        if (!TaskCalendarDates.hasCalendarDate(task)) return false
        return TaskCalendarDates.toLocalDate(due).isBefore(today) &&
            task.status != TaskStatus.COMPLETED
    }

    private fun shouldArchive(
        task: Task,
        today: java.time.LocalDate,
        nowMillis: Long
    ): Boolean {
        val due = task.dueAtMillis
        if (due != null && TaskCalendarDates.hasCalendarDate(task)) {
            val dueDay = TaskCalendarDates.toLocalDate(due)
            val daysSince = ChronoUnit.DAYS.between(dueDay, today)
            return daysSince >= EntryLifecyclePolicy.ARCHIVE_DAYS_AFTER_DUE
        }
        val ageDays = ChronoUnit.DAYS.between(
            TaskCalendarDates.toLocalDate(task.createdAtMillis),
            today
        )
        return ageDays >= EntryLifecyclePolicy.ARCHIVE_UNDATED_AFTER_DAYS
    }
}
