package com.example.taskpulse.di

import android.content.Context
import androidx.room.Room
import com.example.taskpulse.data.local.TaskPulseDatabase
import com.example.taskpulse.data.local.dao.TaskDao
import com.example.taskpulse.data.repository.OfflineTaskRepository
import com.example.taskpulse.domain.repository.TaskRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {
    @Provides
    @Singleton
    fun provideTaskPulseDatabase(@ApplicationContext context: Context): TaskPulseDatabase =
        Room.databaseBuilder(
            context,
            TaskPulseDatabase::class.java,
            "taskpulse.db"
        ).build()

    @Provides
    fun provideTaskDao(database: TaskPulseDatabase): TaskDao = database.taskDao()
}

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {
    @Binds
    abstract fun bindTaskRepository(impl: OfflineTaskRepository): TaskRepository
}
