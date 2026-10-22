package com.limaenaccion.feature_sos

import com.limaenaccion.feature_sos.domain.model.SosDeliveryState
import com.limaenaccion.feature_sos.domain.model.SosSessionConfig
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test

class SosHonestoUnitTest {

    @Before
    fun setUp() {
        SosSessionConfig.reset()
    }

    @Test
    fun testSosSessionConfigDefaults() {
        assertFalse(SosSessionConfig.isSilentMode)
        assertFalse(SosSessionConfig.isDemoMode)
    }

    @Test
    fun testSosSessionConfigToggle() {
        SosSessionConfig.isSilentMode = true
        SosSessionConfig.isDemoMode = true
        assertTrue(SosSessionConfig.isSilentMode)
        assertTrue(SosSessionConfig.isDemoMode)

        SosSessionConfig.reset()
        assertFalse(SosSessionConfig.isSilentMode)
        assertFalse(SosSessionConfig.isDemoMode)
    }

    @Test
    fun testSosDeliveryStatesHierarchy() {
        val states = SosDeliveryState.values()
        assertEquals(5, states.size)
        assertTrue(states.contains(SosDeliveryState.PREPARANDO))
        assertTrue(states.contains(SosDeliveryState.ENVIANDO))
        assertTrue(states.contains(SosDeliveryState.RECIBIDA_SISTEMA))
        assertTrue(states.contains(SosDeliveryState.CONTACTOS_NOTIFICADOS))
        assertTrue(states.contains(SosDeliveryState.ERROR_ENVIO))
    }
}
