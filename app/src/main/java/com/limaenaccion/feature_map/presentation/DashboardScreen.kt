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
import androidx.compose.material.icons.automirrored.filled.ViewList
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Notifications
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
import com.limaenaccion.core.util.LocalAppStrings
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
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
                Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween, verticalAlignment = Alignment.CenterVertically) {
                    Column {
                        Text("San Juan de Lurigancho", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        Text(strings.myNeighborhood, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                    }
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        AssistChip(onClick = onReportClick, label = { Text(strings.reportBtn) })
                        Spacer(modifier = Modifier.width(8.dp))
                        Icon(Icons.Filled.Notifications, contentDescription = "Alertas")
                    }
                }
                Spacer(modifier = Modifier.height(12.dp))
                ViewModeToggle(
                    selected = uiState.viewMode,
                    mapLabel = strings.viewMap,
                    listLabel = strings.viewList,
                    onSelectedChange = viewModel::onViewModeChange
                )
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
                    Text(strings.sosButton, color = Color.White, fontWeight = FontWeight.Bold, style = MaterialTheme.typography.titleMedium)
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
private fun ViewModeToggle(
    selected: DashboardViewMode,
    mapLabel: String,
    listLabel: String,
    onSelectedChange: (DashboardViewMode) -> Unit
) {
    Surface(shape = RoundedCornerShape(50), color = MaterialTheme.colorScheme.surfaceVariant) {
        Row(modifier = Modifier.padding(4.dp)) {
            ToggleChip(mapLabel, Icons.Filled.Map, selected == DashboardViewMode.MAP) { onSelectedChange(DashboardViewMode.MAP) }
            ToggleChip(listLabel, Icons.AutoMirrored.Filled.ViewList, selected == DashboardViewMode.LIST) { onSelectedChange(DashboardViewMode.LIST) }
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
    BoxWithConstraints(modifier = Modifier.fillMaxSize().background(Color(0xFFE5E7EB))) {
        val width = constraints.maxWidth.toFloat()
        val height = constraints.maxHeight.toFloat()

        Canvas(modifier = Modifier.fillMaxSize()) {
            val roadColor = Color.White
            drawLine(color = roadColor, start = Offset(0f, height * 0.4f), end = Offset(width, height * 0.4f), strokeWidth = 14f)
            drawLine(color = roadColor, start = Offset(0f, height * 0.7f), end = Offset(width, height * 0.7f), strokeWidth = 10f)
            drawLine(color = roadColor, start = Offset(width * 0.35f, 0f), end = Offset(width * 0.35f, height), strokeWidth = 14f)
            drawLine(color = roadColor, start = Offset(width * 0.75f, 0f), end = Offset(width * 0.75f, height), strokeWidth = 10f)
        }

        Box(
            modifier = Modifier
                .offset((width * 0.5f / 2.75f).dp, (height * 0.5f / 2.75f).dp)
                .size(16.dp)
                .clip(CircleShape)
                .background(Color(0xFF2563EB))
        )

        incidents.forEach { incident ->
            val pinX = (width * incident.mapX / 2.75f).dp
            val pinY = (height * incident.mapY / 2.75f).dp
            Box(
                modifier = Modifier
                    .offset(pinX, pinY)
                    .size(36.dp)
                    .clip(CircleShape)
                    .background(incident.type.color())
                    .clickable { onPinClick(incident.id) },
                contentAlignment = Alignment.Center
            ) {
                Icon(imageVector = incident.type.icon(), contentDescription = incident.type.label, tint = Color.White, modifier = Modifier.size(20.dp))
            }
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
