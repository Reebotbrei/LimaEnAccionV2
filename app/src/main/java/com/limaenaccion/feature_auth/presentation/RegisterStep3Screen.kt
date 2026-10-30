package com.limaenaccion.feature_auth.presentation

import android.Manifest
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Shield
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.limaenaccion.core.ui.components.DropdownField
import com.limaenaccion.core.ui.components.PrimaryButton
import com.limaenaccion.core.util.LimaLocations

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RegisterStep3Screen(
    viewModel: RegisterViewModel,
    onNavigateBack: () -> Unit,
    onFinish: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    // Solicitud de permisos para sugerencia de ubicación por GPS
    val locationPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        if (permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
            permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        ) {
            viewModel.onSuggestLocationFromGps(context)
        }
    }

    LaunchedEffect(uiState.registrationFinished) {
        if (uiState.registrationFinished) onFinish()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Crear cuenta")
                        Text("Paso 3 de 3 · Zona de Residencia", style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            PrimaryButton(
                text = "Finalizar Registro",
                onClick = viewModel::onFinishRegistrationClicked,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(24.dp)
        ) {
            Text(
                "Zona de Residencia en Lima",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                "Indica tu distrito y urbanización. Esto te conectará con el serenazgo y los vecinos alertas de tu sector.",
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                style = MaterialTheme.typography.bodyMedium
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Botón de sugerencia automática con GPS ──
            OutlinedButton(
                onClick = {
                    locationPermissionLauncher.launch(
                        arrayOf(
                            Manifest.permission.ACCESS_FINE_LOCATION,
                            Manifest.permission.ACCESS_COARSE_LOCATION
                        )
                    )
                },
                shape = RoundedCornerShape(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                if (uiState.isLocatingGps) {
                    CircularProgressIndicator(modifier = Modifier.size(16.dp), strokeWidth = 2.dp)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Obteniendo posición GPS...", style = MaterialTheme.typography.labelMedium)
                } else {
                    Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Sugerir zona con mi ubicación actual", style = MaterialTheme.typography.labelMedium)
                }
            }

            if (!uiState.gpsSuggestionFeedback.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.LocationOn,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(18.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = uiState.gpsSuggestionFeedback.orEmpty(),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // ── Selector de Distrito (43 Distritos de Lima) ──
            Text("Distrito de Lima *", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            DropdownField(
                icon = Icons.Filled.Map,
                selectedValue = uiState.district,
                placeholder = "Selecciona tu distrito",
                options = LimaLocations.ALL_DISTRICT_NAMES,
                isError = uiState.districtError != null,
                errorMessage = uiState.districtError,
                onOptionSelected = viewModel::onDistrictChange
            )

            Spacer(modifier = Modifier.height(16.dp))

            // ── Selector de Urbanización o Barrio ──
            Text("Urbanización / Barrio *", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            val neighborhoods = if (!uiState.district.isNullOrBlank()) {
                uiState.availableNeighborhoods.ifEmpty { LimaLocations.getNeighborhoods(uiState.district) }
            } else {
                listOf(LimaLocations.OTRO_BARRIO)
            }

            val currentSelectedOption = if (uiState.isCustomNeighborhood) {
                LimaLocations.OTRO_BARRIO
            } else {
                uiState.neighborhood
            }

            DropdownField(
                icon = Icons.Filled.Home,
                selectedValue = currentSelectedOption,
                placeholder = if (uiState.district.isNullOrBlank()) "Primero selecciona un distrito" else "Selecciona tu urbanización",
                options = neighborhoods,
                isError = uiState.neighborhoodError != null && !uiState.isCustomNeighborhood,
                errorMessage = if (!uiState.isCustomNeighborhood) uiState.neighborhoodError else null,
                onOptionSelected = viewModel::onNeighborhoodChange
            )

            // ── Campo de texto libre si seleccionó "Otro" ──
            if (uiState.isCustomNeighborhood) {
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = uiState.customNeighborhoodText,
                    onValueChange = viewModel::onCustomNeighborhoodChange,
                    label = { Text("Escribe tu urbanización o barrio") },
                    placeholder = { Text("Ej. Urb. Las Palmeras, Sector 4, Asoc. Los Pinos") },
                    singleLine = true,
                    isError = uiState.neighborhoodError != null,
                    supportingText = uiState.neighborhoodError?.let { { Text(it) } },
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (!uiState.district.isNullOrBlank()) {
                Spacer(modifier = Modifier.height(24.dp))
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(14.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Shield,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            "Comunidad conectada: Monitoreo activo y alertas vecinales coordinadas para ${uiState.district}.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onPrimaryContainer
                        )
                    }
                }
            }
        }
    }
}
