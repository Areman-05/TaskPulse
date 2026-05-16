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

private const val ScrollDragSensitivity = 0.72f

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

            if (scrollState.maxValue > 0) {
                if (scrollbarCompact) {
                    Box(
                        modifier = Modifier
                            .fillMaxHeight()
                            .width(32.dp)
                            .padding(end = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        TaskPulseScrollbar(
                            scrollState = scrollState,
                            compact = true,
                            onDragDelta = { delta ->
                                scope.launch {
                                    scrollState.scroll { scrollBy(delta) }
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
                                scrollState.scroll { scrollBy(delta) }
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
    val touchWidth = if (compact) 32.dp else 28.dp
    val visualThumbWidth = if (compact) 10.dp else 12.dp
    val visualTrackWidth = if (compact) 5.dp else 6.dp

    BoxWithConstraints(
        modifier = modifier.width(touchWidth),
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
            (if (compact) 52.dp else 72.dp).toPx()
        }.coerceIn(
            with(density) { 40.dp.toPx() },
            trackHeightPx * 0.55f
        )
        val scrollableTrackPx = (trackHeightPx - thumbHeightPx).coerceAtLeast(1f)
        val maxScroll = scrollState.maxValue.toFloat().coerceAtLeast(1f)
        val scrollFraction = (scrollState.value / maxScroll).coerceIn(0f, 1f)
        val thumbOffsetPx = trackTopInsetPx + (scrollableTrackPx * scrollFraction)

        fun scrollDeltaForDrag(dragY: Float): Float {
            val scrollPerPixel = maxScroll / scrollableTrackPx
            return dragY * scrollPerPixel * ScrollDragSensitivity
        }

        fun fractionForTrackTap(tapY: Float): Float {
            val relativeY = (tapY - trackTopInsetPx - thumbHeightPx / 2f)
                .coerceIn(0f, scrollableTrackPx)
            return (relativeY / scrollableTrackPx).coerceIn(0f, 1f)
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(0, trackTopInsetPx.roundToInt()) }
                .height(with(density) { trackHeightPx.toDp() })
                .width(touchWidth)
                .align(Alignment.TopCenter)
                .pointerInput(scrollState.maxValue, trackHeightPx, trackTopInsetPx, thumbHeightPx) {
                    detectTapGestures { offset ->
                        onJumpToFraction(fractionForTrackTap(offset.y))
                    }
                }
        ) {
            Box(
                modifier = Modifier
                    .width(visualTrackWidth)
                    .fillMaxHeight()
                    .align(Alignment.Center)
                    .clip(RoundedCornerShape(3.dp))
            ) {
                androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                    drawRoundRect(
                        color = trackColor,
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(8f, 8f)
                    )
                }
            }
        }

        Box(
            modifier = Modifier
                .offset { IntOffset(0, thumbOffsetPx.roundToInt()) }
                .width(touchWidth)
                .height(with(density) { thumbHeightPx.toDp() })
                .align(Alignment.TopCenter)
                .clip(RoundedCornerShape(6.dp))
                .pointerInput(scrollState.maxValue, scrollableTrackPx) {
                    detectDragGestures(
                        onDrag = { change, dragAmount ->
                            change.consume()
                            onDragDelta(scrollDeltaForDrag(dragAmount.y))
                        }
                    )
                },
            contentAlignment = Alignment.Center
        ) {
            Box(
                modifier = Modifier
                    .width(visualThumbWidth)
                    .fillMaxHeight()
                    .clip(RoundedCornerShape(6.dp))
            ) {
                androidx.compose.foundation.Canvas(Modifier.fillMaxSize()) {
                    drawRoundRect(
                        color = if (scrollState.isScrollInProgress) {
                            thumbColor
                        } else {
                            thumbColor.copy(alpha = 0.88f)
                        },
                        cornerRadius = androidx.compose.ui.geometry.CornerRadius(10f, 10f)
                    )
                }
            }
        }
    }
}
