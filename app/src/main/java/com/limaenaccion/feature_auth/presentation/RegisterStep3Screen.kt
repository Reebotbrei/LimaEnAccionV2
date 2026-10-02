package com.limaenaccion.feature_auth.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.limaenaccion.core.ui.components.DropdownField
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.limaenaccion.core.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterStep3Screen(
    viewModel: RegisterViewModel,
    onNavigateBack: () -> Unit,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.registrationFinished) {
        if (uiState.registrationFinished) onFinish()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Crear cuenta"); Text("Paso 3 de 3", style = MaterialTheme.typography.labelSmall) } },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            PrimaryButton(text = "Finalizar Registro", onClick = viewModel::onFinishRegistrationClicked, modifier = Modifier.padding(16.dp))
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp)) {
            Text("Zona de Residencia", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Por favor, indica tu distrito y vecindario actual. Esto nos ayuda a conectarte con la junta vecinal y serenazgo correcto.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(modifier = Modifier.height(20.dp))

            Text("Distrito *", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            DropdownField(
                icon = Icons.Filled.Map,
                selectedValue = uiState.district,
                placeholder = "Selecciona tu distrito",
                options = MOCK_DISTRICTS,
                isError = uiState.districtError != null,
                errorMessage = uiState.districtError,
                onOptionSelected = viewModel::onDistrictChange
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Urbanización / Vecindario *", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            DropdownField(
                icon = Icons.Filled.Home,
                selectedValue = uiState.neighborhood,
                placeholder = "Selecciona tu urbanización",
                options = MOCK_NEIGHBORHOODS,
                isError = uiState.neighborhoodError != null,
                errorMessage = uiState.neighborhoodError,
                onOptionSelected = viewModel::onNeighborhoodChange
            )

            if (uiState.district != null) {
                Spacer(modifier = Modifier.height(20.dp))
                Surface(shape = RoundedCornerShape(12.dp), color = MaterialTheme.colorScheme.primaryContainer) {
                    Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("Tu zona tiene una red de seguridad activa de 1,240 vecinos alertas.", style = MaterialTheme.typography.bodyMedium)
                    }
                }
            }
        }
    }
}
