package com.limaenaccion.feature_alerts.data.repository

import com.limaenaccion.feature_alerts.data.model.Incident
import com.limaenaccion.feature_alerts.data.model.IncidentType

class MockIncidentRepository : IncidentRepository {

    private val incidents = listOf(
        Incident(
            id = "1", type = IncidentType.ROBO,
            title = "Robo al paso cerca al Mercado",
            description = "Sujeto a bordo de motocicleta lineal sin placa arrebató el celular a un joven. Huyó en dirección a la Av. Próceres.",
            reportedBy = "María Q.",
            location = "Mercado Modelo de Caja de Agua",
            distanceMeters = 380, reportedAt = "hace 12 minutos",
            status = "Reportado a Serenazgo", mapX = 0.22f, mapY = 0.28f
        ),
        Incident(
            id = "2", type = IncidentType.SOSPECHOSO,
            title = "Merodeador en la Estación",
            description = "Sujeto con gorra negra y actitud sospechosa observando a los pasajeros que salen con celulares en la mano.",
            reportedBy = "Vecino anónimo",
            location = "Exteriores Estación Caja de Agua (Línea 1)",
            distanceMeters = 620, reportedAt = "hace 50 minutos",
            status = "Pendiente de verificación", mapX = 0.68f, mapY = 0.32f
        ),
        Incident(
            id = "3", type = IncidentType.INCENDIO,
            title = "Cortocircuito en poste de luz",
            description = "Cables de telefonía e internet están haciendo cortocircuito y botando chispas sobre la vereda. ¡Peligro para los peatones!",
            reportedBy = "Carlos L.",
            location = "Jr. Las Cantutas Cdra. 3, Caja de Agua",
            distanceMeters = 1200, reportedAt = "hace 8 horas",
            status = "Atendido por Enel", mapX = 0.55f, mapY = 0.55f
        ),
        Incident(
            id = "4", type = IncidentType.MEDICO,
            title = "Caída fuerte en losa deportiva",
            description = "Joven sufrió una fuerte caída durante un partido y parece tener una fractura en el tobillo. No puede levantarse.",
            reportedBy = "Vecino anónimo",
            location = "Losa Deportiva Caja de Agua",
            distanceMeters = 200, reportedAt = "ayer",
            status = "Atendido por SAMU", mapX = 0.35f, mapY = 0.72f
        ),
        Incident(
            id = "5", type = IncidentType.ACCIDENTE,
            title = "Choque leve en Av. Lima",
            description = "Mototaxi chocó contra un auto particular que estaba estacionado. Tráfico ligero en la zona, están discutiendo.",
            reportedBy = "Juan P.",
            location = "Av. Lima Cdra. 4 (cerca a la posta)",
            distanceMeters = 850, reportedAt = "hace 2 horas",
            status = "Resuelto", mapX = 0.45f, mapY = 0.60f
        )
    )

    override suspend fun getIncidents(): List<Incident> = incidents
    override suspend fun getIncidentById(id: String): Incident? = incidents.find { it.id == id }
}