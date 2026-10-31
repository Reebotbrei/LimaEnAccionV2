package com.limaenaccion.core.data.network.model

import com.google.gson.annotations.SerializedName

/**
 * DTOs de Autenticación y Perfil para Retrofit 2 con Gson.
 *
 * Utiliza @SerializedName para definir el nombre canónico del campo en el contrato JSON,
 * y 'alternate' para compatibilidad retroactiva con nombres en inglés o alias del backend.
 * Se incluyen constructores secundarios de conveniencia para garantizar interoperabilidad
 * en llamadas que usen parámetros con nombres en inglés (email, password, etc.).
 */

data class LoginRequest(
    @SerializedName("correo", alternate = ["email"])
    val correo: String = "",

    @SerializedName("contrasena", alternate = ["password"])
    val contrasena: String = ""
) {
    // Constructor de conveniencia para invocaciones con parámetros en inglés (email, password)
    constructor(email: String, password: String, isEnglish: Boolean = true) : this(
        correo = email,
        contrasena = password
    )
}

data class ContactoDto(
    @SerializedName("id")
    val id: String = "",

    @SerializedName("nombre")
    val nombre: String = "",

    @SerializedName("telefono")
    val telefono: String = "",

    @SerializedName("parentesco")
    val parentesco: String = "",

    @SerializedName("esPrincipal")
    val esPrincipal: Boolean = false
)

data class RegisterRequest(
    @SerializedName("nombreCompleto", alternate = ["fullName"])
    val nombreCompleto: String = "",

    @SerializedName("dni")
    val dni: String = "",

    @SerializedName("genero", alternate = ["gender"])
    val genero: String = "",

    @SerializedName("correo", alternate = ["email"])
    val correo: String = "",

    @SerializedName("telefono", alternate = ["phoneNumber"])
    val telefono: String = "",

    @SerializedName("contrasena", alternate = ["password"])
    val contrasena: String = "",

    @SerializedName("distrito", alternate = ["district"])
    val distrito: String = "",

    @SerializedName("urbanizacion", alternate = ["neighborhood"])
    val urbanizacion: String = "",

    @SerializedName("fotoPerfil", alternate = ["profilePhotoUrl"])
    val fotoPerfil: String = "",

    @SerializedName("contactosAuxilio")
    val contactosAuxilio: List<ContactoDto> = emptyList()
) {
    // Constructor para soportar llamadas con parámetros en inglés
    constructor(
        fullName: String = "",
        dni: String = "",
        gender: String = "",
        email: String = "",
        phoneNumber: String = "",
        password: String = "",
        district: String = "",
        neighborhood: String = "",
        fotoPerfil: String = "",
        profilePhotoUrl: String = fotoPerfil,
        contactosAuxilio: List<ContactoDto> = emptyList(),
        nombreCompleto: String = fullName,
        genero: String = gender,
        correo: String = email,
        telefono: String = phoneNumber,
        contrasena: String = password,
        distrito: String = district,
        urbanizacion: String = neighborhood
    ) : this(
        nombreCompleto = nombreCompleto.ifBlank { fullName },
        dni = dni,
        genero = genero.ifBlank { gender },
        correo = correo.ifBlank { email },
        telefono = telefono.ifBlank { phoneNumber },
        contrasena = contrasena.ifBlank { password },
        distrito = distrito.ifBlank { district },
        urbanizacion = urbanizacion.ifBlank { neighborhood },
        fotoPerfil = fotoPerfil.ifBlank { profilePhotoUrl },
        contactosAuxilio = contactosAuxilio
    )
}

data class UserDto(
    @SerializedName("id", alternate = ["uid"])
    val id: String = "",

    @SerializedName("dni")
    val dni: String = "",

    @SerializedName("nombreCompleto", alternate = ["fullName"])
    val nombreCompleto: String = "",

    @SerializedName("genero", alternate = ["gender"])
    val genero: String = "",

    @SerializedName("correo", alternate = ["email"])
    val correo: String = "",

    @SerializedName("telefono", alternate = ["phoneNumber"])
    val telefono: String = "",

    @SerializedName("distrito", alternate = ["district"])
    val distrito: String = "",

    @SerializedName("urbanizacion", alternate = ["neighborhood"])
    val urbanizacion: String = "",

    @SerializedName("fotoPerfil", alternate = ["profilePhotoUrl"])
    val fotoPerfil: String = "",

    @SerializedName("contactosAuxilio")
    val contactosAuxilio: List<ContactoDto> = emptyList(),

    @SerializedName("createdAt")
    val createdAt: Long = System.currentTimeMillis(),

    @SerializedName("creadoEn")
    val creadoEn: String? = null
) {
    val uid: String get() = id
    val fullName: String get() = nombreCompleto
    val gender: String get() = genero
    val email: String get() = correo
    val phoneNumber: String get() = telefono
    val district: String get() = distrito
    val neighborhood: String get() = urbanizacion
    val profilePhotoUrl: String get() = fotoPerfil
}

data class AuthResponse(
    @SerializedName("status")
    val status: String = "success",

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data", alternate = ["user"])
    val data: UserDto? = null
)

data class ApiErrorBody(
    @SerializedName("status")
    val status: String? = null,

    @SerializedName("message")
    val message: String? = null
)

data class ForgotPasswordRequest(
    @SerializedName("correo", alternate = ["email"])
    val correo: String = ""
) {
    constructor(email: String, correo: String = email) : this(correo = if (correo.isNotBlank()) correo else email)
}

data class VerifyResetCodeRequest(
    @SerializedName("correo", alternate = ["email"])
    val correo: String = "",

    @SerializedName("codigo", alternate = ["code"])
    val codigo: String = ""
) {
    constructor(email: String, code: String, correo: String = email, codigo: String = code) : this(
        correo = if (correo.isNotBlank()) correo else email,
        codigo = if (codigo.isNotBlank()) codigo else code
    )
}

data class ResetPasswordRequest(
    @SerializedName("correo", alternate = ["email"])
    val correo: String = "",

    @SerializedName("codigo", alternate = ["code"])
    val codigo: String = "",

    @SerializedName("nuevaContrasena", alternate = ["newPassword", "password"])
    val nuevaContrasena: String = ""
) {
    constructor(
        email: String,
        code: String,
        newPassword: String,
        correo: String = email,
        codigo: String = code,
        nuevaContrasena: String = newPassword
    ) : this(
        correo = if (correo.isNotBlank()) correo else email,
        codigo = if (codigo.isNotBlank()) codigo else code,
        nuevaContrasena = if (nuevaContrasena.isNotBlank()) nuevaContrasena else newPassword
    )
}

data class GenericApiResponse(
    @SerializedName("status")
    val status: String = "success",

    @SerializedName("message")
    val message: String = ""
)

data class SendRegisterCodeRequest(
    @SerializedName("correo", alternate = ["email"])
    val correo: String = "",

    @SerializedName("dni")
    val dni: String = "",

    @SerializedName("nombreCompleto", alternate = ["fullName"])
    val nombreCompleto: String = "",

    @SerializedName("telefono", alternate = ["phoneNumber"])
    val telefono: String = ""
) {
    constructor(
        email: String,
        dni: String,
        fullName: String = "",
        phoneNumber: String = "",
        correo: String = email,
        nombreCompleto: String = fullName,
        telefono: String = phoneNumber
    ) : this(
        correo = if (correo.isNotBlank()) correo else email,
        dni = dni,
        nombreCompleto = if (nombreCompleto.isNotBlank()) nombreCompleto else fullName,
        telefono = if (telefono.isNotBlank()) telefono else phoneNumber
    )
}

data class RegisterCodeData(
    @SerializedName("correo")
    val correo: String = "",

    @SerializedName("telefono")
    val telefono: String = "",

    @SerializedName("debugCode")
    val debugCode: String = ""
)

data class SendRegisterCodeResponse(
    @SerializedName("status")
    val status: String = "success",

    @SerializedName("message")
    val message: String = "",

    @SerializedName("data")
    val data: RegisterCodeData? = null
)
