package com.example.taskpulse.data.repository

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskpulse.data.local.TaskPulseDatabase
import com.example.taskpulse.data.local.entity.CategoryEntity
import com.example.taskpulse.domain.model.TaskEntryType
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.usecase.CountPendingTasksUseCase
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class OfflineTaskRepositoryInstrumentedTest {

    private lateinit var db: TaskPulseDatabase
    private lateinit var repository: OfflineTaskRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        db = Room.inMemoryDatabaseBuilder(context, TaskPulseDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        runTest {
            db.categoryDao().upsertCategory(
                CategoryEntity(id = 1L, name = "General", colorHex = "#6750A4")
            )
        }
        repository = OfflineTaskRepository(db.taskDao())
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun upsertAndListTasks_persistsActiveTask() = runTest {
        val task = sampleTask(id = 0L, title = "Persistida")
        val id = repository.upsertTask(task)

        val listed = repository.listTasks()

        assertEquals(1, listed.size)
        assertEquals(id, listed.first().id)
        assertEquals("Persistida", listed.first().title)
    }

    @Test
    fun archiveTask_movesOutOfActiveList() = runTest {
        val id = repository.upsertTask(sampleTask(id = 0L))
        repository.archiveTask(id, nowMillis = 50L)

        assertEquals(0, repository.listTasks().size)
        assertEquals(1, repository.countArchived())
    }

    @Test
    fun countPendingTasks_matchesDaoSemantics() = runTest {
        repository.upsertTask(sampleTask(id = 0L, status = TaskStatus.PENDING))
        repository.upsertTask(sampleTask(id = 0L, title = "Hecha", status = TaskStatus.COMPLETED))

        val count = CountPendingTasksUseCase(repository).invoke()

        assertEquals(1, count)
    }

    @Test
    fun deleteTasks_removesFromDatabase() = runTest {
        val id = repository.upsertTask(sampleTask(id = 0L, entryType = TaskEntryType.NOTE))
        repository.deleteTasks(listOf(id))

        assertEquals(null, repository.getTask(id))
    }

    private fun sampleTask(
        id: Long = 0L,
        title: String = "Test",
        status: TaskStatus = TaskStatus.PENDING,
        entryType: TaskEntryType = TaskEntryType.TASK
    ): Task = Task(
        id = id,
        categoryId = 1L,
        title = title,
        description = "",
        status = status,
        priority = TaskPriority.MEDIUM,
        dueAtMillis = null,
        recurrence = null,
        createdAtMillis = 1L,
        updatedAtMillis = 1L,
        entryType = entryType
    )
}
