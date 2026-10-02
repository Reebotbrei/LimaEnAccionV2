package com.limaenaccion.feature_sos.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SosActivationScreen(
    onActivated: () -> Unit,
    onCancelled: () -> Unit,
    viewModel: SosActivationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.activated) { if (uiState.activated) onActivated() }
    LaunchedEffect(uiState.cancelled) { if (uiState.cancelled) onCancelled() }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFF0A2E1F)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text("ACTIVANDO SOS EN", color = Color.White, style = MaterialTheme.typography.labelLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(24.dp))
            Box(modifier = Modifier.size(160.dp).clip(CircleShape).background(Color(0xFF14532D)), contentAlignment = Alignment.Center) {
                Text(uiState.secondsRemaining.toString(), color = Color.White, fontSize = 64.sp, fontWeight = FontWeight.Bold)
            }
            Spacer(modifier = Modifier.height(32.dp))
            OutlinedButton(
                onClick = viewModel::onCancelClicked,
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                modifier = Modifier.height(48.dp)
            ) { Text("Cancelar Alarma") }
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                "Se notificará a todos los vecinos en 500m.",
                color = Color.White.copy(alpha = 0.7f),
                style = MaterialTheme.typography.labelSmall,
                modifier = Modifier.padding(horizontal = 32.dp),
                textAlign = TextAlign.Center
            )
        }
    }
}