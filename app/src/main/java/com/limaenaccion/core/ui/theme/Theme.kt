package com.limaenaccion.core.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color

enum class AppThemeMode {
    LIGHT,
    DARK,
    COLORBLIND
}

val LocalAppThemeMode = staticCompositionLocalOf { AppThemeMode.LIGHT }

private val LightColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = SurfaceWhite,
    primaryContainer = BrandGreenLight,
    onPrimaryContainer = BrandGreen,
    secondary = BrandGreen,
    secondaryContainer = BrandGreenLight,      // Elimina el rosado/lavanda por defecto de M3
    onSecondaryContainer = BrandGreen,
    tertiary = BrandGreen,
    tertiaryContainer = BrandGreenLight,
    onTertiaryContainer = BrandGreen,
    error = EmergencyRed,                      // En modo normal, el botón de llamada y alerta es ROJO
    onError = SurfaceWhite,
    errorContainer = EmergencyRedLight,
    onErrorContainer = EmergencyRed,
    background = BackgroundGray,
    onBackground = Color(0xFF111827),
    surface = SurfaceWhite,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF6B7280),
    outline = Color(0xFFE5E7EB),
    outlineVariant = Color(0xFFE5E7EB)
)

private val DarkColors = darkColorScheme(
    primary = Color(0xFF4ADE80),
    onPrimary = Color(0xFF062E16),
    primaryContainer = Color(0xFF1E3A2F),
    onPrimaryContainer = Color(0xFF86EFAC),
    secondary = Color(0xFF4ADE80),
    secondaryContainer = Color(0xFF1E3A2F),
    onSecondaryContainer = Color(0xFF86EFAC),
    error = EmergencyRed,                      // En modo oscuro, alerta sigue siendo ROJO
    onError = SurfaceWhite,
    errorContainer = Color(0xFF7F1D1D),
    onErrorContainer = Color(0xFFFECACA),
    background = Color(0xFF0F172A),
    onBackground = Color(0xFFF8FAFC),
    surface = Color(0xFF1E293B),
    onSurface = Color(0xFFF8FAFC),
    surfaceVariant = Color(0xFF334155),
    onSurfaceVariant = Color(0xFF94A3B8),
    outline = Color(0xFF475569),
    outlineVariant = Color(0xFF475569)
)

private val ColorblindColors = lightColorScheme(
    primary = AccessibleIntenseBlue,
    onPrimary = SurfaceWhite,
    primaryContainer = ColorblindPrimaryLight,
    onPrimaryContainer = AccessibleIntenseBlue,
    secondary = AccessibleIntenseBlue,
    secondaryContainer = ColorblindPrimaryLight, // Elimina el rosado en modo daltónico
    onSecondaryContainer = AccessibleIntenseBlue,
    tertiary = AccessibleIntenseBlue,
    tertiaryContainer = ColorblindPrimaryLight,
    onTertiaryContainer = AccessibleIntenseBlue,
    error = AccessibleIntenseBlue,               // SOLO EN MODO DALTÓNICO: El botón de llamada es AZUL INTENSO
    onError = SurfaceWhite,
    errorContainer = ColorblindPrimaryLight,
    onErrorContainer = AccessibleIntenseBlue,
    background = BackgroundGray,
    onBackground = Color(0xFF111827),
    surface = SurfaceWhite,
    onSurface = Color(0xFF111827),
    surfaceVariant = Color(0xFFF3F4F6),
    onSurfaceVariant = Color(0xFF4B5563),
    outline = Color(0xFFD1D5DB),
    outlineVariant = Color(0xFFD1D5DB)
)

@Composable
fun LimaEnAccionTheme(
    theme: String = "Sistema",
    fontSizeSp: Float = 16f,
    content: @Composable () -> Unit
) {
    val systemDark = isSystemInDarkTheme()
    val themeMode = when (theme.lowercase()) {
        "oscuro" -> AppThemeMode.DARK
        "claro" -> AppThemeMode.LIGHT
        "daltonico", "daltónico" -> AppThemeMode.COLORBLIND
        else -> if (systemDark) AppThemeMode.DARK else AppThemeMode.LIGHT
    }
    val colorScheme = when (themeMode) {
        AppThemeMode.DARK -> DarkColors
        AppThemeMode.LIGHT -> LightColors
        AppThemeMode.COLORBLIND -> ColorblindColors
    }

    CompositionLocalProvider(LocalAppThemeMode provides themeMode) {
        MaterialTheme(
            colorScheme = colorScheme,
            typography = getTypography(fontSizeSp),
            content = content
        )
    }
}
