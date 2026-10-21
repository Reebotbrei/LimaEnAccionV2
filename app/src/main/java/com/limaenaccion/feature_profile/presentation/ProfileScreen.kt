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
import com.limaenaccion.core.util.LocalAppStrings

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
    val strings = LocalAppStrings.current

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
                "${strings.dniLabel}: ${uiState.dni} · ${strings.currentDistrictLabel}: ${uiState.district}",
                color = MaterialTheme.colorScheme.onPrimary.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall
            )
        }

        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            Text(strings.accountSettingsSection, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            MenuRow(icon = Icons.Filled.Person, title = strings.editPersonalData, onClick = onEditPersonalData)
            MenuRow(icon = Icons.Filled.PhotoCamera, title = strings.changeProfilePhoto, onClick = onChangePhoto)
            MenuRow(icon = Icons.Filled.LocationCity, title = strings.changeAddress, onClick = onUpdateAddress)
            MenuRow(icon = Icons.Filled.History, title = strings.reportHistory, onClick = onReportHistory)
            MenuRow(icon = Icons.Filled.ContactPhone, title = strings.auxiliaryContacts, subtitle = strings.manageContactsSubtitle, onClick = onManageContacts)
        }
    }
}
