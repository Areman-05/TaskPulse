package com.example.taskpulse

import android.app.Application
import com.example.taskpulse.core.AppContainer

class TaskPulseApp : Application() {
    lateinit var container: AppContainer
        private set

    override fun onCreate() {
        super.onCreate()
        container = AppContainer(this)
    }
}
