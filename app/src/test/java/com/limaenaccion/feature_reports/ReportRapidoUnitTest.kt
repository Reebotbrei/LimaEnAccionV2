package com.limaenaccion.feature_reports

import com.limaenaccion.feature_reports.data.model.ReportTimeframe
import com.limaenaccion.feature_reports.data.model.ReportType
import com.limaenaccion.feature_reports.presentation.ReportUiState
import org.junit.Assert.*
import org.junit.Test

class ReportRapidoUnitTest {

    @Test
    fun testReportTypeActividadInusualLabel() {
        assertEquals("Actividad Inusual", ReportType.SOSPECHOSO.label)
        assertEquals("Robo", ReportType.ROBO.label)
        assertEquals("Emergencia Médica", ReportType.EMERGENCIA_MEDICA.label)
    }

    @Test
    fun testReportTimeframeEnum() {
        assertEquals("Ocurriendo Ahora", ReportTimeframe.AHORA.label)
        assertEquals("Ocurrió Antes", ReportTimeframe.ANTES.label)
    }

    @Test
    fun testQuickReportCanPublishWithoutPhotoOrDescription() {
        // Estado inicial sin tipo seleccionado -> no puede publicar
        val initialState = ReportUiState(selectedType = null)
        assertFalse(initialState.canPublish)

        // Cuando se selecciona tipo y ubicación, ya puede publicar sin obligar foto ni descripción
        val readyState = ReportUiState(
            selectedType = ReportType.ROBO,
            location = "Av. Próceres de la Independencia 133, SJL",
            description = "",
            photoBitmap = null,
            photoTaken = false
        )
        assertTrue(readyState.canPublish)
    }

    @Test
    fun testAnonymousReportingToggle() {
        val publicState = ReportUiState(isAnonymous = false)
        assertFalse(publicState.isAnonymous)

        val anonymousState = publicState.copy(isAnonymous = true)
        assertTrue(anonymousState.isAnonymous)
    }

    @Test
    fun testTimeframeToggle() {
        val state = ReportUiState(timeframe = ReportTimeframe.AHORA)
        assertEquals(ReportTimeframe.AHORA, state.timeframe)

        val pastState = state.copy(timeframe = ReportTimeframe.ANTES)
        assertEquals(ReportTimeframe.ANTES, pastState.timeframe)
    }
}
