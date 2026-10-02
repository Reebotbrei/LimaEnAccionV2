package com.limaenaccion.core.ui.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable

private val LightColors = lightColorScheme(
    primary = BrandGreen,
    onPrimary = SurfaceWhite,
    primaryContainer = BrandGreenLight,
    error = EmergencyRed,
    errorContainer = EmergencyRedLight,
    onErrorContainer = EmergencyRed,
    background = BackgroundGray,
    surface = SurfaceWhite
)

private val DarkColors = darkColorScheme(
    primary = BrandGreen,
    onPrimary = SurfaceWhite,
    error = EmergencyRed,
    errorContainer = EmergencyRedLight,
    onErrorContainer = EmergencyRed
)

@Composable
fun LimaEnAccionTheme(
    darkTheme: Boolean = false,
    content: @Composable () -> Unit
) {
    MaterialTheme(
        colorScheme = if (darkTheme) DarkColors else LightColors,
        typography = LimaEnAccionTypography,
        content = content
    )
}