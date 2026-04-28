package com.example.taskpulse.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.taskpulse.domain.model.TaskStatus

@Entity(
    tableName = "task_history",
    foreignKeys = [
        ForeignKey(
            entity = TaskEntity::class,
            parentColumns = ["id"],
            childColumns = ["taskId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("taskId")]
)
data class TaskHistoryEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val taskId: Long,
    val fromStatus: TaskStatus?,
    val toStatus: TaskStatus,
    val changedAtMillis: Long,
    val reason: String?
)
