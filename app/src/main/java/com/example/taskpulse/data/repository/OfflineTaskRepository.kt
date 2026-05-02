package com.example.taskpulse.data.repository

import com.example.taskpulse.data.local.dao.TaskDao
import com.example.taskpulse.data.local.entity.TaskHistoryEntity
import com.example.taskpulse.data.mapper.toDomain
import com.example.taskpulse.data.mapper.toEntity
import com.example.taskpulse.domain.model.DailyProductivityPoint
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskDetails
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineTaskRepository(
    private val taskDao: TaskDao
) : TaskRepository {
    override fun observeTasks(): Flow<List<Task>> = taskDao.observeTasks().map { tasks ->
        tasks.map { it.toDomain() }
    }

    override suspend fun listTasks(): List<Task> = taskDao.listTasks().map { it.toDomain() }

    override fun observeDailyProductivity(limit: Int): Flow<List<DailyProductivityPoint>> =
        taskDao.observeDailyCompletions(TaskStatus.COMPLETED, limit).map { rows ->
            rows.map { DailyProductivityPoint(it.dayStartMillis, it.completedCount) }
        }

    override fun observeTaskDetails(taskId: Long): Flow<TaskDetails?> =
        taskDao.observeTaskDetails(taskId).map { details ->
            details?.toDomain()
        }

    override suspend fun upsertTask(task: Task): Long = taskDao.upsertTask(task.toEntity())

    override suspend fun updateTaskStatus(taskId: Long, status: TaskStatus, updatedAtMillis: Long) {
        taskDao.updateTaskStatus(taskId, status, updatedAtMillis)
    }

    override suspend fun updateTaskDueDate(taskId: Long, dueAtMillis: Long, updatedAtMillis: Long) {
        taskDao.updateDueDate(taskId, dueAtMillis, updatedAtMillis)
    }

    override suspend fun transitionTaskStatus(
        taskId: Long,
        to: TaskStatus,
        nowMillis: Long,
        reason: String?
    ) {
        val existing = taskDao.getTask(taskId) ?: return
        if (existing.status == to) return
        taskDao.insertHistory(
            TaskHistoryEntity(
                taskId = taskId,
                fromStatus = existing.status,
                toStatus = to,
                changedAtMillis = nowMillis,
                reason = reason
            )
        )
        taskDao.updateTaskStatus(taskId, to, nowMillis)
    }
}
