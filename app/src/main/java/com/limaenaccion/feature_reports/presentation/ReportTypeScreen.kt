package com.limaenaccion.feature_reports.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.limaenaccion.core.ui.components.PrimaryButton
import com.limaenaccion.feature_reports.data.model.ReportType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportTypeScreen(
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit,
    onContinue: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Nuevo reporte") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
                }
            )
        },
        bottomBar = {
            PrimaryButton(
                text = if (uiState.selectedType != null) "Continuar" else "Selecciona un tipo",
                onClick = onContinue,
                enabled = uiState.selectedType != null,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(horizontal = 16.dp)) {
            Spacer(modifier = Modifier.height(8.dp))
            Text("¿Qué tipo de incidente reportas?", style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = Modifier.weight(1f)
            ) {
                items(ReportType.entries) { type ->
                    ReportTypeCard(type = type, selected = uiState.selectedType == type, onClick = { viewModel.onTypeSelected(type) })
                }
            }
            Spacer(modifier = Modifier.height(8.dp))
        }
    }
}

@Composable
private fun ReportTypeCard(type: ReportType, selected: Boolean, onClick: () -> Unit) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = if (selected) type.color().copy(alpha = 0.1f) else MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, if (selected) type.color() else MaterialTheme.colorScheme.outlineVariant),
        onClick = onClick,
        modifier = Modifier.height(96.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize().padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(imageVector = type.icon(), contentDescription = null, tint = type.color(), modifier = Modifier.size(28.dp))
            Spacer(modifier = Modifier.height(6.dp))
            Text(type.label, textAlign = TextAlign.Center, style = MaterialTheme.typography.labelMedium, color = if (selected) type.color() else MaterialTheme.colorScheme.onSurface)
        }
    }
}