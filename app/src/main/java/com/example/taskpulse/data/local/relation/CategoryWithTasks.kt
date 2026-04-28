package com.example.taskpulse.data.local.relation

import androidx.room.Embedded
import androidx.room.Relation
import com.example.taskpulse.data.local.entity.CategoryEntity
import com.example.taskpulse.data.local.entity.TaskEntity

data class CategoryWithTasks(
    @Embedded val category: CategoryEntity,
    @Relation(
        parentColumn = "id",
        entityColumn = "categoryId"
    )
    val tasks: List<TaskEntity>
)
