package com.limaenaccion.feature_auth.data.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "usuario_cache")
data class Usuario(
    @PrimaryKey
    val uid: String = "",
    val fullName: String = "",
    val dni: String = "",
    val gender: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val district: String = "",
    val neighborhood: String = "",
    val address: String = "",
    val profilePhotoUrl: String = "",
    val contrasena: String = "",
    val password: String = contrasena,
    val createdAt: Long = System.currentTimeMillis()
)
