package com.limaenaccion.feature_profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material.icons.filled.WarningAmber
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.ui.components.DropdownField
import com.limaenaccion.core.ui.components.PrimaryButton
import com.limaenaccion.feature_auth.presentation.MOCK_DISTRICTS
import com.limaenaccion.feature_auth.presentation.MOCK_NEIGHBORHOODS

@Composable
fun UpdateAddressScreen(
    onNavigateBack: () -> Unit,
    viewModel: UpdateAddressViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.changesSaved) { if (uiState.changesSaved) onNavigateBack() }

    Scaffold(
        topBar = {
            IconButton(onClick = onNavigateBack, modifier = Modifier.padding(8.dp)) {
                Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
            }
        },
        bottomBar = {
            PrimaryButton(text = "Guardar cambios", onClick = viewModel::onSaveClicked, enabled = uiState.isPhoneVerified, modifier = Modifier.padding(16.dp))
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp)) {
            Text("Actualizar domicilio", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text("El cambio de zona requiere verificación. Solo podrás actualizar tu domicilio una vez por mes.", color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(16.dp))

            Surface(shape = RoundedCornerShape(12.dp), color = Color(0xFFFEF3C7), border = BorderStroke(1.dp, Color(0xFFF59E0B))) {
                Row(modifier = Modifier.fillMaxWidth().padding(12.dp)) {
                    Icon(Icons.Filled.WarningAmber, contentDescription = null, tint = Color(0xFFB45309), modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        buildAnnotatedString {
                            append("Tu zona actual: ")
                            withStyle(SpanStyle(fontWeight = FontWeight.Bold)) { append(uiState.currentZone) }
                            append(". Al cambiar de zona perderás tu historial de alertas local.")
                        },
                        style = MaterialTheme.typography.labelSmall,
                        color = Color(0xFF92400E)
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("Nuevo Distrito", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            DropdownField(icon = Icons.Filled.Map, selectedValue = uiState.newDistrict, placeholder = "Selecciona tu distrito", options = MOCK_DISTRICTS, onOptionSelected = viewModel::onDistrictChange)

            Spacer(modifier = Modifier.height(16.dp))
            Text("Nuevo Vecindario", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            DropdownField(icon = Icons.Filled.Home, selectedValue = uiState.newNeighborhood, placeholder = "Selecciona tu vecindario", options = MOCK_NEIGHBORHOODS, onOptionSelected = viewModel::onNeighborhoodChange)

            Spacer(modifier = Modifier.height(20.dp))
            if (uiState.isPhoneVerified) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Número verificado", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                }
            } else {
                Surface(shape = RoundedCornerShape(50), border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary), onClick = viewModel::onVerifyWithSmsClicked, modifier = Modifier.fillMaxWidth()) {
                    Row(modifier = Modifier.fillMaxWidth().padding(vertical = 12.dp), horizontalArrangement = Arrangement.Center, verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Verificar con SMS", color = MaterialTheme.colorScheme.primary, fontWeight = FontWeight.Bold)
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}