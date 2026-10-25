package com.limaenaccion.core.data.network

import com.limaenaccion.core.data.network.model.AuthResponse
import com.limaenaccion.core.data.network.model.CreateReportRequest
import com.limaenaccion.core.data.network.model.LoginRequest
import com.limaenaccion.core.data.network.model.RegisterRequest
import com.limaenaccion.core.data.network.model.ReportListResponse
import com.limaenaccion.core.data.network.model.ReportResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.Headers
import retrofit2.http.POST

interface ApiService {
    // Prueba de conexión con Node.js
    @GET("/api/test")
    suspend fun testConnection(): ApiResponse

    @GET("/api/test")
    fun testConnectionCall(): Call<ApiResponse>

    // Requerimiento (c): Registro de usuario consumiendo API Service (Node.js)
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): AuthResponse

    // Requerimiento (c): Inicio de sesión consumiendo API Service (Node.js)
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): AuthResponse

    // Actualizar perfil consumiendo API Service (Node.js)
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/auth/update")
    suspend fun updateProfile(@Body request: RegisterRequest): AuthResponse

    // Eliminar cuenta permanentemente (Ley N° 29733)
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/auth/delete")
    suspend fun deleteAccount(@Body request: RegisterRequest): AuthResponse

    // US-03 / API-01: Crear reporte en el backend
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/reportes")
    suspend fun createReport(@Body request: CreateReportRequest): ReportResponse

    // US-02 / US-06 / API-03: Listar reportes vigentes del backend
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @GET("/api/v1/reportes")
    suspend fun getReports(): ReportListResponse
}
