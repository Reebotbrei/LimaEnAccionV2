package com.limaenaccion.feature_alerts

import com.limaenaccion.feature_alerts.data.model.Incident
import com.limaenaccion.feature_alerts.data.model.IncidentStatus
import com.limaenaccion.feature_alerts.data.model.IncidentType
import org.junit.Assert.*
import org.junit.Test

class CivicFeedUnitTest {

    @Test
    fun testActividadInusualCategory() {
        assertEquals("Actividad Inusual", IncidentType.ACTIVIDAD_INUSUAL.label)
        assertEquals("Robo", IncidentType.ROBO.label)
    }

    @Test
    fun testIncidentStatusBadges() {
        assertEquals("Reportado", IncidentStatus.REPORTADO.label)
        assertEquals("🟡", IncidentStatus.REPORTADO.iconEmoji)

        assertEquals("En atención", IncidentStatus.EN_ATENCION.label)
        assertEquals("🔵", IncidentStatus.EN_ATENCION.iconEmoji)

        assertEquals("Resuelto", IncidentStatus.RESUELTO.label)
        assertEquals("🟢", IncidentStatus.RESUELTO.iconEmoji)
    }

    @Test
    fun testWitnessCountIncrements() {
        val incident = Incident(
            id = "test_1",
            type = IncidentType.ROBO,
            title = "Prueba de robo",
            description = "Descripción de prueba",
            reportedBy = "Vecino Test",
            location = "SJL",
            distanceMeters = 100,
            reportedAt = "5 min",
            status = "Reportado",
            mapX = 0.5f,
            mapY = 0.5f,
            incidentStatus = IncidentStatus.REPORTADO,
            witnessCount = 3
        )

        assertEquals(3, incident.witnessCount)
        val witnessedIncident = incident.copy(
            hasWitnessed = true,
            witnessCount = incident.witnessCount + 1
        )
        assertEquals(4, witnessedIncident.witnessCount)
        assertTrue(witnessedIncident.hasWitnessed)
    }

    @Test
    fun testFalsehoodReporting() {
        val incident = Incident(
            id = "test_2",
            type = IncidentType.ACTIVIDAD_INUSUAL,
            title = "Reporte dudoso",
            description = "Descripción",
            reportedBy = "Vecino Test",
            location = "SJL",
            distanceMeters = 200,
            reportedAt = "10 min",
            status = "Reportado",
            mapX = 0.5f,
            mapY = 0.5f,
            isReportedAsFalse = false
        )

        assertFalse(incident.isReportedAsFalse)
        val flagged = incident.copy(isReportedAsFalse = true)
        assertTrue(flagged.isReportedAsFalse)
    }
}
