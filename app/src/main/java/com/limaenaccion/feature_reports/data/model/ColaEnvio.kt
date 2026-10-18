package com.limaenaccion.feature_reports.data.model

data class ColaEnvio(
    val id: Long = 0,
    val reporteId: String = "",
    val operacion: String = "CREAR",
    val datosJson: String = "",
    val fotoLocalUri: String? = null,
    val intentos: Int = 0,
    val ultimoIntentoEn: Long = System.currentTimeMillis()
)