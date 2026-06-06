package com.example.taskpulse.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import com.example.taskpulse.R

val HankenGrotesk = FontFamily(
    Font(R.font.hanken_grotesk, FontWeight.W400),
    Font(R.font.hanken_grotesk, FontWeight.W500),
    Font(R.font.hanken_grotesk, FontWeight.W600),
    Font(R.font.hanken_grotesk, FontWeight.W700)
)

/** Tokens tipográficos exactos del HTML Stitch. */
object StitchTypography {
    val headlineMd = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.W600,
        fontSize = 24.sp,
        lineHeight = 32.sp
    )
    val headlineSm = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.W500,
        fontSize = 20.sp,
        lineHeight = 28.sp
    )
    val bodyLg = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.W400,
        fontSize = 16.sp,
        lineHeight = 24.sp
    )
    val bodyMd = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.W400,
        fontSize = 14.sp,
        lineHeight = 20.sp
    )
    val labelLg = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.W500,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.5.sp
    )
}
