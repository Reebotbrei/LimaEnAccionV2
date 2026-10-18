package com.limaenaccion.feature_profile.data.model

import com.google.firebase.Timestamp

data class Usuario(
    val uid: String = "",
    val nombreCompleto: String = "",
    val dni: String = "",
    val genero: String = "",
    val correo: String = "",
    val telefono: String = "",
    val distrito: String = "",
    val barrio: String = "",
    val domicilio: String = "",
    val fotoPerfilUrl: String? = "",
    val creadoEn: Timestamp = Timestamp.now()
)