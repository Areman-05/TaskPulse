package com.example.taskpulse.data.local.converter

import androidx.room.TypeConverter
import com.example.taskpulse.domain.model.AutomationAction
import com.example.taskpulse.domain.model.AutomationTrigger

class AutomationConverters {
    @TypeConverter
    fun toAutomationTrigger(value: String): AutomationTrigger = AutomationTrigger.valueOf(value)

    @TypeConverter
    fun fromAutomationTrigger(value: AutomationTrigger): String = value.name

    @TypeConverter
    fun toAutomationAction(value: String): AutomationAction = AutomationAction.valueOf(value)

    @TypeConverter
    fun fromAutomationAction(value: AutomationAction): String = value.name
}
