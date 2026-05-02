package com.example.taskpulse.data.repository

import com.example.taskpulse.data.local.dao.CategoryDao
import com.example.taskpulse.data.local.entity.CategoryEntity
import com.example.taskpulse.domain.repository.CategoryRepository

class OfflineCategoryRepository(
    private val categoryDao: CategoryDao
) : CategoryRepository {
    override suspend fun ensureDefaultInbox() {
        if (categoryDao.countCategories() > 0) return
        categoryDao.upsertCategory(
            CategoryEntity(
                id = 1L,
                name = "General",
                colorHex = "#6750A4"
            )
        )
    }
}
