package com.limaenaccion

import retrofit2.Call
import retrofit2.http.GET

interface ApiService {
    @GET("/api/test")
    fun testConnection(): Call<ApiResponse>
}