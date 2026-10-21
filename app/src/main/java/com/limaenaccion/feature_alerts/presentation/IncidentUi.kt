package com.limaenaccion.feature_alerts.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsCar
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.material.icons.filled.ReportProblem
import androidx.compose.material.icons.filled.Warning
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.limaenaccion.feature_alerts.data.model.IncidentType

fun IncidentType.color(): Color = when (this) {
    IncidentType.ROBO -> Color(0xFFDC2626)
    IncidentType.SOSPECHOSO -> Color(0xFFEA580C)
    IncidentType.INCENDIO -> Color(0xFFD97706)
    IncidentType.MEDICO, IncidentType.EMERGENCIA_MEDICA -> Color(0xFF2563EB)
    IncidentType.ACCIDENTE -> Color(0xFF9333EA)
    IncidentType.VANDALISMO -> Color(0xFF7C3AED)
}

fun IncidentType.icon(): ImageVector = when (this) {
    IncidentType.ROBO -> Icons.Filled.Warning
    IncidentType.SOSPECHOSO -> Icons.Filled.PersonSearch
    IncidentType.INCENDIO -> Icons.Filled.LocalFireDepartment
    IncidentType.MEDICO, IncidentType.EMERGENCIA_MEDICA -> Icons.Filled.LocalHospital
    IncidentType.ACCIDENTE -> Icons.Filled.DirectionsCar
    IncidentType.VANDALISMO -> Icons.Filled.ReportProblem
}
