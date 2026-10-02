package com.limaenaccion.feature_emergency.data.repository

import com.limaenaccion.feature_emergency.data.model.EmergencyContact
import com.limaenaccion.feature_emergency.data.model.EmergencyContactType

class MockEmergencyRepository : EmergencyRepository {

    private val institutionalContacts = listOf(
        EmergencyContact("samu", "SAMU", "106", EmergencyContactType.SAMU),
        EmergencyContact("bomberos", "Bomberos", "116", EmergencyContactType.BOMBEROS),
        EmergencyContact("pnp", "Policía Nacional", "105", EmergencyContactType.POLICIA_NACIONAL),
        EmergencyContact("defensa_civil", "Defensa Civil", "115", EmergencyContactType.DEFENSA_CIVIL),
        EmergencyContact("emergencia_nacional", "Emergencia Nacional", "119", EmergencyContactType.EMERGENCIA_NACIONAL)
    )

    override suspend fun getEmergencyDirectory(): List<EmergencyContact> = institutionalContacts
}