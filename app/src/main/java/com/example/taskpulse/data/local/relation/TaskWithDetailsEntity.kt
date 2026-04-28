package com.example.taskpulse.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.taskpulse.data.local.entity.SubtaskEntity
import com.example.taskpulse.data.local.entity.TaskEntity
import com.example.taskpulse.data.local.entity.TaskHistoryEntity

data class TaskWithDetailsEntity(
    @Embedded val task: TaskEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val subtasks: List<SubtaskEntity>,
    @Relation(
        parentColumn = "id",
        entityColumn = "taskId"
    )
    val history: List<TaskHistoryEntity>
)
