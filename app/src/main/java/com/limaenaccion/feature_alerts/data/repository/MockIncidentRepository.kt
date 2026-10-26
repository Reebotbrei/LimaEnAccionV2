package com.limaenaccion.feature_alerts.data.repository

import com.limaenaccion.feature_alerts.data.model.Incident
import com.limaenaccion.feature_alerts.data.model.IncidentStatus
import com.limaenaccion.feature_alerts.data.model.IncidentType

class MockIncidentRepository : IncidentRepository {

    private val incidents = listOf(
        Incident(
            id = "1",
            type = IncidentType.ROBO,
            title = "Robo al paso en Jr. Los Jardines",
            description = "Acaban de reportar un robo al paso en Jr. Los Jardines. El sospechoso huyó en una moto roja hacia la Av. Gran Chimú. Tengan cuidado por la zona.",
            reportedBy = "María Q.",
            handle = "@maria_vecina",
            isOfficial = false,
            location = "Caja de Agua, SJL",
            distanceMeters = 380,
            reportedAt = "12 min",
            status = "Reportado a Serenazgo",
            mapX = 0.22f,
            mapY = 0.28f,
            incidentStatus = IncidentStatus.REPORTADO,
            witnessCount = 4,
            commentsCount = 2,
            isVerifiedByAuthority = false
        ),
        Incident(
            id = "2",
            type = IncidentType.ACTIVIDAD_INUSUAL,
            title = "Manipulación de cerraduras en Av. Gran Chimú",
            description = "Se observa a una persona con herramientas intentando manipular cerraduras de viviendas deshabitadas cerca del parque. Hecho objetivo observable.",
            reportedBy = "Luis A.",
            handle = "@luis_cajadeagua",
            isOfficial = false,
            location = "Av. Gran Chimú",
            distanceMeters = 620,
            reportedAt = "45 min",
            status = "Patrulla de Serenazgo en camino",
            mapX = 0.68f,
            mapY = 0.32f,
            incidentStatus = IncidentStatus.EN_ATENCION,
            witnessCount = 6,
            commentsCount = 3,
            isVerifiedByAuthority = false
        ),
        Incident(
            id = "3",
            type = IncidentType.ACCIDENTE,
            title = "Patrullaje preventivo reforzado e intervención",
            description = "Patrullaje preventivo reforzado en Jr. Los Jardines y Av. Gran Chimú tras reportes de vecinos. Unidades de Serenazgo y PNP en el punto.",
            reportedBy = "Serenazgo SJL",
            handle = "@SerenazgoSJL",
            isOfficial = true,
            location = "Jr. Los Jardines, SJL",
            distanceMeters = 150,
            reportedAt = "2 h",
            status = "Intervención oficial en curso",
            mapX = 0.50f,
            mapY = 0.45f,
            incidentStatus = IncidentStatus.EN_ATENCION,
            witnessCount = 12,
            commentsCount = 5,
            isVerifiedByAuthority = true
        ),
        Incident(
            id = "4",
            type = IncidentType.INCENDIO,
            title = "Cortocircuito en poste de luz",
            description = "Cables de telefonía e internet haciendo chispas sobre la vereda. Cuadrilla municipal controló la situación.",
            reportedBy = "Carlos L.",
            handle = "@carlos_vecino",
            isOfficial = false,
            location = "Jr. Las Cantutas Cdra. 3, Caja de Agua",
            distanceMeters = 1200,
            reportedAt = "4 h",
            status = "Atendido y reparado",
            mapX = 0.55f,
            mapY = 0.55f,
            incidentStatus = IncidentStatus.RESUELTO,
            witnessCount = 8,
            commentsCount = 4,
            isVerifiedByAuthority = true
        ),
        Incident(
            id = "5",
            type = IncidentType.MEDICO,
            title = "Auxilio médico en losa deportiva",
            description = "Joven auxiliado tras fuerte caída durante partido. Trasladado a posta médica por ambulancia SAMU.",
            reportedBy = "Vecino vigilante",
            handle = "@vecino_alerta",
            isOfficial = false,
            location = "Losa Deportiva Caja de Agua",
            distanceMeters = 200,
            reportedAt = "ayer",
            status = "Atendido por SAMU",
            mapX = 0.35f,
            mapY = 0.72f,
            incidentStatus = IncidentStatus.RESUELTO,
            witnessCount = 5,
            commentsCount = 2,
            isVerifiedByAuthority = true
        )
    )

    override suspend fun getIncidents(): List<Incident> = incidents

    override suspend fun getIncidentById(id: String): Incident? = incidents.find { it.id == id }

    override fun observeIncidents(): kotlinx.coroutines.flow.Flow<List<Incident>> =
        kotlinx.coroutines.flow.flowOf(incidents)
}
