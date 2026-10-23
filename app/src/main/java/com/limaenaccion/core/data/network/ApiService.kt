package com.limaenaccion.core.data.network
import com.limaenaccion.core.data.network.model.AuthResponse
import com.limaenaccion.core.data.network.model.LoginRequest
import com.limaenaccion.core.data.network.model.RegisterRequest
import retrofit2.http.Headers
import com.limaenaccion.core.data.network.model.CreateReportRequest
import com.limaenaccion.core.data.network.model.ReportListResponse
import com.limaenaccion.core.data.network.model.ReportResponse
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {
    // Prueba de conexión con Node.js
    @GET("/api/test")
    suspend fun testConnection(): ApiResponse

    @GET("/api/test")
    fun testConnectionCall(): Call<ApiResponse>

    // US-03 / API-01: Crear reporte en el backend
    @POST("/api/v1/reportes")
    suspend fun createReport(@Body request: CreateReportRequest): ReportResponse

    // US-02 / US-06 / API-03: Listar reportes vigentes del backend
    @GET("/api/v1/reportes")
    suspend fun getReports(): ReportListResponse
    // API-04: Registrar usuario en el backend consumiendo API Service
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/auth/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    // API-05: Iniciar sesión consumiendo API Service
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/auth/login")
    suspend fun login(@Body request: LoginRequest): AuthResponse

}
