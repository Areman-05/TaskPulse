package com.example.taskpulse.domain.usecase

import com.example.taskpulse.domain.model.DailyProductivityPoint
import com.example.taskpulse.domain.repository.TaskRepository
import kotlinx.coroutines.flow.Flow

class ObserveDailyProductivityUseCase(
    private val repository: TaskRepository
) {
    operator fun invoke(limit: Int = 7): Flow<List<DailyProductivityPoint>> {
        return repository.observeDailyProductivity(limit)
    }
}
