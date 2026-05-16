package com.example.taskpulse.ui.components

import androidx.compose.foundation.ScrollState
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.launch
import kotlin.math.roundToInt

@Composable
fun TaskPulseScrollableColumn(
    modifier: Modifier = Modifier,
    scrollState: ScrollState = rememberScrollState(),
    showAmbientGrid: Boolean = true,
    contentPaddingBottom: androidx.compose.ui.unit.Dp = 32.dp,
    scrollbarCompact: Boolean = false,
    content: @Composable ColumnScope.() -> Unit
) {
    val scope = rememberCoroutineScope()

    Box(modifier = modifier.fillMaxSize()) {
        if (showAmbientGrid) {
            TaskPulseAmbientGrid(Modifier.fillMaxSize())
        }

        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.Top
        ) {
            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(scrollState)
                    .padding(
                        end = if (scrollbarCompact) 4.dp else 6.dp,
                        bottom = contentPaddingBottom
                    ),
                content = content
            )

            if (scrollbarCompact) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .width(28.dp)
                        .padding(end = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    TaskPulseScrollbar(
                        scrollState = scrollState,
                        compact = true,
                        onDragDelta = { delta ->
                            scope.launch {
                                scrollState.scroll {
                                    scrollBy(delta)
                                }
                            }
                        },
                        onJumpToFraction = { fraction ->
                            scope.launch {
                                scrollState.scrollTo((scrollState.maxValue * fraction).toInt())
                            }
                        },
                        modifier = Modifier.fillMaxHeight(0.52f)
                    )
                }
            } else {
                TaskPulseScrollbar(
                    scrollState = scrollState,
                    compact = false,
                    onDragDelta = { delta ->
                        scope.launch {
                            scrollState.scroll {
                                scrollBy(delta)
                            }
                        }
                    },
                    onJumpToFraction = { fraction ->
                        scope.launch {
                            scrollState.scrollTo((scrollState.maxValue * fraction).toInt())
                        }
                    },
                    modifier = Modifier
                        .fillMaxHeight()
                        .padding(top = 8.dp, bottom = 8.dp, end = 2.dp)
                )
            }
        }
    }
}

@Composable
private fun TaskPulseScrollbar(
    scrollState: ScrollState,
    compact: Boolean,
    onDragDelta: (Float) -> Unit,
    onJumpToFraction: (Float) -> Unit,
    modifier: Modifier = Modifier
) {
    val density = LocalDensity.current
    val trackColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.4f)
    val thumbColor = MaterialTheme.colorScheme.tertiary
    val barWidth = if (compact) 12.dp else 18.dp

    BoxWithConstraints(
        modifier = modifier.width(barWidth),
        contentAlignment = Alignment.TopCenter
    ) {
        val containerHeightPx = constraints.maxHeight.toFloat().coerceAtLeast(1f)
        val trackHeightPx = if (compact) {
            containerHeightPx * 0.5f
        } else {
            containerHeightPx
        }
        val trackTopInsetPx = if (compact) {
            (containerHeightPx - trackHeightPx) / 2f
        } else {
            0f
        }
        val thumbHeightPx = with(density) {
            (if (compact) 44.dp else 56.dp).toPx()
        }.coerceAtMost(trackHeightPx * 0.45f)
        val maxScroll = scrollState.maxValue.toFloat().coerceAtLeast(1f)
        val scrollFraction = (scrollState.value / maxScroll).coerceIn(0f, 1f)
        val thumbOffsetPx = trackTopInsetPx + ((trackHeightPx - thumbHeightPx) * scrollFraction).coerceAtLeast(0f)

        Box(
            modifier = Modifier
                .offset { IntOffset(0, trackTopInsetPx.roundToInt()) }
                .height(with(density) { trackHeightPx.toDp() })
                .width(if (compact) 4.dp else 5.dp)
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(3.dp))
                .pointerInput(scrollState.maxValue, trackHeightPx, trackTopInsetPx) {
                    detectTapGestures { offset ->
                        val fraction = (offset.y / trackHeightPx).coerceIn(0f, 1f)
                        onJumpToFraction(fraction)
                    }
                }
        ) {
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = trackColor,
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                )
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(0, thumbOffsetPx.roundToInt()) }
                .width(if (compact) 8.dp else 10.dp)
                .height(with(density) { thumbHeightPx.toDp() })
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(5.dp))
                .pointerInput(Unit) {
                    detectDragGestures { change, dragAmount ->
                        change.consume()
                        val scale = maxScroll / trackHeightPx.coerceAtLeast(1f)
                        onDragDelta(dragAmount.y * scale * 1.15f)
                    }
                }
        ) {
            androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                drawRoundRect(
                    color = if (scrollState.isScrollInProgress) thumbColor else thumbColor.copy(alpha = 0.85f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                )
            }
        }
    }
}
