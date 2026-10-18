package com.limaenaccion.core.data.network

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("/api/test")
    suspend fun testConnection(): ApiResponse

    // Compatible con llamadas tradicionales Callback si fueran necesarias
    @GET("/api/test")
    fun testConnectionCall(): Call<ApiResponse>
}
