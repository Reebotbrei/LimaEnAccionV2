package com.limaenaccion.core.data.network

import com.limaenaccion.core.data.network.model.AuthResponse
import com.limaenaccion.core.data.network.model.CreateReportRequest
import com.limaenaccion.core.data.network.model.ForgotPasswordRequest
import com.limaenaccion.core.data.network.model.GenericApiResponse
import com.limaenaccion.core.data.network.model.LoginRequest
import com.limaenaccion.core.data.network.model.RegisterRequest
import com.limaenaccion.core.data.network.model.ReportListResponse
import com.limaenaccion.core.data.network.model.ReportResponse
import com.limaenaccion.core.data.network.model.ResetPasswordRequest
import com.limaenaccion.core.data.network.model.SendRegisterCodeRequest
import com.limaenaccion.core.data.network.model.SendRegisterCodeResponse
import com.limaenaccion.core.data.network.model.VerifyResetCodeRequest
import retrofit2.Call
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.HTTP
import retrofit2.http.Headers
import retrofit2.http.POST
import retrofit2.http.PUT
import retrofit2.http.Path

interface ApiService {
    // Prueba de conexión con Node.js
    @GET("/api/test")
    suspend fun testConnection(): ApiResponse

    @GET("/api/test")
    fun testConnectionCall(): Call<ApiResponse>

    // Pre-registro: Solicitud y validación de código OTP (1 minuto) por Gmail y Celular
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/auth/send-register-code")
    suspend fun sendRegisterCode(@Body request: SendRegisterCodeRequest): SendRegisterCodeResponse

    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/auth/verify-register-code")
    suspend fun verifyRegisterCode(@Body request: VerifyResetCodeRequest): GenericApiResponse
//REQUERIMIENTOS
    // Registro y login de usuario consumiendo API Service (Node.js)
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/auth/register")
    suspend fun registerUser(@Body request: RegisterRequest): AuthResponse

    // Inicio de sesión
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/auth/login")
    suspend fun loginUser(@Body request: LoginRequest): AuthResponse
    // Publicar y listar reportes
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/reportes")
    suspend fun createReport(@Body request: CreateReportRequest): ReportResponse

    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @GET("/api/v1/reportes")
    suspend fun getReports(): ReportListResponse

    // Actualizar reporte existente (PUT / PATCH - Verbo RESTful para actualizar)
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @PUT("/api/v1/reportes/{id}")
    suspend fun updateReport(@Path("id") id: String, @Body request: CreateReportRequest): ReportResponse

    // Eliminar reporte existente (DELETE - Verbo RESTful para eliminar)
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @DELETE("/api/v1/reportes/{id}")
    suspend fun deleteReport(@Path("id") id: String): GenericApiResponse

    // Actualizar perfil de usuario (PUT - Verbo RESTful para actualizar perfil)
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @PUT("/api/v1/auth/profile")
    suspend fun updateProfile(@Body request: RegisterRequest): AuthResponse

    // Eliminar cuenta permanentemente (DELETE - Verbo RESTful para eliminar recurso según Ley N° 29733)
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @HTTP(method = "DELETE", path = "/api/v1/auth/account", hasBody = true)
    suspend fun deleteAccount(@Body request: RegisterRequest): AuthResponse

    // Recuperación de contraseña vía SMTP real
    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/auth/forgot-password")
    suspend fun forgotPassword(@Body request: ForgotPasswordRequest): GenericApiResponse

    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/auth/verify-reset-code")
    suspend fun verifyResetCode(@Body request: VerifyResetCodeRequest): GenericApiResponse

    @Headers("x-api-key: LIMA_EN_ACCION_API_KEY_2026")
    @POST("/api/v1/auth/reset-password")
    suspend fun resetPassword(@Body request: ResetPasswordRequest): GenericApiResponse

}
