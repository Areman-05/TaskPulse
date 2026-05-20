package com.example.taskpulse.domain.repository

import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.DailyProductivityPoint
import com.example.taskpulse.domain.model.TaskDetails
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    fun observeAllTasks(): Flow<List<Task>>
    fun observeArchivedTasks(): Flow<List<Task>>
    suspend fun listTasks(): List<Task>
    suspend fun listAllTasks(): List<Task>
    suspend fun getTask(taskId: Long): Task?
    suspend fun listTasksBlockedBy(blockerTaskId: Long): List<Task>
    fun observeDailyProductivity(limit: Int): Flow<List<DailyProductivityPoint>>
    fun observeTaskDetails(taskId: Long): Flow<TaskDetails?>
    suspend fun upsertTask(task: Task): Long
    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus, updatedAtMillis: Long)
    suspend fun updateTaskDueDate(taskId: Long, dueAtMillis: Long, updatedAtMillis: Long)
    suspend fun transitionTaskStatus(
        taskId: Long,
        to: TaskStatus,
        nowMillis: Long,
        reason: String?
    )

    suspend fun deleteTasks(taskIds: List<Long>)

    suspend fun archiveTask(taskId: Long, nowMillis: Long)

    suspend fun restoreTask(taskId: Long, nowMillis: Long)

    suspend fun countArchived(): Int

    suspend fun updateTasksPriority(
        taskIds: List<Long>,
        priority: TaskPriority,
        updatedAtMillis: Long
    )
}
