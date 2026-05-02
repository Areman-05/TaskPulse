package com.example.taskpulse.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.taskpulse.data.local.converter.AutomationConverters
import com.example.taskpulse.data.local.converter.TaskConverters
import com.example.taskpulse.data.local.dao.AutomationDao
import com.example.taskpulse.data.local.dao.CategoryDao
import com.example.taskpulse.data.local.dao.TaskDao
import com.example.taskpulse.data.local.entity.AutomationRuleEntity
import com.example.taskpulse.data.local.entity.CategoryEntity
import com.example.taskpulse.data.local.entity.SubtaskEntity
import com.example.taskpulse.data.local.entity.TaskEntity
import com.example.taskpulse.data.local.entity.TaskHistoryEntity

@Database(
    entities = [
        CategoryEntity::class,
        TaskEntity::class,
        SubtaskEntity::class,
        TaskHistoryEntity::class,
        AutomationRuleEntity::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(TaskConverters::class, AutomationConverters::class)
abstract class TaskPulseDatabase : RoomDatabase() {
    abstract fun taskDao(): TaskDao
    abstract fun automationDao(): AutomationDao
    abstract fun categoryDao(): CategoryDao
}
