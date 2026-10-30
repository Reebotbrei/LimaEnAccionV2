package com.limaenaccion.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuarios")
data class UsuarioEntity(
    @PrimaryKey
    val correo: String,
    val dni: String,
    val nombreCompleto: String,
    val genero: String,
    val telefono: String,
    val contrasena: String,
    val distrito: String,
    val urbanizacion: String,
    val fechaRegistro: Long = System.currentTimeMillis(),
    val fotoPerfil: String = ""
)
