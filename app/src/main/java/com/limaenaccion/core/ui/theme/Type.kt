package com.limaenaccion.core.ui.theme

import androidx.compose.material3.Typography
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

fun getTypography(baseFontSizeSp: Float = 16f): Typography {
    val scale = (baseFontSizeSp / 16f).coerceIn(0.75f, 1.875f) // Rango 12sp a 30sp
    return Typography(
        displayLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = (57 * scale).sp),
        displayMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = (45 * scale).sp),
        displaySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = (36 * scale).sp),
        headlineLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = (32 * scale).sp),
        headlineMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = (28 * scale).sp),
        headlineSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = (24 * scale).sp),
        titleLarge = TextStyle(fontWeight = FontWeight.Bold, fontSize = (22 * scale).sp),
        titleMedium = TextStyle(fontWeight = FontWeight.Bold, fontSize = (18 * scale).sp),
        titleSmall = TextStyle(fontWeight = FontWeight.Bold, fontSize = (14 * scale).sp),
        bodyLarge = TextStyle(fontWeight = FontWeight.Normal, fontSize = (16 * scale).sp),
        bodyMedium = TextStyle(fontWeight = FontWeight.Normal, fontSize = (14 * scale).sp),
        bodySmall = TextStyle(fontWeight = FontWeight.Normal, fontSize = (12 * scale).sp),
        // Estilos usados por botones, chips y NavigationBarItem
        labelLarge = TextStyle(fontWeight = FontWeight.Medium, fontSize = (14 * scale).sp),
        labelMedium = TextStyle(fontWeight = FontWeight.Medium, fontSize = (12 * scale).sp),
        labelSmall = TextStyle(fontWeight = FontWeight.Medium, fontSize = (11 * scale).sp)
    )
}

val LimaEnAccionTypography = getTypography(16f)
