package com.limaenaccion.feature_emergency.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.ui.components.AddOptionCard
import com.limaenaccion.core.ui.components.EmergencyContactCard
import com.limaenaccion.core.ui.components.PrimaryButton
import com.limaenaccion.core.util.UiState
import com.limaenaccion.feature_emergency.data.model.EmergencyContactType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun EmergencyScreen(
    onNavigateToLogin: () -> Unit,
    onAddAuxiliaryContact: () -> Unit,
    viewModel: EmergencyViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val context = LocalContext.current

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Líneas de Emergencia") },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            PrimaryButton(
                text = "Iniciar Sesión",
                onClick = onNavigateToLogin,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        when (val state = uiState) {

            is UiState.Loading -> {
                Box(Modifier.fillMaxSize().padding(paddingValues), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }

            is UiState.Error -> {
                Box(
                    Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }

            is UiState.Success -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp),
                    contentPadding = PaddingValues(vertical = 16.dp)
                ) {
                    items(state.data, key = { it.id }) { contact ->
                        EmergencyContactCard(
                            name = contact.name,
                            phoneNumber = contact.phoneNumber,
                            isPrimary = contact.type == EmergencyContactType.CONTACTO_PRINCIPAL,
                            onCallClick = {
                                val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                                    data = Uri.parse("tel:${contact.phoneNumber}")
                                }
                                context.startActivity(dialIntent)
                            }
                        )
                    }
                    item {
                        AddOptionCard(
                            title = "Nuevo contacto de auxilio",
                            subtitle = "Agrega un número vecinal",
                            onClick = onAddAuxiliaryContact
                        )
                    }
                }
            }
        }
    }
}