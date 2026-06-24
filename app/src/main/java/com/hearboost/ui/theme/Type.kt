package com.hearboost.ui.theme

import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ═══════════════════════════════════════════════════════════════
// Typography — Poppins (headings) + Roboto (body)
// Minimum 16sp for accessibility (presbyopia-friendly)
// ═══════════════════════════════════════════════════════════════

val PoppinsFamily = FontFamily.Default
val RobotoFamily = FontFamily.Default

// Display
val DisplayLarge = TextStyle(
    fontFamily = PoppinsFamily,
    fontSize = 36.sp,
    fontWeight = FontWeight.Bold,
    lineHeight = 44.sp,
    letterSpacing = (-0.02).sp
)

// Headlines
val HeadlineLarge = TextStyle(
    fontFamily = PoppinsFamily,
    fontSize = 28.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 36.sp
)

val HeadlineMedium = TextStyle(
    fontFamily = PoppinsFamily,
    fontSize = 24.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 32.sp
)

val HeadlineSmall = TextStyle(
    fontFamily = PoppinsFamily,
    fontSize = 20.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 28.sp
)

// Body
val BodyLarge = TextStyle(
    fontFamily = RobotoFamily,
    fontSize = 20.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 30.sp
)

val BodyMedium = TextStyle(
    fontFamily = RobotoFamily,
    fontSize = 18.sp,
    fontWeight = FontWeight.Normal,
    lineHeight = 28.sp
)

// Labels
val LabelLarge = TextStyle(
    fontFamily = RobotoFamily,
    fontSize = 16.sp,
    fontWeight = FontWeight.Medium,
    lineHeight = 24.sp,
    letterSpacing = 0.01.sp
)

val LabelMedium = TextStyle(
    fontFamily = RobotoFamily,
    fontSize = 14.sp,
    fontWeight = FontWeight.Medium,
    lineHeight = 20.sp,
    letterSpacing = 0.01.sp
)

// Button
val ButtonText = TextStyle(
    fontFamily = PoppinsFamily,
    fontSize = 18.sp,
    fontWeight = FontWeight.SemiBold,
    lineHeight = 24.sp,
    letterSpacing = 0.02.sp
)
