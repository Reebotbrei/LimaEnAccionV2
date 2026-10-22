package com.limaenaccion.feature_reports.presentation

import android.graphics.Bitmap
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.result.launch
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Cameraswitch
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FlashOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp

@Composable
fun CameraSimulationScreen(
    viewModel: ReportViewModel,
    onClose: () -> Unit,
    onPhotoCaptured: () -> Unit
) {
    // Launcher para abrir la cámara nativa del sistema
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap: Bitmap? ->
        if (bitmap != null) {
            viewModel.onPhotoCaptured(bitmap)
            onPhotoCaptured()
        }
    }

    Box(modifier = Modifier.fillMaxSize().background(Color.Black)) {
        IconButton(onClick = onClose, modifier = Modifier.align(Alignment.TopStart).padding(16.dp)) {
            Icon(Icons.Filled.Close, contentDescription = "Cerrar", tint = Color.White)
        }

        Column(
            modifier = Modifier
                .align(Alignment.Center)
                .fillMaxWidth(0.85f),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Visor de la cámara
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(300.dp)
                    .border(2.dp, Color.White.copy(alpha = 0.6f), RoundedCornerShape(16.dp)),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Filled.CameraAlt,
                        contentDescription = null,
                        tint = Color.White.copy(alpha = 0.8f),
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "Cámara de Evidencia",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    Text(
                        "Presiona el botón para tomar la foto del incidente",
                        color = Color.White.copy(alpha = 0.7f),
                        textAlign = TextAlign.Center,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.padding(horizontal = 20.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Botón directo para disparar la cámara del sistema
            Button(
                onClick = { cameraLauncher.launch() },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.fillMaxWidth().height(48.dp)
            ) {
                Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text("Abrir Cámara Real", fontWeight = FontWeight.Bold)
            }
        }

        // Barra inferior de captura
        Row(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(bottom = 32.dp),
            horizontalArrangement = Arrangement.SpaceEvenly,
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircleIconButton(icon = Icons.Filled.FlashOff, onClick = { /* Sin lógica */ })

            // Botón de obturador principal: abre la cámara
            Box(
                modifier = Modifier
                    .size(76.dp)
                    .clip(CircleShape)
                    .background(Color.White.copy(alpha = 0.2f))
                    .border(3.dp, Color.White, CircleShape)
                    .clickable {
                        cameraLauncher.launch()
                    },
                contentAlignment = Alignment.Center
            ) {
                Box(modifier = Modifier.size(60.dp).clip(CircleShape).background(Color.White))
            }

            CircleIconButton(icon = Icons.Filled.Cameraswitch, onClick = { cameraLauncher.launch() })
        }
    }
}

@Composable
private fun CircleIconButton(icon: ImageVector, onClick: () -> Unit) {
    Box(modifier = Modifier.size(48.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.15f)), contentAlignment = Alignment.Center) {
        IconButton(onClick = onClick) {
            Icon(icon, contentDescription = null, tint = Color.White)
        }
    }
}