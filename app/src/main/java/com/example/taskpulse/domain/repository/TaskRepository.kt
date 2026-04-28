package com.example.taskpulse.domain.repository

import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskDetails
import kotlinx.coroutines.flow.Flow

interface TaskRepository {
    fun observeTasks(): Flow<List<Task>>
    fun observeTaskDetails(taskId: Long): Flow<TaskDetails?>
    suspend fun upsertTask(task: Task): Long
}
