package com.example.taskpulse.data.local.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus

@Entity(
    tableName = "tasks",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("categoryId")]
)
data class TaskEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val categoryId: Long,
    val title: String,
    val description: String,
    val status: TaskStatus,
    val priority: TaskPriority,
    val dueAtMillis: Long?,
    val recurrenceInterval: Int?,
    val recurrenceUnit: String?,
    val createdAtMillis: Long,
    val updatedAtMillis: Long
)
