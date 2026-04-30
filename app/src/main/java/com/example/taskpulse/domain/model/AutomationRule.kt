package com.example.taskpulse.domain.model

data class AutomationRule(
    val id: Long,
    val name: String,
    val enabled: Boolean,
    val trigger: AutomationTrigger,
    val action: AutomationAction,
    val thresholdDays: Int? = null
)
