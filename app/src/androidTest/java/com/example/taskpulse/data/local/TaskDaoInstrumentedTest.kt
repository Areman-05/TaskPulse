package com.example.taskpulse.data.local

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.taskpulse.data.local.entity.CategoryEntity
import com.example.taskpulse.data.local.entity.TaskEntity
import com.example.taskpulse.data.mapper.toDomain
import com.example.taskpulse.domain.model.TaskEntryType
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class TaskDaoInstrumentedTest {

    private lateinit var db: TaskPulseDatabase
    private var categoryId: Long = 1L

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
            categoryId = 1L
        }
    }

    @After
    fun tearDown() {
        db.close()
    }

    @Test
    fun observeTasks_excludesArchived() = runTest {
        val dao = db.taskDao()
        val activeId = dao.upsertTask(sampleEntity(title = "Activa"))
        dao.upsertTask(sampleEntity(title = "Archivada").copy(archivedAtMillis = 99L))

        val observed = dao.observeTasks().first()

        assertEquals(1, observed.size)
        assertEquals(activeId, observed.first().id)
    }

    @Test
    fun countTasksNotCompleted_ignoresCompletedAndArchived() = runTest {
        val dao = db.taskDao()
        dao.upsertTask(sampleEntity(title = "Pendiente", status = TaskStatus.PENDING))
        dao.upsertTask(sampleEntity(title = "Hecha", status = TaskStatus.COMPLETED))
        dao.upsertTask(
            sampleEntity(title = "Archivada", status = TaskStatus.PENDING)
                .copy(archivedAtMillis = 1L)
        )

        val count = dao.countTasksNotCompleted(TaskStatus.COMPLETED)

        assertEquals(1, count)
    }

    @Test
    fun archiveAndRestore_roundTrip() = runTest {
        val dao = db.taskDao()
        val id = dao.upsertTask(sampleEntity(title = "Para archivar"))
        dao.archiveTask(id, archivedAtMillis = 100L, updatedAtMillis = 100L)

        assertTrue(dao.observeArchivedTasks().first().isNotEmpty())

        dao.restoreTask(id, updatedAtMillis = 200L)
        val restored = dao.getTask(id)!!

        assertEquals(null, restored.archivedAtMillis)
    }

    @Test
    fun mapper_roundTrip_fromEntity() = runTest {
        val dao = db.taskDao()
        val id = dao.upsertTask(
            sampleEntity(title = "Mapper room", entryType = TaskEntryType.NOTE)
        )
        val entity = dao.getTask(id)!!

        val domain = entity.toDomain()

        assertEquals(TaskEntryType.NOTE, domain.entryType)
        assertEquals("Mapper room", domain.title)
    }

    private fun sampleEntity(
        title: String,
        status: TaskStatus = TaskStatus.PENDING,
        entryType: TaskEntryType = TaskEntryType.TASK
    ): TaskEntity = TaskEntity(
        categoryId = categoryId,
        title = title,
        description = "",
        status = status,
        priority = TaskPriority.MEDIUM,
        dueAtMillis = null,
        recurrenceInterval = null,
        recurrenceUnit = null,
        createdAtMillis = 1L,
        updatedAtMillis = 1L,
        entryType = entryType
    )
}
