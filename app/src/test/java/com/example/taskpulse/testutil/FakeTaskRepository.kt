package com.example.taskpulse.testutil

import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskDetails
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.update

class FakeTaskRepository(
    initialTasks: List<Task> = emptyList()
) : TaskRepository {
    private val tasks = MutableStateFlow(initialTasks.toMutableList())

    override fun observeTasks(): Flow<List<Task>> =
        tasks.map { list -> list.filter { it.archivedAtMillis == null } }

    override fun observeAllTasks(): Flow<List<Task>> = tasks

    override fun observeArchivedTasks(): Flow<List<Task>> =
        tasks.map { list -> list.filter { it.archivedAtMillis != null } }

    override suspend fun listTasks(): List<Task> =
        tasks.value.filter { it.archivedAtMillis == null }

    override suspend fun listAllTasks(): List<Task> = tasks.value.toList()

    override suspend fun getTask(taskId: Long): Task? = tasks.value.find { it.id == taskId }

    override suspend fun listTasksBlockedBy(blockerTaskId: Long): List<Task> =
        tasks.value.filter { it.blockedByTaskId == blockerTaskId }

    override fun observeTaskDetails(taskId: Long): Flow<TaskDetails?> =
        tasks.map { list ->
            list.find { it.id == taskId }?.let { TaskDetails(it, emptyList(), emptyList()) }
        }

    override suspend fun countPendingTasks(): Int =
        tasks.value.count { it.archivedAtMillis == null && it.status != TaskStatus.COMPLETED }

    override suspend fun upsertTask(task: Task): Long {
        var assignedId = task.id
        tasks.update { list ->
            val mutable = list.toMutableList()
            if (task.id == 0L) {
                assignedId = (mutable.maxOfOrNull { it.id } ?: 0L) + 1L
                mutable.add(task.copy(id = assignedId))
            } else {
                val index = mutable.indexOfFirst { it.id == task.id }
                if (index >= 0) mutable[index] = task else mutable.add(task)
            }
            mutable
        }
        return assignedId
    }

    override suspend fun updateTaskStatus(taskId: Long, status: TaskStatus, updatedAtMillis: Long) {
        updateTask(taskId) { it.copy(status = status, updatedAtMillis = updatedAtMillis) }
    }

    override suspend fun updateTaskDueDate(taskId: Long, dueAtMillis: Long, updatedAtMillis: Long) {
        updateTask(taskId) { it.copy(dueAtMillis = dueAtMillis, updatedAtMillis = updatedAtMillis) }
    }

    override suspend fun transitionTaskStatus(
        taskId: Long,
        to: TaskStatus,
        nowMillis: Long,
        reason: String?
    ) {
        updateTask(taskId) { it.copy(status = to, updatedAtMillis = nowMillis) }
    }

    override suspend fun deleteTasks(taskIds: List<Long>) {
        if (taskIds.isEmpty()) return
        tasks.update { list -> list.filterNot { it.id in taskIds }.toMutableList() }
    }

    override suspend fun archiveTask(taskId: Long, nowMillis: Long) {
        updateTask(taskId) { it.copy(archivedAtMillis = nowMillis, updatedAtMillis = nowMillis) }
    }

    override suspend fun restoreTask(taskId: Long, nowMillis: Long) {
        updateTask(taskId) { it.copy(archivedAtMillis = null, updatedAtMillis = nowMillis) }
    }

    override suspend fun countArchived(): Int =
        tasks.value.count { it.archivedAtMillis != null }

    override suspend fun updateTasksPriority(
        taskIds: List<Long>,
        priority: TaskPriority,
        updatedAtMillis: Long
    ) {
        tasks.update { list ->
            list.map { task ->
                if (task.id in taskIds) task.copy(priority = priority, updatedAtMillis = updatedAtMillis)
                else task
            }.toMutableList()
        }
    }

    private fun updateTask(taskId: Long, transform: (Task) -> Task) {
        tasks.update { list ->
            list.map { if (it.id == taskId) transform(it) else it }.toMutableList()
        }
    }
}
