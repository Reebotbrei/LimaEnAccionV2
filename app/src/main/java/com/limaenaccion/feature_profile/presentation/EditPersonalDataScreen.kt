package com.limaenaccion.feature_profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.ui.components.DropdownField
import com.limaenaccion.core.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EditPersonalDataScreen(
    onNavigateBack: () -> Unit,
    viewModel: EditPersonalDataViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.savedChanges) { if (uiState.savedChanges) onNavigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Editar datos personales") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") } }
            )
        },
        bottomBar = { PrimaryButton(text = "Guardar cambios", onClick = viewModel::onSaveClicked, modifier = Modifier.padding(16.dp)) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 24.dp)) {
            Spacer(modifier = Modifier.height(8.dp))
            Box(
                modifier = Modifier.align(Alignment.CenterHorizontally).size(72.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))

            Text("Nombre completo", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = uiState.fullName, onValueChange = viewModel::onFullNameChange, singleLine = true, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))
            Text("DNI", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = uiState.dni, onValueChange = {}, singleLine = true, enabled = false, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))
            Text("Género", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            DropdownField(
                selectedValue = uiState.gender,
                placeholder = "Selecciona tu género",
                options = listOf("Masculino", "Femenino"),
                onOptionSelected = viewModel::onGenderChange
            )

            Spacer(modifier = Modifier.height(16.dp))
            Text("Correo electrónico", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = uiState.email, onValueChange = viewModel::onEmailChange, singleLine = true, modifier = Modifier.fillMaxWidth())

            Spacer(modifier = Modifier.height(16.dp))
            Text("Número de celular", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(value = uiState.phoneNumber, onValueChange = viewModel::onPhoneNumberChange, singleLine = true, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}