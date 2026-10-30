package com.limaenaccion.core.data.local.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "contacto_auxilio")
data class ContactoEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val nombre: String,
    val telefono: String,
    val parentesco: String = "",
    val esPrincipal: Boolean = true,
    val correoUsuario: String = ""
)
