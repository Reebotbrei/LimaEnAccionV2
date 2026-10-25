package com.limaenaccion.core.data.network.model

data class LoginRequest(
    val correo: String,
    val contrasena: String
)

data class RegisterRequest(
    val dni: String,
    val nombreCompleto: String,
    val genero: String,
    val correo: String,
    val telefono: String,
    val contrasena: String,
    val distrito: String,
    val urbanizacion: String
)

data class UserDto(
    val id: String,
    val dni: String,
    val nombreCompleto: String,
    val genero: String,
    val correo: String,
    val telefono: String,
    val distrito: String,
    val urbanizacion: String,
    val creadoEn: String? = null
)

data class AuthResponse(
    val status: String,
    val message: String,
    val data: UserDto? = null
)

data class ApiErrorBody(
    val status: String? = null,
    val message: String? = null
)
