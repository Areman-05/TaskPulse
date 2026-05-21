package com.example.taskpulse.core

import android.content.Context
import com.example.taskpulse.TaskPulseApp

fun Context.requireAppContainer(): AppContainer =
    (applicationContext as TaskPulseApp).container
