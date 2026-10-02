package com.limaenaccion.feature_profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.LocationCity
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.PhotoCamera
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.ui.components.MenuRow

@Composable
fun ProfileScreen(
    onEditPersonalData: () -> Unit,
    onChangePhoto: () -> Unit,
    onUpdateAddress: () -> Unit,
    onReportHistory: () -> Unit,
    onManageContacts: () -> Unit,
    viewModel: ProfileViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Column(modifier = Modifier.fillMaxSize().verticalScroll(rememberScrollState())) {
        Column(
            modifier = Modifier.fillMaxWidth().background(MaterialTheme.colorScheme.primary).padding(vertical = 32.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Box(
                modifier = Modifier.size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.15f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.onPrimary, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text(uiState.fullName, color = MaterialTheme.colorScheme.onPrimary, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Text(
                "DNI: ${uiState.dni} · Distrito actual: ${uiState.district}",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text("CONFIGURACIÓN DE CUENTA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            MenuRow(icon = Icons.Filled.Person, title = "Editar Datos Personales", onClick = onEditPersonalData)
            MenuRow(icon = Icons.Filled.PhotoCamera, title = "Cambiar foto de perfil", onClick = onChangePhoto)
            MenuRow(icon = Icons.Filled.LocationCity, title = "Cambiar de Domicilio / Zona", onClick = onUpdateAddress)
            MenuRow(icon = Icons.Filled.History, title = "Historial de Mis Reportes", onClick = onReportHistory)
            MenuRow(icon = Icons.Filled.ContactPhone, title = "Contactos de auxilio", subtitle = "Registrar o administrar contactos", onClick = onManageContacts)
        }
    }
}