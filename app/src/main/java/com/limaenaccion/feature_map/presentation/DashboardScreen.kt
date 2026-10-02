package com.limaenaccion.feature_map.presentation

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material.icons.filled.ViewList
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.ui.components.PrimaryButton
import com.limaenaccion.core.util.UiState
import com.limaenaccion.feature_alerts.data.model.Incident
import com.limaenaccion.feature_alerts.presentation.IncidentCard
import com.limaenaccion.feature_alerts.presentation.color
import com.limaenaccion.feature_alerts.presentation.icon

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DashboardScreen(
    onSosClick: () -> Unit,
    onNavigateToIncidentDetail: (String) -> Unit,
    onReportClick: () -> Unit,
    viewModel: DashboardViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("San Juan de Lurigancho", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text("Mi Vecindario", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AssistChip(onClick = onReportClick, label = { Text("+ Reportar") })
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Filled.Notifications, contentDescription = "Alertas")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                ViewModeToggle(selected = uiState.viewMode, onSelectedChange = viewModel::onViewModeChange)
            }
        },
        bottomBar = {
            Surface(shadowElevation = 4.dp) {
                Button(
                    onClick = onSosClick,
                    modifier = Modifier.fillMaxWidth().padding(16.dp).height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color.White)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("BOTÓN SOS", color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
                }
            }
        }
    ) { paddingValues ->
        Box(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            when (val state = uiState.incidentsState) {
                is UiState.Loading -> {
                    Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                    }
                }
                is UiState.Error -> {
                    Box(Modifier.fillMaxSize().padding(24.dp), contentAlignment = Alignment.Center) {
                        Text(state.message, color = MaterialTheme.colorScheme.error)
                    }
                }
                is UiState.Success -> {
                    if (uiState.viewMode == DashboardViewMode.MAP) {
                        SimulatedMap(incidents = state.data, onPinClick = viewModel::onPinClick)
                    } else {
                        LazyColumn(
                            modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                            verticalArrangement = Arrangement.spacedBy(10.dp),
                            contentPadding = PaddingValues(top = 12.dp, bottom = 16.dp)
                        ) {
                            items(state.data, key = { it.id }) { incident ->
                                IncidentCard(incident = incident, onClick = { onNavigateToIncidentDetail(incident.id) })
                            }
                        }
                    }

                    val selectedIncident = state.data.find { it.id == uiState.selectedIncidentId }
                    if (selectedIncident != null) {
                        ModalBottomSheet(onDismissRequest = viewModel::onDismissBottomSheet) {
                            IncidentSummarySheet(
                                incident = selectedIncident,
                                onViewFullDetailClick = {
                                    viewModel.onDismissBottomSheet()
                                    onNavigateToIncidentDetail(selectedIncident.id)
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ViewModeToggle(selected: DashboardViewMode, onSelectedChange: (DashboardViewMode) -> Unit) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(modifier = Modifier.padding(4.dp)) {
            ToggleChip("Mapa", Icons.Filled.Map, selected == DashboardViewMode.MAP) { onSelectedChange(DashboardViewMode.MAP) }
            ToggleChip("Lista", Icons.Filled.ViewList, selected == DashboardViewMode.LIST) { onSelectedChange(DashboardViewMode.LIST) }
        }
    }
}

@Composable
private fun ToggleChip(text: String, icon: ImageVector, selected: Boolean, onClick: () -> Unit) {
    Surface(shape = RoundedCornerShape(50), color = if (selected) MaterialTheme.colorScheme.primary else Color.Transparent, onClick = onClick) {
        Row(modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(icon, contentDescription = null, tint = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.width(4.dp))
            Text(text, color = if (selected) Color.White else MaterialTheme.colorScheme.onSurfaceVariant)
        }
    }
}

@Composable
private fun SimulatedMap(incidents: List<Incident>, onPinClick: (String) -> Unit) {
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color(0xFFE8EDE9))) {
        val mapWidth = maxWidth
        val mapHeight = maxHeight

        // Cuadrícula simulada, sustituye a un mapa real por ahora.
        Canvas(modifier = Modifier.fillMaxSize()) {
            val step = 60f
            var x = 0f
            while (x < size.width) {
                drawLine(Color(0xFFD0D8D2), Offset(x, 0f), Offset(x, size.height), strokeWidth = 1f)
                x += step
            }
            var y = 0f
            while (y < size.height) {
                drawLine(Color(0xFFD0D8D2), Offset(0f, y), Offset(size.width, y), strokeWidth = 1f)
                y += step
            }
        }

        incidents.forEach { incident ->
            Box(
                modifier = Modifier
                    .offset(x = mapWidth * incident.mapX - 16.dp, y = mapHeight * incident.mapY - 16.dp)
                    .size(32.dp)
                    .clip(CircleShape)
                    .background(incident.type.color())
                    .clickable { onPinClick(incident.id) },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = incident.type.icon(), contentDescription = incident.title, tint = Color.White, modifier = Modifier.size(18.dp))
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.BottomEnd).padding(12.dp),
            shape = RoundedCornerShape(50),
            color = Color.White,
            shadowElevation = 2.dp
        ) {
            Text("${incidents.size} incidentes", modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp), style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
private fun IncidentSummarySheet(incident: Incident, onViewFullDetailClick: () -> Unit) {
    Column(modifier = Modifier.padding(20.dp).padding(bottom = 24.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Surface(shape = RoundedCornerShape(50), color = incident.type.color().copy(alpha = 0.15f)) {
                Text(
                    incident.type.label.uppercase(),
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                    color = incident.type.color(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.width(8.dp))
            Text(incident.reportedAt, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(incident.title, style = MaterialTheme.typography.titleMedium, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(4.dp))
        Text(incident.description, style = MaterialTheme.typography.bodyMedium, maxLines = 3, overflow = TextOverflow.Ellipsis)
        Spacer(modifier = Modifier.height(4.dp))
        Text("A ${incident.distanceMeters}m de tu ubicación", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Spacer(modifier = Modifier.height(16.dp))
        PrimaryButton(text = "Ver detalle completo", onClick = onViewFullDetailClick)
    }
}