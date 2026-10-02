package com.limaenaccion.feature_profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Phone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.ui.components.PrimaryButton
import com.limaenaccion.feature_profile.data.model.ContactRelationship

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddAuxiliaryContactScreen(
    onContactSaved: () -> Unit,
    onNavigateBack: () -> Unit,
    viewModel: AddAuxiliaryContactViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.contactSaved) {
        if (uiState.contactSaved) onContactSaved()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo Contacto de Auxilio") },
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
                text = "Guardar Contacto",
                onClick = viewModel::onSaveClicked,
                enabled = uiState.isFormValid,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Surface(color = MaterialTheme.colorScheme.primaryContainer, shape = MaterialTheme.shapes.medium) {
                Text(
                    text = "Asigna contactos de confianza para alertar rápidamente por SOS o en caso de una emergencia en tu zona.",
                    modifier = Modifier.padding(12.dp),
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(20.dp))
            Text("DATOS DEL CONTACTO", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))

            OutlinedTextField(
                value = uiState.fullName,
                onValueChange = viewModel::onFullNameChange,
                label = { Text("Nombre Completo *") },
                leadingIcon = { Icon(Icons.Filled.Person, contentDescription = null) },
                placeholder = { Text("Ej. Luz Cachuda") },
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Parentesco o Relación", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(8.dp))
            LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                items(ContactRelationship.entries) { relationship ->
                    FilterChip(
                        selected = uiState.relationship == relationship,
                        onClick = { viewModel.onRelationshipChange(relationship) },
                        label = { Text(relationship.label) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            OutlinedTextField(
                value = uiState.phoneNumber,
                onValueChange = viewModel::onPhoneNumberChange,
                label = { Text("Número de Teléfono *") },
                leadingIcon = { Icon(Icons.Filled.Phone, contentDescription = null) },
                placeholder = { Text("Ej. 987654321") },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(24.dp))
            Text("PREFERENCIAS DE ALERTA", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.primary)
            Spacer(modifier = Modifier.height(8.dp))
            Surface(
                shape = MaterialTheme.shapes.medium,
                color = MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text("Contacto de Auxilio Principal", style = MaterialTheme.typography.bodyLarge)
                        Text(
                            "Llamar automáticamente al presionar el botón de pánico SOS.",
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(checked = uiState.isPrimaryContact, onCheckedChange = viewModel::onIsPrimaryContactChange)
                }
            }

            if (!uiState.isFormValid && (uiState.fullName.isNotEmpty() || uiState.phoneNumber.isNotEmpty())) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Completa el nombre y un teléfono válido (mínimo 6 dígitos) para poder guardar.",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.labelSmall
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}