package com.example.taskpulse.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

/** Tipografía global con Hanken Grotesk (Stitch). */
val Typography = Typography(
    displayLarge = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.W600,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = (-0.02).sp
    ),
    displayMedium = TextStyle(
        fontFamily = HankenGrotesk,
        fontWeight = FontWeight.W600,
        fontSize = 28.sp,
        lineHeight = 36.sp
    ),
    headlineLarge = StitchTypography.headlineMd,
    headlineMedium = StitchTypography.headlineMd,
    headlineSmall = StitchTypography.headlineSm,
    titleLarge = StitchTypography.headlineSm,
    titleMedium = StitchTypography.bodyLg.copy(fontWeight = FontWeight.W500),
    titleSmall = StitchTypography.bodyMd.copy(fontWeight = FontWeight.W500),
    bodyLarge = StitchTypography.bodyLg,
    bodyMedium = StitchTypography.bodyMd,
    bodySmall = StitchTypography.labelLg.copy(fontWeight = FontWeight.W400),
    labelLarge = StitchTypography.labelLg,
    labelMedium = StitchTypography.labelLg,
    labelSmall = StitchTypography.labelLg
)
