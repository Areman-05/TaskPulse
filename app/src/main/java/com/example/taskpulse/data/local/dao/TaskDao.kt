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
import com.example.taskpulse.domain.model.TaskStatus
import kotlinx.coroutines.flow.Flow

@Dao
interface TaskDao {
    @Query("SELECT * FROM tasks ORDER BY updatedAtMillis DESC")
    fun observeTasks(): Flow<List<TaskEntity>>

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
}
