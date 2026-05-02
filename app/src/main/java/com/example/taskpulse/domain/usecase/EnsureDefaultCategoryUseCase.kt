package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.repository.CategoryRepository

class EnsureDefaultCategoryUseCase(
    private val categoryRepository: CategoryRepository
) {
    suspend operator fun invoke() {
        categoryRepository.ensureDefaultInbox()
    }
}
