package com.limaenaccion.feature_emergency.data.model

data class EmergencyContact(
    val id: String,
    val name: String,
    val phoneNumber: String,
    val type: EmergencyContactType,
    val isCustom: Boolean = false
)

enum class EmergencyContactType {
    CONTACTO_PRINCIPAL,
    SAMU,
    BOMBEROS,
    POLICIA_NACIONAL,
    DEFENSA_CIVIL,
    EMERGENCIA_NACIONAL,
    CONTACTO_VECINAL
}