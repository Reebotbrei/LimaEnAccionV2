package com.limaenaccion.feature_reports.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun PhotoPreviewScreen(
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit,
    onRetake: () -> Unit,
    onAccept: () -> Unit
) {
    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF1A1A1A))) {
        // Simulación de la foto sin CameraX, es solo un placeholder visual.
        Box(modifier = Modifier.fillMaxSize().padding(bottom = 96.dp), contentAlignment = Alignment.Center) {
            Text("FOTO CAPTURADA", color = Color(0xFFEF4444), fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
        }

        IconButton(onClick = onNavigateBack, modifier = Modifier.align(Alignment.TopStart).padding(12.dp)) {
            Icon(Icons.Filled.Delete, contentDescription = "Descartar", tint = Color.White)
        }
        IconButton(onClick = onNavigateBack, modifier = Modifier.align(Alignment.TopEnd).padding(12.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
        }

        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            OutlinedButton(
                onClick = { viewModel.onRetakePhoto(); onRetake() },
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Retomar")
            }
            Button(
                onClick = onAccept,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF14532D)),
                modifier = Modifier.weight(1f).height(48.dp)
            ) {
                Icon(Icons.Filled.Check, contentDescription = null, tint = Color.White, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(6.dp))
                Text("Aceptar", color = Color.White)
            }
        }
    }
}