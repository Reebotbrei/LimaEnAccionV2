package com.limaenaccion.core.data.network.model

data class CreateReportRequest(
    val tipo: String,
    val titulo: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val fotoUrl: String? = null,
    val esSos: Boolean = false,
    val usuarioId: String? = null,
    val usuarioNombre: String? = null,
    val esAnonimo: Boolean = false
)
