package com.limaenaccion.feature_profile.data.model

import com.limaenaccion.feature_alerts.data.model.IncidentType

enum class ReportReviewStatus(val label: String) {
    VERIFICADO("Verificado"), EN_REVISION("En revisión"), RESUELTO("Resuelto")
}

data class ReportHistoryItem(
    val id: String,
    val type: IncidentType,
    val title: String,
    val dateLabel: String,
    val status: ReportReviewStatus
)