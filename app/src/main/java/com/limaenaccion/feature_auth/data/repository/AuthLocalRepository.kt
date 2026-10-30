package com.limaenaccion.feature_auth.data.repository

import android.content.Context
import android.util.Log
import com.google.gson.Gson
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.SqliteDatabaseHelper
import com.limaenaccion.core.data.local.dao.UsuarioDao
import com.limaenaccion.core.data.local.entities.UsuarioEntity
import com.limaenaccion.core.data.network.ApiService
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.network.model.ApiErrorBody
import com.limaenaccion.core.data.network.model.LoginRequest
import com.limaenaccion.core.data.network.model.RegisterRequest
import com.limaenaccion.core.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

enum class AuthErrorField {
    EMAIL,
    DNI,
    PASSWORD,
    GENERAL
}

sealed class AuthActionResult {
    data class Success(val user: UsuarioEntity, val syncedWithNodeApi: Boolean) : AuthActionResult()
    data class Error(val message: String, val field: AuthErrorField = AuthErrorField.GENERAL) : AuthActionResult()
}

interface AuthLocalRepository {
    suspend fun registerUser(usuario: UsuarioEntity): AuthActionResult
    suspend fun loginUser(email: String, password: String): AuthActionResult
    fun logout()
}

/**
 * Repositorio de Autenticación que consume el API Service de Node.js mediante Retrofit,
 * sincroniza el usuario en SQLite local (SQLiteOpenHelper + Room) y administra la sesión
 * únicamente en memoria RAM ("No recordar sesión").
 */
class AuthLocalRepositoryImpl(
    context: Context,
    private val apiServiceProvider: () -> ApiService = { RetrofitClient.instance },
    private val usuarioDao: UsuarioDao = AppDatabase.getInstance(context).usuarioDao(),
    private val sqliteHelper: SqliteDatabaseHelper = SqliteDatabaseHelper.getInstance(context)
) : AuthLocalRepository {

    private val gson = Gson()

    override suspend fun registerUser(usuario: UsuarioEntity): AuthActionResult =
        withContext(Dispatchers.IO) {
            val request = RegisterRequest(
                dni = usuario.dni,
                nombreCompleto = usuario.nombreCompleto,
                genero = usuario.genero,
                correo = usuario.correo.trim().lowercase(),
                telefono = usuario.telefono,
                contrasena = usuario.contrasena,
                distrito = usuario.distrito,
                urbanizacion = usuario.urbanizacion
            )

            try {
                val response = try {
                    apiServiceProvider().registerUser(request)
                } catch (_: IOException) {
                    // Si la IP inicial no respondió, autodetecta el servidor Node.js activo y reintenta
                    val resolved = RetrofitClient.autoSelectReachableBackend()
                    if (resolved != null) {
                        apiServiceProvider().registerUser(request)
                    } else {
                        throw IOException("Servidor Node.js fuera de línea")
                    }
                }

                val dto = response.data
                val finalUser = if (dto != null) {
                    usuario.copy(
                        correo = dto.correo,
                        dni = dto.dni,
                        nombreCompleto = dto.nombreCompleto,
                        genero = dto.genero,
                        telefono = dto.telefono,
                        distrito = dto.distrito,
                        urbanizacion = dto.urbanizacion
                    )
                } else {
                    usuario
                }

                // Sincroniza en SQLite local y activa sesión efímera en RAM
                sqliteHelper.guardarUsuario(finalUser)
                usuarioDao.guardarUsuario(finalUser)
                SessionManager.startTransientSession(finalUser)

                Log.d("AUTH_NODE", "✅ [NODE.JS API] Registro exitoso en servidor: ${finalUser.correo}")
                AuthActionResult.Success(finalUser, syncedWithNodeApi = true)
            } catch (e: HttpException) {
                val errorMsg = parseHttpErrorMessage(e) ?: "No se pudo completar el registro en el servidor"
                val field = when {
                    errorMsg.contains("DNI", ignoreCase = true) -> AuthErrorField.DNI
                    errorMsg.contains("correo", ignoreCase = true) -> AuthErrorField.EMAIL
                    else -> AuthErrorField.GENERAL
                }
                Log.w("AUTH_NODE", "⚠️ [NODE.JS API] Registro rechazado (${e.code()}): $errorMsg")
                AuthActionResult.Error(errorMsg, field)
            } catch (e: Exception) {
                // Respaldo local en SQLite únicamente cuando no hay conexión de red con el servidor
                Log.w("AUTH_NODE", "⚠️ [NODE.JS API] Sin conexión al servidor (${e.message}). Guardando en SQLite local.")
                sqliteHelper.guardarUsuario(usuario)
                usuarioDao.guardarUsuario(usuario)
                SessionManager.startTransientSession(usuario)
                AuthActionResult.Success(usuario, syncedWithNodeApi = false)
            }
        }

    override suspend fun loginUser(email: String, password: String): AuthActionResult =
        withContext(Dispatchers.IO) {
            val cleanEmail = email.trim().lowercase()
            val request = LoginRequest(
                correo = cleanEmail,
                contrasena = password
            )

            try {
                val response = try {
                    apiServiceProvider().loginUser(request)
                } catch (_: IOException) {
                    val resolved = RetrofitClient.autoSelectReachableBackend()
                    if (resolved != null) {
                        apiServiceProvider().loginUser(request)
                    } else {
                        throw IOException("Servidor Node.js fuera de línea")
                    }
                }

                val dto = response.data
                    ?: return@withContext AuthActionResult.Error(
                        "Respuesta inválida del servidor",
                        AuthErrorField.GENERAL
                    )

                val foto = dto.fotoPerfil.ifBlank { dto.profilePhotoUrl }
                val loggedInUser = UsuarioEntity(
                    correo = dto.correo,
                    dni = dto.dni,
                    nombreCompleto = dto.nombreCompleto,
                    genero = dto.genero,
                    telefono = dto.telefono,
                    contrasena = password,
                    distrito = dto.distrito,
                    urbanizacion = dto.urbanizacion,
                    fotoPerfil = foto
                )

                // Sincroniza en SQLite local y activa sesión efímera en RAM ("No recordar sesión")
                sqliteHelper.guardarUsuario(loggedInUser)
                usuarioDao.guardarUsuario(loggedInUser)
                SessionManager.startTransientSession(loggedInUser)

                Log.d("AUTH_NODE", "✅ [NODE.JS API] Login exitoso desde servidor: ${loggedInUser.correo}")
                AuthActionResult.Success(loggedInUser, syncedWithNodeApi = true)
            } catch (e: HttpException) {
                val errorMsg = parseHttpErrorMessage(e) ?: "Credenciales inválidas"
                val field = when {
                    errorMsg.contains("correo", ignoreCase = true) ||
                        errorMsg.contains("cuenta", ignoreCase = true) -> AuthErrorField.EMAIL
                    else -> AuthErrorField.PASSWORD
                }
                Log.w("AUTH_NODE", "⚠️ [NODE.JS API] Login rechazado (${e.code()}): $errorMsg")
                AuthActionResult.Error(errorMsg, field)
            } catch (e: Exception) {
                Log.w("AUTH_NODE", "⚠️ [NODE.JS API] Sin red hacia Node.js (${e.message}). Validando contra SQLite local.")
                val localUser = sqliteHelper.autenticarUsuario(cleanEmail, password)
                    ?: usuarioDao.autenticarLocal(cleanEmail, password)

                if (localUser != null) {
                    SessionManager.startTransientSession(localUser)
                    return@withContext AuthActionResult.Success(localUser, syncedWithNodeApi = false)
                }

                val existingEmail = sqliteHelper.obtenerPorCorreo(cleanEmail)
                    ?: usuarioDao.obtenerPorCorreo(cleanEmail)
                if (existingEmail != null) {
                    return@withContext AuthActionResult.Error(
                        "Contraseña incorrecta",
                        AuthErrorField.PASSWORD
                    )
                }

                AuthActionResult.Error(
                    "No se pudo conectar al servidor Node.js ni se encontró este correo en SQLite local",
                    AuthErrorField.GENERAL
                )
            }
        }

    override fun logout() {
        SessionManager.clearSession()
    }

    private fun parseHttpErrorMessage(exception: HttpException): String? {
        return try {
            val rawJson = exception.response()?.errorBody()?.string()
            if (rawJson.isNullOrBlank()) null
            else gson.fromJson(rawJson, ApiErrorBody::class.java)?.message
        } catch (_: Exception) {
            null
        }
    }
}
