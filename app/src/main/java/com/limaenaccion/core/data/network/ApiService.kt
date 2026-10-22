package com.limaenaccion.core.data.network

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
}
