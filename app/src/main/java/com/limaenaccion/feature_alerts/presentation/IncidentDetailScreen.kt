package com.limaenaccion.feature_alerts.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Schedule
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.ui.components.PrimaryButton
import com.limaenaccion.core.util.UiState
import com.limaenaccion.feature_alerts.data.model.IncidentStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IncidentDetailScreen(
    onNavigateBack: () -> Unit,
    viewModel: IncidentDetailViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalle del incidente cívico") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
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
                Box(Modifier.fillMaxSize().padding(paddingValues).padding(24.dp), contentAlignment = Alignment.Center) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                val incident = state.data
                Column(modifier = Modifier.fillMaxSize().padding(paddingValues).verticalScroll(rememberScrollState())) {
                    Box(
                        modifier = Modifier.fillMaxWidth().height(180.dp).background(MaterialTheme.colorScheme.surfaceVariant),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(40.dp))
                            Spacer(modifier = Modifier.height(4.dp))
                            Text("Evidencia fotográfica adjunta", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                        }
                    }

                    Column(modifier = Modifier.padding(20.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Surface(shape = RoundedCornerShape(50), color = incident.type.color().copy(alpha = 0.15f)) {
                                Text(
                                    incident.type.label.uppercase(),
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = incident.type.color(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))

                            val (badgeBg, badgeFg) = when (incident.incidentStatus) {
                                IncidentStatus.REPORTADO -> Color(0xFFFEF3C7) to Color(0xFFB45309)
                                IncidentStatus.EN_ATENCION -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
                                IncidentStatus.RESUELTO -> Color(0xFFDCFCE7) to Color(0xFF15803D)
                            }
                            Surface(shape = RoundedCornerShape(50), color = badgeBg) {
                                Text(
                                    "${incident.incidentStatus.iconEmoji} ${incident.incidentStatus.label}",
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                                    color = badgeFg, style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.ExtraBold
                                )
                            }
                            Spacer(modifier = Modifier.width(8.dp))
                            Text("· ${incident.distanceMeters}m", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }

                        Spacer(modifier = Modifier.height(12.dp))
                        Text(incident.title, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(incident.description, style = MaterialTheme.typography.bodyMedium)

                        Spacer(modifier = Modifier.height(16.dp))
                        InfoRow(Icons.Filled.Schedule, incident.reportedAt)
                        Spacer(modifier = Modifier.height(4.dp))
                        InfoRow(Icons.Filled.LocationOn, incident.location)
                        Spacer(modifier = Modifier.height(4.dp))
                        InfoRow(Icons.Filled.Person, "Reportado por ${incident.reportedBy}")

                        Spacer(modifier = Modifier.height(16.dp))
                        Surface(shape = RoundedCornerShape(10.dp), color = MaterialTheme.colorScheme.tertiaryContainer) {
                            Text("ℹ Estado vecinal: ${incident.status}", modifier = Modifier.padding(10.dp), style = MaterialTheme.typography.labelSmall)
                        }

                        Spacer(modifier = Modifier.height(24.dp))
                        PrimaryButton(text = "Aportar testimonio o información", onClick = onNavigateBack)
                    }
                }
            }
        }
    }
}

@Composable
private fun InfoRow(icon: ImageVector, text: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = MaterialTheme.colorScheme.onSurfaceVariant, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}
