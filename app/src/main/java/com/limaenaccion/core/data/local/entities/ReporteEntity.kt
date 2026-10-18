package com.limaenaccion.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "reporte_cache")
data class ReporteEntity(
    @PrimaryKey
    val id: String,
    val usuarioId: String,
    val tipo: String,
    val titulo: String,
    val descripcion: String?,
    val latitud: Double,
    val longitud: Double,
    val fotoUri: String?,
    val creadoEn: Long,
    val venceEn: Long,
    val estado: String,
    val esSos: Boolean
)
