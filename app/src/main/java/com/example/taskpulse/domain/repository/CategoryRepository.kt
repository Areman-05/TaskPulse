package com.example.taskpulse.domain.repository

interface CategoryRepository {
    suspend fun ensureDefaultInbox()
}
