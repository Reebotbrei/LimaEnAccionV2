package com.limaenaccion.feature_profile.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.feature_alerts.presentation.color
import com.limaenaccion.feature_alerts.presentation.icon
import com.limaenaccion.feature_profile.data.model.ReportHistoryItem
import com.limaenaccion.feature_profile.data.model.ReportReviewStatus

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportHistoryScreen(
    onNavigateBack: () -> Unit,
    viewModel: ReportHistoryViewModel = viewModel()
) {
    val reports by viewModel.uiState.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Historial de Reportes") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") } }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues)) {
            Text(
                "${reports.size} reportes publicados",
                style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
            )
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(10.dp),
                contentPadding = PaddingValues(bottom = 16.dp)
            ) {
                items(reports, key = { it.id }) { report -> ReportHistoryCard(report) }
            }
        }
    }
}

/** Variante ligera de IncidentCard (distancia → badge de estado); reutiliza IncidentType.color()/icon(). */
@Composable
private fun ReportHistoryCard(report: ReportHistoryItem) {
    Surface(shape = RoundedCornerShape(16.dp), shadowElevation = 1.dp, color = MaterialTheme.colorScheme.surface, modifier = Modifier.fillMaxWidth()) {
        Row(modifier = Modifier.fillMaxWidth().padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
            Icon(imageVector = report.type.icon(), contentDescription = null, tint = report.type.color(), modifier = Modifier.size(24.dp))
            Spacer(modifier = Modifier.width(10.dp))
            Column(modifier = Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(report.type.label, color = report.type.color(), style = MaterialTheme.typography.labelSmall, fontWeight = FontWeight.Bold)
                    Spacer(modifier = Modifier.weight(1f))
                    StatusBadge(report.status)
                }
                Text(report.title, style = MaterialTheme.typography.bodyLarge)
                Text(report.dateLabel, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
        }
    }
}

@Composable
private fun StatusBadge(status: ReportReviewStatus) {
    val (bg, fg) = when (status) {
        ReportReviewStatus.VERIFICADO -> MaterialTheme.colorScheme.primaryContainer to MaterialTheme.colorScheme.primary
        ReportReviewStatus.EN_REVISION -> Color(0xFFFEF3C7) to Color(0xFFB45309)
        ReportReviewStatus.RESUELTO -> Color(0xFFDBEAFE) to Color(0xFF1D4ED8)
    }
    Surface(shape = RoundedCornerShape(50), color = bg) {
        Text(status.label, modifier = Modifier.padding(horizontal = 8.dp, vertical = 2.dp), style = MaterialTheme.typography.labelSmall, color = fg)
    }
}