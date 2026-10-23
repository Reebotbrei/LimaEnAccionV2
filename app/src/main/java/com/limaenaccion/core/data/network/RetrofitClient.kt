package com.limaenaccion.core.data.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitClient {

    const val CLOUD_BASE_URL = "https://limaenaccion-api.onrender.com/"

    // IP virtual para pruebas de desarrollo local en Android Emulator (10.0.2.2 -> localhost de la PC)
    const val EMULATOR_BASE_URL = "http://10.0.2.2:3000/"

    // URL pública generada por Ngrok para pruebas en celular físico o emulador
    const val NGROK_BASE_URL = "https://yanking-bounce-engorge.ngrok-free.dev/"

    // URL activa apuntando a tu túnel seguro dec Ngrok
    var baseUrl: String = NGROK_BASE_URL
        private set

    @Volatile
    private var _instance: ApiService? = null

    val instance: ApiService
        get() = _instance ?: synchronized(this) {
            _instance ?: createService(baseUrl).also { _instance = it }
        }

    fun setBaseUrl(newUrl: String) {
        val sanitizedUrl = if (newUrl.endsWith("/")) newUrl else "$newUrl/"
        baseUrl = sanitizedUrl
        synchronized(this) {
            _instance = createService(sanitizedUrl)
        }
    }

    fun createService(url: String = baseUrl): ApiService {
        return Retrofit.Builder()
            .baseUrl(url)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}
