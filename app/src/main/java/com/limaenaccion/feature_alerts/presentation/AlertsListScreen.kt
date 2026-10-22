package com.limaenaccion.feature_alerts.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Notifications
import androidx.compose.material3.*
import androidx.compose.material3.TabRowDefaults.tabIndicatorOffset
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.util.LocalAppStrings
import com.limaenaccion.core.util.UiState

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AlertsListScreen(
    onIncidentClick: (String) -> Unit,
    viewModel: AlertsListViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val selectedTab by viewModel.selectedTab.collectAsState()
    val witnessedIds by viewModel.witnessedIncidentIds.collectAsState()
    val falsehoodIds by viewModel.falsehoodReportedIds.collectAsState()
    val strings = LocalAppStrings.current

    Scaffold(
        topBar = {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.surface)
            ) {
                // 1. Cabecera principal estilo mockup: "COMUNIDAD EN VIVO" + "Alertas" + Campana
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 14.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Column {
                        Text(
                            text = strings.liveCommunity,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.ExtraBold,
                            color = MaterialTheme.colorScheme.primary,
                            letterSpacing = 1.2.sp
                        )
                        Spacer(modifier = Modifier.height(2.dp))
                        Text(
                            text = strings.alertsTitle,
                            style = MaterialTheme.typography.headlineMedium,
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    // Botón circular con icono de campana
                    Box(
                        modifier = Modifier
                            .size(44.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.primaryContainer),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Filled.Notifications,
                            contentDescription = "Notificaciones activas",
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(22.dp)
                        )
                    }
                }

                // 2. Pestañas tipo Twitter: "Cerca de ti" | "Recientes"
                TabRow(
                    selectedTabIndex = if (selectedTab == AlertFeedTab.CERCA_DE_TI) 0 else 1,
                    containerColor = MaterialTheme.colorScheme.surface,
                    contentColor = MaterialTheme.colorScheme.primary,
                    indicator = { tabPositions ->
                        val index = if (selectedTab == AlertFeedTab.CERCA_DE_TI) 0 else 1
                        TabRowDefaults.SecondaryIndicator(
                            modifier = Modifier.tabIndicatorOffset(tabPositions[index]),
                            color = MaterialTheme.colorScheme.primary,
                            height = 3.dp
                        )
                    },
                    divider = {
                        HorizontalDivider(
                            color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f),
                            thickness = 0.8.dp
                        )
                    }
                ) {
                    Tab(
                        selected = selectedTab == AlertFeedTab.CERCA_DE_TI,
                        onClick = { viewModel.onTabSelected(AlertFeedTab.CERCA_DE_TI) },
                        text = {
                            Text(
                                text = strings.tabNearYou,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedTab == AlertFeedTab.CERCA_DE_TI) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == AlertFeedTab.CERCA_DE_TI) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                    Tab(
                        selected = selectedTab == AlertFeedTab.RECIENTES,
                        onClick = { viewModel.onTabSelected(AlertFeedTab.RECIENTES) },
                        text = {
                            Text(
                                text = strings.tabRecent,
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = if (selectedTab == AlertFeedTab.RECIENTES) FontWeight.Bold else FontWeight.Medium,
                                color = if (selectedTab == AlertFeedTab.RECIENTES) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                    )
                }

                // 3. Barra de estado en vivo: Punto verde + Distrito + Actualizado ahora
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 20.dp, vertical = 10.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF16A34A))
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = strings.liveUpdatedNow,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }

                HorizontalDivider(
                    color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.35f),
                    thickness = 0.6.dp
                )
            }
        }
    ) { paddingValues ->
        when (val state = uiState) {
            is UiState.Loading -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
                }
            }
            is UiState.Error -> {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(paddingValues)
                        .padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text(state.message, color = MaterialTheme.colorScheme.error)
                }
            }
            is UiState.Success -> {
                val incidents = state.data
                if (incidents.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues)
                            .padding(32.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "No se encontraron incidentes dentro del filtro seleccionado.",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                } else {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(paddingValues),
                        contentPadding = PaddingValues(bottom = 24.dp)
                    ) {
                        items(incidents, key = { it.id }) { incident ->
                            IncidentCard(
                                incident = incident,
                                onClick = { onIncidentClick(incident.id) },
                                hasWitnessed = witnessedIds.contains(incident.id),
                                onToggleWitness = { viewModel.onToggleWitness(incident.id) },
                                isReportedAsFalse = falsehoodIds.contains(incident.id),
                                onReportFalsehood = { viewModel.onReportFalsehood(incident.id) }
                            )
                        }
                    }
                }
            }
        }
    }
}
