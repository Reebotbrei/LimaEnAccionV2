package com.limaenaccion.feature_alerts.data.model

enum class IncidentType(val label: String) {
    ROBO("Robo"),
    SOSPECHOSO("Sospechoso"),
    INCENDIO("Incendio"),
    MEDICO("Médico"),
    ACCIDENTE("Accidente")
}

data class Incident(
    val id: String,
    val type: IncidentType,
    val title: String,
    val description: String,
    val reportedBy: String,
    val location: String,
    val distanceMeters: Int,
    val reportedAt: String,   // texto simple ("hace 12 min") — sin fechas reales todavía
    val status: String,       // ej. "Reportado a Serenazgo · En investigación"
    val mapX: Float,          // posición normalizada (0f..1f) dentro del mapa simulado
    val mapY: Float
)