package com.example.taskpulse.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.taskpulse.data.local.entity.CategoryEntity

@Dao
interface CategoryDao {
    @Query("SELECT COUNT(*) FROM categories")
    suspend fun countCategories(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCategory(category: CategoryEntity)
}
