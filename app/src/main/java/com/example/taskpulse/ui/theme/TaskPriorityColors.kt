package com.example.taskpulse.ui.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.example.taskpulse.R
import com.example.taskpulse.domain.model.Task
import com.example.taskpulse.domain.model.TaskPriority
import com.example.taskpulse.domain.model.TaskStatus
import com.example.taskpulse.domain.model.isNote

object TaskPriorityColors {
    val Critical = Color(0xFFE57373)
    val High = Color(0xFFFF8A65)
    val Medium = Color(0xFFFFE082)
    val Low = Color(0xFFA5D6A7)
    val Note = Color(0xFFB0B0B0)

    fun forPriority(priority: TaskPriority): Color = when (priority) {
        TaskPriority.CRITICAL -> Critical
        TaskPriority.HIGH -> High
        TaskPriority.MEDIUM -> Medium
        TaskPriority.LOW -> Low
    }

    @Composable
    fun forEntry(task: Task): Color {
        if (task.isNote) return Note
        val base = forPriority(task.priority)
        return if (task.status == TaskStatus.COMPLETED) {
            base.copy(alpha = 0.45f)
        } else {
            base
        }
    }
}

@Composable
fun EntryPriorityDot(
    task: Task,
    modifier: Modifier = Modifier,
    size: Dp = 10.dp
) {
    Box(
        modifier = modifier
            .size(size)
            .clip(CircleShape)
            .background(TaskPriorityColors.forEntry(task))
    )
}

@Composable
fun entryPriorityLabelText(task: Task): String = when {
    task.isNote -> stringResource(R.string.home_entry_note_label)
    task.priority == TaskPriority.CRITICAL -> stringResource(R.string.home_priority_critical_label)
    task.priority == TaskPriority.HIGH -> stringResource(R.string.home_priority_high_label)
    task.priority == TaskPriority.MEDIUM -> stringResource(R.string.home_priority_medium_label)
    else -> stringResource(R.string.home_priority_low_label)
}

@Composable
fun EntryPriorityLabel(
    task: Task,
    modifier: Modifier = Modifier
) {
    Text(
        text = entryPriorityLabelText(task),
        modifier = modifier,
        style = MaterialTheme.typography.labelMedium,
        fontWeight = FontWeight.Medium,
        color = TaskPriorityColors.forEntry(task),
        textAlign = TextAlign.End
    )
}
