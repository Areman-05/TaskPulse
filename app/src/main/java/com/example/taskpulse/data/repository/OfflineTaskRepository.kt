package com.example.taskpulse.data.repository

import com.example.taskpulse.data.local.dao.TaskDao
import com.example.taskpulse.data.local.entity.TaskHistoryEntity
import com.example.taskpulse.data.mapper.toDomain
import com.example.taskpulse.data.mapper.toEntity
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskDetails
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.lifecycle.EntryLifecyclePolicy
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class OfflineTaskRepository(
    private val taskDao: TaskDao
) : TaskRepository {
    override fun observeTasks(): Flow<List<Task>> =
        taskDao.observeTasks().map { tasks -> tasks.map { it.toDomain() } }

    override fun observeAllTasks(): Flow<List<Task>> =
        taskDao.observeAllTasks().map { tasks -> tasks.map { it.toDomain() } }

    override fun observeArchivedTasks(): Flow<List<Task>> =
        taskDao.observeArchivedTasks().map { tasks -> tasks.map { it.toDomain() } }

    override suspend fun listTasks(): List<Task> =
        taskDao.listTasks().map { it.toDomain() }.filter { it.archivedAtMillis == null }

    override suspend fun listAllTasks(): List<Task> = taskDao.listTasks().map { it.toDomain() }

    override suspend fun getTask(taskId: Long): Task? = taskDao.getTask(taskId)?.toDomain()

    override suspend fun listTasksBlockedBy(blockerTaskId: Long): List<Task> =
        taskDao.listTasksBlockedBy(blockerTaskId).map { it.toDomain() }

    override suspend fun countPendingTasks(): Int =
        taskDao.countTasksNotCompleted(TaskStatus.COMPLETED)

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

    override suspend fun deleteTasks(taskIds: List<Long>) {
        if (taskIds.isEmpty()) return
        taskDao.deleteTasks(taskIds)
    }

    override suspend fun archiveTask(taskId: Long, nowMillis: Long) {
        val max = EntryLifecyclePolicy.MAX_ARCHIVED_ENTRIES
        val count = taskDao.countArchived()
        if (count >= max) {
            val oldest = taskDao.listArchivedOldest(count - max + 1)
            if (oldest.isNotEmpty()) {
                taskDao.deleteTasks(oldest.map { it.id })
            }
        }
        taskDao.archiveTask(taskId, nowMillis, nowMillis)
    }

    override suspend fun restoreTask(taskId: Long, nowMillis: Long) {
        taskDao.restoreTask(taskId, nowMillis)
    }

    override suspend fun countArchived(): Int = taskDao.countArchived()

    override suspend fun updateTasksPriority(
        taskIds: List<Long>,
        priority: TaskPriority,
        updatedAtMillis: Long
    ) {
        if (taskIds.isEmpty()) return
        taskDao.updateTasksPriority(taskIds, priority, updatedAtMillis)
    }
}
