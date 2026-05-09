package com.example.taskpulse.data.export

import com.example.taskpulse.domain.repository.TaskRepository
import java.io.File

class TaskSnapshotFileExporter(
    private val repository: TaskRepository,
    private val filesDir: File
) {
    private val exportDir: File
        get() = File(filesDir, "exports").also { it.mkdirs() }

    suspend fun writeJson(): File {
        val target = File(exportDir, "taskpulse-tasks-${System.currentTimeMillis()}.json")
        TaskPulseTaskExport.writeJsonSnapshot(repository.listTasks(), target)
        return target
    }

    suspend fun writeCsv(): File {
        val target = File(exportDir, "taskpulse-tasks-${System.currentTimeMillis()}.csv")
        TaskPulseTaskExport.writeCsvSnapshot(repository.listTasks(), target)
        return target
    }

    suspend fun writeDatabaseBackup(sourceDbFile: File): File {
        val target = File(exportDir, "taskpulse-backup-${System.currentTimeMillis()}.db")
        TaskPulseTaskExport.copyDatabase(sourceDbFile, target)
        return target
    }
}
