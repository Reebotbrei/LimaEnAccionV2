package com.limaenaccion.core.ui.components

import androidx.annotation.DrawableRes
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.limaenaccion.R
import com.limaenaccion.core.ui.theme.AccessibleIntenseBlue
import com.limaenaccion.core.ui.theme.AppThemeMode
import com.limaenaccion.core.ui.theme.BrandForestDark
import com.limaenaccion.core.ui.theme.ColorblindDeepNavy
import com.limaenaccion.core.ui.theme.LocalAppThemeMode

enum class LogoVariant {
    PIN_ONLY,       // Solo el pin de geolocalización (transparente)
    ICON_SQUIRCLE,  // El ícono de app enmarcado en squircle (Claro / Oscuro / Daltónico)
    FULL_BRAND      // Logo completo: Ícono/Pin + "Lima en Acción" + "SEGURIDAD VECINAL"
}

enum class BrandLayout {
    ICON_TOP,       // Ícono arriba y tipografía centrada debajo (Login)
    TITLE_TOP,      // Tipografía arriba y Pin debajo, fiel a la lámina maestra (Splash)
    HORIZONTAL      // Ícono a la izquierda y tipografía alineada a la derecha (Cabeceras)
}

@Composable
fun AppLogo(
    modifier: Modifier = Modifier,
    variant: LogoVariant = LogoVariant.FULL_BRAND,
    layout: BrandLayout = BrandLayout.ICON_TOP,
    size: Dp = 80.dp,
    showSubtitle: Boolean = true,
    useSquircleInFullBrand: Boolean = true,
    titleFontSize: TextUnit = TextUnit.Unspecified
) {
    val themeMode = LocalAppThemeMode.current

    @DrawableRes
    val squircleRes = when (themeMode) {
        AppThemeMode.LIGHT -> R.drawable.ic_app_logo_light
        AppThemeMode.DARK -> R.drawable.ic_app_logo_dark
        AppThemeMode.COLORBLIND -> R.drawable.ic_app_logo_colorblind
    }

    @DrawableRes
    val pinRes = when (themeMode) {
        AppThemeMode.COLORBLIND -> R.drawable.ic_lima_pin_colorblind
        AppThemeMode.LIGHT, AppThemeMode.DARK -> R.drawable.ic_lima_pin
    }

    val titleColor = when (themeMode) {
        AppThemeMode.LIGHT -> BrandForestDark
        AppThemeMode.DARK -> Color(0xFFF8FAFC)
        AppThemeMode.COLORBLIND -> ColorblindDeepNavy
    }

    val subtitleColor = when (themeMode) {
        AppThemeMode.LIGHT -> Color(0xFF475569)
        AppThemeMode.DARK -> Color(0xFF94A3B8)
        AppThemeMode.COLORBLIND -> AccessibleIntenseBlue
    }

    when (variant) {
        LogoVariant.PIN_ONLY -> {
            Image(
                painter = painterResource(id = pinRes),
                contentDescription = "Pin oficial de Lima en Acción",
                contentScale = ContentScale.Fit,
                modifier = modifier.size(size)
            )
        }

        LogoVariant.ICON_SQUIRCLE -> {
            Image(
                painter = painterResource(id = squircleRes),
                contentDescription = "Ícono oficial de Lima en Acción",
                contentScale = ContentScale.Fit,
                modifier = modifier.size(size)
            )
        }

        LogoVariant.FULL_BRAND -> {
            val activeLogoRes = if (useSquircleInFullBrand) squircleRes else pinRes

            when (layout) {
                BrandLayout.ICON_TOP -> {
                    Column(
                        modifier = modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Image(
                            painter = painterResource(id = activeLogoRes),
                            contentDescription = "Logo de Lima en Acción",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(size)
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        BrandWordmark(
                            titleColor = titleColor,
                            subtitleColor = subtitleColor,
                            showSubtitle = showSubtitle,
                            titleFontSize = titleFontSize,
                            centered = true
                        )
                    }
                }

                BrandLayout.TITLE_TOP -> {
                    Column(
                        modifier = modifier,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        BrandWordmark(
                            titleColor = titleColor,
                            subtitleColor = subtitleColor,
                            showSubtitle = showSubtitle,
                            titleFontSize = titleFontSize,
                            centered = false
                        )
                        Spacer(modifier = Modifier.height(28.dp))
                        Image(
                            painter = painterResource(id = activeLogoRes),
                            contentDescription = "Logo de Lima en Acción",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(size)
                        )
                    }
                }

                BrandLayout.HORIZONTAL -> {
                    Row(
                        modifier = modifier,
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Start
                    ) {
                        Image(
                            painter = painterResource(id = activeLogoRes),
                            contentDescription = "Logo de Lima en Acción",
                            contentScale = ContentScale.Fit,
                            modifier = Modifier.size(size)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        BrandWordmark(
                            titleColor = titleColor,
                            subtitleColor = subtitleColor,
                            showSubtitle = showSubtitle,
                            titleFontSize = titleFontSize,
                            centered = false
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun BrandWordmark(
    titleColor: Color,
    subtitleColor: Color,
    showSubtitle: Boolean,
    titleFontSize: TextUnit,
    centered: Boolean
) {
    val baseTitleStyle = MaterialTheme.typography.headlineMedium
    val effectiveFontSize = if (titleFontSize != TextUnit.Unspecified) {
        titleFontSize
    } else {
        baseTitleStyle.fontSize
    }

    val brandAnnotatedTitle = buildAnnotatedString {
        withStyle(style = SpanStyle(fontWeight = FontWeight.ExtraBold)) {
            append("Lima")
        }
        withStyle(style = SpanStyle(fontWeight = FontWeight.Normal)) {
            append(" en Acción")
        }
    }

    Column(
        horizontalAlignment = if (centered) Alignment.CenterHorizontally else Alignment.Start
    ) {
        Text(
            text = brandAnnotatedTitle,
            color = titleColor,
            style = baseTitleStyle.copy(
                fontSize = effectiveFontSize,
                letterSpacing = (-0.5).sp
            ),
            textAlign = if (centered) TextAlign.Center else TextAlign.Start
        )

        if (showSubtitle) {
            Spacer(modifier = Modifier.height(3.dp))
            Text(
                text = "SEGURIDAD VECINAL",
                color = subtitleColor,
                style = MaterialTheme.typography.labelSmall.copy(
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 2.4.sp
                ),
                textAlign = if (centered) TextAlign.Center else TextAlign.Start
            )
        }
    }
}
