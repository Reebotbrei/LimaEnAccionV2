package com.limaenaccion

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // Si usas el emulador de Android, '10.0.2.2' apunta al localhost de tu PC.
    private const val BASE_URL = "http://192.168.121.165:3000/"

    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}