package com.limaenaccion.feature_profile.presentation

import androidx.lifecycle.ViewModel
import com.limaenaccion.feature_alerts.data.model.IncidentType
import com.limaenaccion.feature_profile.data.model.ReportHistoryItem
import com.limaenaccion.feature_profile.data.model.ReportReviewStatus
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ReportHistoryViewModel : ViewModel() {
    // Sólo maqueta, estamos agregando datos simulados localmente
    private val _uiState = MutableStateFlow(
        listOf(
            ReportHistoryItem("1", IncidentType.ROBO, "Robo al paso en Jr. Los Jardines", "12 may 2025, 10:23 AM", ReportReviewStatus.VERIFICADO),
            ReportHistoryItem("2", IncidentType.SOSPECHOSO, "Persona merodeando Av. Gran Chi...", "10 may 2025, 9:15 PM", ReportReviewStatus.EN_REVISION),
            ReportHistoryItem("3", IncidentType.INCENDIO, "Incendio en calle Santa Rosa", "8 may 2025, 8:00 AM", ReportReviewStatus.RESUELTO),
            ReportHistoryItem("4", IncidentType.MEDICO, "Adulto mayor caído en vereda", "5 may 2025, 3:44 PM", ReportReviewStatus.VERIFICADO)
        )
    )
    val uiState: StateFlow<List<ReportHistoryItem>> = _uiState.asStateFlow()
}