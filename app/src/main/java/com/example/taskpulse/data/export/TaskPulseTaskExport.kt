package com.example.taskpulse.data.export

import com.example.taskpulse.domain.model.Task
import org.json.JSONArray
import org.json.JSONObject
import java.io.File

object TaskPulseTaskExport {

    fun writeJsonSnapshot(tasks: List<Task>, target: File) {
        val array = JSONArray()
        tasks.forEach { task ->
            array.put(
                JSONObject().apply {
                    put("id", task.id)
                    put("categoryId", task.categoryId)
                    put("title", task.title)
                    put("description", task.description)
                    put("status", task.status.name)
                    put("priority", task.priority.name)
                    task.dueAtMillis?.let { put("dueAtMillis", it) }
                    task.recurrence?.let { rec ->
                        put(
                            "recurrence",
                            JSONObject().apply {
                                put("interval", rec.interval)
                                put("unit", rec.unit.name)
                            }
                        )
                    }
                    task.blockedByTaskId?.let { put("blockedByTaskId", it) }
                    put("createdAtMillis", task.createdAtMillis)
                    put("updatedAtMillis", task.updatedAtMillis)
                }
            )
        }
        val root = JSONObject().apply {
            put("schema", 1)
            put("tasks", array)
        }
        target.writeText(root.toString(2))
    }

    fun writeCsvSnapshot(tasks: List<Task>, target: File) {
        val header =
            "id,categoryId,title,description,status,priority,dueAtMillis,blockedBy,createdAt,updatedAt"
        val lines = tasks.joinToString("\n") { task ->
            listOf(
                task.id,
                task.categoryId,
                csvCell(task.title),
                csvCell(task.description),
                task.status.name,
                task.priority.name,
                task.dueAtMillis?.toString().orEmpty(),
                task.blockedByTaskId?.toString().orEmpty(),
                task.createdAtMillis,
                task.updatedAtMillis
            ).joinToString(",")
        }
        target.writeText("$header\n$lines")
    }

    private fun csvCell(value: String): String {
        val escaped = value.replace("\"", "\"\"")
        return "\"$escaped\""
    }

    fun copyDatabase(sourceDb: File, target: File) {
        sourceDb.inputStream().use { input ->
            target.outputStream().use { output ->
                input.copyTo(output)
            }
        }
    }
}
