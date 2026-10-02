package com.limaenaccion.feature_reports.presentation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CarCrash
import androidx.compose.material.icons.filled.DirectionsRun
import androidx.compose.material.icons.filled.Handyman
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.LocalHospital
import androidx.compose.material.icons.filled.PersonSearch
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import com.limaenaccion.feature_reports.data.model.ReportType

fun ReportType.icon(): ImageVector = when (this) {
    ReportType.ROBO -> Icons.Filled.DirectionsRun
    ReportType.SOSPECHOSO -> Icons.Filled.PersonSearch
    ReportType.EMERGENCIA_MEDICA -> Icons.Filled.LocalHospital
    ReportType.INCENDIO -> Icons.Filled.LocalFireDepartment
    ReportType.ACCIDENTE -> Icons.Filled.CarCrash
    ReportType.VANDALISMO -> Icons.Filled.Handyman
}

fun ReportType.color(): Color = when (this) {
    ReportType.ROBO -> Color(0xFFDC2626)
    ReportType.SOSPECHOSO -> Color(0xFF7C3AED)
    ReportType.EMERGENCIA_MEDICA -> Color(0xFF2563EB)
    ReportType.INCENDIO -> Color(0xFFD97706)
    ReportType.ACCIDENTE -> Color(0xFF0891B2)
    ReportType.VANDALISMO -> Color(0xFF64748B)
}