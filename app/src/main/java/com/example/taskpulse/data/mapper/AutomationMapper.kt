package com.example.taskpulse.data.mapper

import com.example.taskpulse.data.local.entity.AutomationRuleEntity
import com.example.taskpulse.domain.model.AutomationRule

fun AutomationRuleEntity.toDomain(): AutomationRule = AutomationRule(
    id = id,
    name = name,
    enabled = enabled,
    trigger = trigger,
    action = action,
    thresholdDays = thresholdDays
)

fun AutomationRule.toEntity(): AutomationRuleEntity = AutomationRuleEntity(
    id = id,
    name = name,
    enabled = enabled,
    trigger = trigger,
    action = action,
    thresholdDays = thresholdDays
)
