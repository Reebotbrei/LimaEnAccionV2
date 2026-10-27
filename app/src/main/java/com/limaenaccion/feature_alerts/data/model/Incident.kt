package com.limaenaccion.feature_alerts.data.model

enum class IncidentType(val label: String) {
    ROBO("Robo"),
    ACTIVIDAD_INUSUAL("Actividad Inusual"),
    INCENDIO("Incendio"),
    MEDICO("Médico"),
    ACCIDENTE("Accidente"),
    VANDALISMO("Vandalismo"),
    EMERGENCIA_MEDICA("Emergencia Médica")
}

enum class IncidentStatus(val label: String, val iconEmoji: String) {
    REPORTADO("Reportado", "🟡"),
    EN_ATENCION("En atención", "🔵"),
    RESUELTO("Resuelto", "🟢")
}

data class Incident(
    val id: String,
    val type: IncidentType,
    val title: String,
    val description: String,
    val reportedBy: String,
    val location: String,
    val distanceMeters: Int,
    val reportedAt: String,   // ej. "12 min", "hace 45 min"
    val status: String,       // ej. "Reportado a Serenazgo · En investigación"
    val mapX: Float,          // posición normalizada (0f..1f) dentro del mapa simulado
    val mapY: Float,
    val handle: String = "@vecino_sjl",
    val isOfficial: Boolean = false,
    val photoBase64: String? = null,
    val incidentStatus: IncidentStatus = IncidentStatus.REPORTADO,
    val witnessCount: Int = 3,
    val hasWitnessed: Boolean = false,
    val commentsCount: Int = 4,
    val isVerifiedByAuthority: Boolean = false,
    val isReportedAsFalse: Boolean = false,
    val latitude: Double = -12.019542,
    val longitude: Double = -77.014231
)
