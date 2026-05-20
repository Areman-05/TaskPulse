package com.example.taskpulse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.example.taskpulse.data.local.entity.TaskEntity
import com.example.taskpulse.data.local.entity.TaskHistoryEntity
import com.example.taskpulse.data.local.model.DailyCompletionCount
import com.example.taskpulse.data.local.relation.TaskWithDetailsEntity
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks WHERE archivedAtMillis IS NULL ORDER BY updatedAtMillis DESC")
    fun observeTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks ORDER BY updatedAtMillis DESC")
    fun observeAllTasks(): Flow<List<TaskEntity>>

    @Query(
        "SELECT * FROM tasks WHERE archivedAtMillis IS NOT NULL ORDER BY archivedAtMillis DESC"
    )
    fun observeArchivedTasks(): Flow<List<TaskEntity>>

    @Query("SELECT * FROM tasks")
    suspend fun listTasks(): List<TaskEntity>

    @Query("SELECT COUNT(*) FROM tasks WHERE archivedAtMillis IS NOT NULL")
    suspend fun countArchived(): Int

    @Query(
        """
        SELECT * FROM tasks
        WHERE archivedAtMillis IS NOT NULL
        ORDER BY archivedAtMillis ASC
        LIMIT :limit
        """
    )
    suspend fun listArchivedOldest(limit: Int): List<TaskEntity>

    @Query(
        """
        UPDATE tasks
        SET archivedAtMillis = :archivedAtMillis, updatedAtMillis = :updatedAtMillis
        WHERE id = :taskId
        """
    )
    suspend fun archiveTask(taskId: Long, archivedAtMillis: Long, updatedAtMillis: Long)

    @Query(
        """
        UPDATE tasks
        SET archivedAtMillis = NULL, updatedAtMillis = :updatedAtMillis
        WHERE id = :taskId
        """
    )
    suspend fun restoreTask(taskId: Long, updatedAtMillis: Long)

    @Query("SELECT * FROM tasks WHERE blockedByTaskId = :blockerTaskId")
    suspend fun listTasksBlockedBy(blockerTaskId: Long): List<TaskEntity>

    @Transaction
    @Query("SELECT * FROM tasks WHERE id = :taskId")
    fun observeTaskDetails(taskId: Long): Flow<TaskWithDetailsEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertTask(task: TaskEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertHistory(entry: TaskHistoryEntity)

    @Query("SELECT * FROM tasks WHERE id = :taskId LIMIT 1")
    suspend fun getTask(taskId: Long): TaskEntity?

    @Query("UPDATE tasks SET status = :status, updatedAtMillis = :updatedAtMillis WHERE id = :taskId")
    suspend fun updateTaskStatus(taskId: Long, status: TaskStatus, updatedAtMillis: Long)

    @Query("UPDATE tasks SET dueAtMillis = :dueAtMillis, updatedAtMillis = :updatedAtMillis WHERE id = :taskId")
    suspend fun updateDueDate(taskId: Long, dueAtMillis: Long, updatedAtMillis: Long)

    @Query(
        """
        SELECT ((updatedAtMillis / 86400000) * 86400000) AS dayStartMillis, COUNT(*) AS completedCount
        FROM tasks
        WHERE status = :completedStatus
        GROUP BY dayStartMillis
        ORDER BY dayStartMillis DESC
        LIMIT :limit
        """
    )
    fun observeDailyCompletions(completedStatus: TaskStatus, limit: Int): Flow<List<DailyCompletionCount>>

    @Query(
        """
        SELECT COUNT(*) FROM tasks
        WHERE status != :completed AND archivedAtMillis IS NULL
        """
    )
    suspend fun countTasksNotCompleted(completed: TaskStatus): Int

    @Query("DELETE FROM tasks WHERE id IN (:taskIds)")
    suspend fun deleteTasks(taskIds: List<Long>)

    @Query(
        """
        UPDATE tasks
        SET priority = :priority, updatedAtMillis = :updatedAtMillis
        WHERE id IN (:taskIds)
        """
    )
    suspend fun updateTasksPriority(
        taskIds: List<Long>,
        priority: TaskPriority,
        updatedAtMillis: Long
    )
}
