package com.limaenaccion.core.data.network.model

data class LoginRequest(
    val email: String = "",
    val password: String = "",
    val correo: String = email,
    val contrasena: String = password
)

data class RegisterRequest(
    val fullName: String = "",
    val dni: String = "",
    val gender: String = "",
    val email: String = "",
    val phoneNumber: String = "",
    val password: String = "",
    val district: String = "",
    val neighborhood: String = "",

    val nombreCompleto: String = fullName,
    val genero: String = gender,
    val correo: String = email,
    val telefono: String = phoneNumber,
    val contrasena: String = password,
    val distrito: String = district,
    val urbanizacion: String = neighborhood
)

data class UserDto(
    val id: String = "",
    val uid: String = id,
    val dni: String = "",
    val fullName: String = "",
    val nombreCompleto: String = fullName,
    val gender: String = "",
    val genero: String = gender,
    val email: String = "",
    val correo: String = email,
    val phoneNumber: String = "",
    val telefono: String = phoneNumber,
    val district: String = "",
    val distrito: String = district,
    val neighborhood: String = "",
    val urbanizacion: String = neighborhood,
    val createdAt: Long = System.currentTimeMillis(),
    val creadoEn: String? = null
)

data class AuthResponse(
    val status: String = "success",
    val message: String = "",
    val data: UserDto? = null
)

data class ApiErrorBody(
    val status: String? = null,
    val message: String? = null
)
