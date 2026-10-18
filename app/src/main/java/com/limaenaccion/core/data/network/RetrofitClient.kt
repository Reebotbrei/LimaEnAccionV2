package com.limaenaccion.core.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {
    // 10.0.2.2 es el alias estándar de Android Emulator para conectarse a localhost de la PC.
    // Para dispositivo físico por Wi-Fi, cambiar o configurar a la IP de la máquina (ej: 192.168.x.x).
    private const val DEFAULT_EMULATOR_URL = "http://10.0.2.2:3000/"

    private var currentBaseUrl: String = DEFAULT_EMULATOR_URL

    val instance: ApiService by lazy {
        createService(currentBaseUrl)
    }

    fun createService(baseUrl: String = currentBaseUrl): ApiService {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
