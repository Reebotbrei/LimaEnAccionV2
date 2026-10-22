package com.limaenaccion.core.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun UserAvatar(
    name: String,
    isOfficial: Boolean = false,
    size: Dp = 44.dp,
    modifier: Modifier = Modifier
) {
    if (isOfficial) {
        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(MaterialTheme.colorScheme.primaryContainer),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.Filled.Shield,
                contentDescription = "Entidad Oficial",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(size * 0.55f)
            )
        }
    } else {
        val initials = extractInitials(name)
        val (bgColor, textColor) = getAvatarColors(name)

        Box(
            modifier = modifier
                .size(size)
                .clip(CircleShape)
                .background(bgColor),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = initials,
                color = textColor,
                fontWeight = FontWeight.Bold,
                fontSize = (size.value * 0.35f).sp
            )
        }
    }
}

private fun extractInitials(name: String): String {
    val clean = name.trim()
    if (clean.isBlank()) return "V"
    val parts = clean.split("\\s+".toRegex()).filter { it.isNotBlank() }
    return when {
        parts.size >= 2 -> "${parts[0].first().uppercase()}${parts[1].first().uppercase()}"
        parts.isNotEmpty() && parts[0].length >= 2 -> parts[0].take(2).uppercase()
        parts.isNotEmpty() -> parts[0].take(1).uppercase()
        else -> "V"
    }
}

private fun getAvatarColors(name: String): Pair<Color, Color> {
    val palettes = listOf(
        Color(0xFFFCE7F3) to Color(0xFFBE185D), // Rosa suave
        Color(0xFFFEF3C7) to Color(0xFFB45309), // Ámbar / mostaza
        Color(0xFFE0E7FF) to Color(0xFF4338CA), // Azul índigo
        Color(0xFFDCFCE7) to Color(0xFF15803D), // Verde esmeralda
        Color(0xFFF3E8FF) to Color(0xFF7E22CE), // Púrpura
        Color(0xFFCFFAFE) to Color(0xFF0E7490)  // Celeste
    )
    val index = kotlin.math.abs(name.hashCode()) % palettes.size
    return palettes[index]
}
