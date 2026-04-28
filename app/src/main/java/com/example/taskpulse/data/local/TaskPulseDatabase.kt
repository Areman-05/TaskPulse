package com.example.taskpulse.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.taskpulse.data.local.converter.TaskConverters
import com.example.taskpulse.data.local.dao.TaskDao
import com.example.taskpulse.data.local.entity.CategoryEntity
import com.example.taskpulse.data.local.entity.SubtaskEntity
import com.example.taskpulse.data.local.entity.TaskEntity
import com.example.taskpulse.data.local.entity.TaskHistoryEntity

@Database(
    entities = [
        CategoryEntity::class,
        TaskEntity::class,
        SubtaskEntity::class,
        TaskHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
@TypeConverters(TaskConverters::class)
abstract class TaskPulseDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
}
