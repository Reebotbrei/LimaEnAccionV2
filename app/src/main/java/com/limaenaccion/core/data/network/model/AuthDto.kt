package com.limaenaccion.core.data.network.model

import com.limaenaccion.feature_auth.data.model.Usuario

// Lo que enviamos al backend para el registro
data class RegisterRequest(
    val fullName: String,
    val dni: String,
    val gender: String,
    val email: String,
    val phoneNumber: String,
    val password: String,
    val district: String,
    val neighborhood: String
)

// Lo que enviamos al backend para el inicio de sesión
data class LoginRequest(
    val email: String,
    val password: String
)

// Lo que el backend nos responde en ambos casos
data class AuthResponse(
    val status: String,
    val message: String,
    val data: Usuario?
)
