package com.limaenaccion.core.ui.components

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun DangerButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    containerColor: Color = Color(0xFFFEE2E2),
    contentColor: Color = Color(0xFFDC2626)
) {
    Surface(
        shape = RoundedCornerShape(14.dp),
        color = containerColor,
        onClick = onClick,
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = text,
            modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp),
            color = contentColor,
            fontWeight = FontWeight.Bold,
            textAlign = TextAlign.Center
        )
    }
}
