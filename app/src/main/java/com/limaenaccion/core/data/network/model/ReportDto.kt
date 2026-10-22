package com.limaenaccion.core.data.network.model

data class ReportDto(
    val id: String,
    val usuarioId: String,
    val tipo: String,
    val titulo: String,
    val descripcion: String,
    val latitud: Double,
    val longitud: Double,
    val fotoUrl: String? = null,
    val estado: String,
    val esSos: Boolean = false,
    val creadoEn: String,
    val venceEn: String
)

data class ReportResponse(
    val status: String,
    val message: String,
    val data: ReportDto
)

data class ReportListResponse(
    val status: String,
    val total: Int,
    val data: List<ReportDto>
)
