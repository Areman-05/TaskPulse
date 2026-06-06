package com.example.taskpulse.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

/** Fondo Stitch: linear-gradient(135deg, #f8f9fa 0%, rgba(237,221,210,0.3) 100%) */
@Composable
fun StitchHomeBackground(modifier: Modifier = Modifier) {
    val start = Color(0xFFF8F9FA)
    val end = Color(0x4DEDD2D2) // rgba(237, 221, 210, 0.3)

    Canvas(modifier = modifier) {
        drawRect(
            brush = Brush.linearGradient(
                colors = listOf(start, end),
                start = Offset(0f, 0f),
                end = Offset(size.width, size.height)
            )
        )
    }
}
