package com.example.taskpulse.ui.insights

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.taskpulse.domain.model.DailyProductivityPoint

@Composable
fun ProductivityBarChart(
    points: List<DailyProductivityPoint>,
    modifier: Modifier = Modifier
) {
    if (points.isEmpty()) return
    val max = points.maxOf { it.completedCount }.coerceAtLeast(1)
    Row(
        modifier = modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.Bottom
    ) {
        points.forEach { point ->
            ColumnBar(
                fraction = point.completedCount.toFloat() / max,
                countLabel = point.completedCount.toString()
            )
        }
    }
}

@Composable
private fun ColumnBar(
    fraction: Float,
    countLabel: String
) {
    val trackHeight = 72.dp
    val barHeight = (trackHeight.value * fraction.coerceIn(0f, 1f)).dp.coerceAtLeast(3.dp)
    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Bottom
    ) {
        Box(
            modifier = Modifier
                .width(22.dp)
                .height(trackHeight),
            contentAlignment = Alignment.BottomCenter
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(barHeight)
                    .clip(RoundedCornerShape(topStart = 4.dp, topEnd = 4.dp))
                    .background(MaterialTheme.colorScheme.tertiary)
            )
        }
        Text(
            text = countLabel,
            style = MaterialTheme.typography.labelSmall,
            modifier = Modifier.padding(top = 4.dp)
        )
    }
}
