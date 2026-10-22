package com.limaenaccion.feature_reports.data.model

enum class ReportTimeframe(val label: String, val subtitle: String) {
    AHORA("Ocurriendo Ahora", "En tiempo real · Alerta urgente"),
    ANTES("Ocurrió Antes", "Hecho pasado · Registro preventivo")
}
