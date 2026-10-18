package com.limaenaccion.feature_profile.data.model

data class ContactoAuxilio(
    val id: String = "",
    val uid: String = "",
    val nombre: String = "",
    val parentesco: String = "",
    val telefono: String = "",
    val esPrincipal: Boolean = false
)