package com.limaenaccion.feature_settings.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel

@Composable
fun SettingsScreen(
    onLogoutConfirmed: () -> Unit,
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Configuración", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(20.dp))

        Text("NOTIFICACIONES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        SettingSwitchRow("Recibir notificaciones push", "Alertas de incidentes en tu zona", uiState.pushNotificationsEnabled, viewModel::onTogglePushNotifications)
        Spacer(modifier = Modifier.height(8.dp))
        SettingSwitchRow("Alertas sonoras SOS", "Sonido al recibir emergencias", uiState.sosSoundEnabled, viewModel::onToggleSosSound)

        Spacer(modifier = Modifier.height(24.dp))
        Text("CUENTA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        InfoRow("Zona registrada", uiState.registeredZone)
        InfoRow("Versión de la app", uiState.appVersion)

        Spacer(modifier = Modifier.height(24.dp))
        Text("LEGAL", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(8.dp))
        LegalLinkRow("Términos y condiciones")
        LegalLinkRow("Política de privacidad")
        LegalLinkRow("Ayuda y soporte")

        Spacer(modifier = Modifier.height(24.dp))
        Surface(shape = RoundedCornerShape(14.dp), color = Color(0xFFFEE2E2), onClick = viewModel::onLogoutClicked, modifier = Modifier.fillMaxWidth()) {
            Text("Cerrar sesión", modifier = Modifier.fillMaxWidth().padding(vertical = 14.dp), color = Color(0xFFDC2626), fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
        }
    }

    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissLogoutDialog,
            title = { Text("¿Seguro que deseas salir?") },
            text = { Text("Dejarás de recibir alertas de tu vecindario mientras estés desconectado.") },
            confirmButton = {
                TextButton(onClick = { viewModel.onDismissLogoutDialog(); onLogoutConfirmed() }) {
                    Text("Sí, cerrar sesión", color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = viewModel::onDismissLogoutDialog) { Text("Cancelar") } }
        )
    }
}

@Composable
private fun SettingSwitchRow(title: String, subtitle: String, checked: Boolean, onCheckedChange: (Boolean) -> Unit) {
    Surface(shape = RoundedCornerShape(14.dp), color = MaterialTheme.colorScheme.surface, shadowElevation = 1.dp, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(14.dp), verticalAlignment = Alignment.CenterVertically) {
            Column(modifier = Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            Switch(checked = checked, onCheckedChange = onCheckedChange)
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp), horizontalArrangement = Arrangement.SpaceBetween) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold)
    }
}

@Composable
private fun LegalLinkRow(title: String) {
    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 10.dp), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
        Text(title)
        Icon(Icons.Filled.ChevronRight, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}