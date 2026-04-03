package com.jbncode.anotadordomino.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

// ── Font family ────────────────────────────────────────────────────────────────
// Option A: usa fuente del sistema (funciona inmediatamente)
val KineticFont = FontFamily.Default

// Option B: Orbitron desde Google Fonts (recomendado para el look exacto)
// Pasos:
//   1. Agrega en res/font/: orbitron_regular.ttf, orbitron_medium.ttf,
//      orbitron_semibold.ttf, orbitron_bold.ttf, orbitron_extrabold.ttf
//      Descarga: https://fonts.google.com/specimen/Orbitron
//   2. Comenta la línea de arriba y descomenta esto:
//
// val KineticFont = FontFamily(
//     Font(R.font.orbitron_regular,   FontWeight.Normal),
//     Font(R.font.orbitron_medium,    FontWeight.Medium),
//     Font(R.font.orbitron_semibold,  FontWeight.SemiBold),
//     Font(R.font.orbitron_bold,      FontWeight.Bold),
//     Font(R.font.orbitron_extrabold, FontWeight.ExtraBold),
// )

// ── Typography scale ───────────────────────────────────────────────────────────
val KineticTypography = Typography(

    // Display — season title, big numbers
    displayLarge = TextStyle(
        fontFamily = KineticFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 40.sp,
        lineHeight = 48.sp,
        letterSpacing = 1.sp
    ),
    displayMedium = TextStyle(
        fontFamily = KineticFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 32.sp,
        lineHeight = 40.sp,
        letterSpacing = 0.5.sp
    ),
    displaySmall = TextStyle(
        fontFamily = KineticFont,
        fontWeight = FontWeight.Bold,
        fontSize = 26.sp,
        lineHeight = 34.sp,
        letterSpacing = 0.sp
    ),

    // Headline — dialog titles, screen headers
    headlineLarge = TextStyle(
        fontFamily = KineticFont,
        fontWeight = FontWeight.ExtraBold,
        fontSize = 28.sp,
        lineHeight = 36.sp,
        letterSpacing = 0.sp
    ),
    headlineMedium = TextStyle(
        fontFamily = KineticFont,
        fontWeight = FontWeight.Bold,
        fontSize = 22.sp,
        lineHeight = 30.sp,
        letterSpacing = 0.sp
    ),
    headlineSmall = TextStyle(
        fontFamily = KineticFont,
        fontWeight = FontWeight.Bold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 0.sp
    ),

    // Title — card titles, player names, top bar
    titleLarge = TextStyle(
        fontFamily = KineticFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp,
        lineHeight = 26.sp,
        letterSpacing = 1.sp
    ),
    titleMedium = TextStyle(
        fontFamily = KineticFont,
        fontWeight = FontWeight.SemiBold,
        fontSize = 15.sp,
        lineHeight = 22.sp,
        letterSpacing = 0.5.sp
    ),
    titleSmall = TextStyle(
        fontFamily = KineticFont,
        fontWeight = FontWeight.Medium,
        fontSize = 13.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.5.sp
    ),

    // Body — descriptions, status text
    bodyLarge = TextStyle(
        fontFamily = KineticFont,
        fontWeight = FontWeight.Normal,
        fontSize = 16.sp,
        lineHeight = 24.sp,
        letterSpacing = 0.sp
    ),
    bodyMedium = TextStyle(
        fontFamily = KineticFont,
        fontWeight = FontWeight.Normal,
        fontSize = 14.sp,
        lineHeight = 20.sp,
        letterSpacing = 0.sp
    ),
    bodySmall = TextStyle(
        fontFamily = KineticFont,
        fontWeight = FontWeight.Normal,
        fontSize = 12.sp,
        lineHeight = 16.sp,
        letterSpacing = 0.sp
    ),

    // Label — section labels (GAME MODE, WINS: 42, etc.)
    labelLarge = TextStyle(
        fontFamily = KineticFont,
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        lineHeight = 18.sp,
        letterSpacing = 2.sp
    ),
    labelMedium = TextStyle(
        fontFamily = KineticFont,
        fontWeight = FontWeight.Bold,
        fontSize = 11.sp,
        lineHeight = 16.sp,
        letterSpacing = 2.sp
    ),
    labelSmall = TextStyle(
        fontFamily = KineticFont,
        fontWeight = FontWeight.Bold,
        fontSize = 9.sp,
        lineHeight = 14.sp,
        letterSpacing = 1.5.sp
    ),
)