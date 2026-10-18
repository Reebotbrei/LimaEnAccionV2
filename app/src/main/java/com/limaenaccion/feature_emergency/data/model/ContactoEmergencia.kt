package com.limaenaccion.feature_emergency.data.model

data class ContactoEmergencia(
    val id: Long = 0,
    val nombre: String = "",
    val telefono: String = "",
    val tipo: String = "",
    val esPersonalizado: Boolean = false
)