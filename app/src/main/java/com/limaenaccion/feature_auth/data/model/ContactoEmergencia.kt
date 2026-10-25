package com.limaenaccion.feature_auth.data.model

data class ContactoEmergencia(
    val id: String = "",
    val nombre: String = "",
    val parentesco: String = "",
    val telefono: String = "",
    val esPrincipal: Boolean = false
)
