package com.limaenaccion.core.data.network

import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    // Túnel USB directo si se ejecuta `adb reverse tcp:3000 tcp:3000`
    const val LOCAL_USB_URL = "http://127.0.0.1:3000/"

    // IP en red local Wi-Fi/LAN de la PC para pruebas desde un smartphone físico
    const val LOCAL_LAN_URL = "http://192.168.18.27:3000/"

    // IP virtual para pruebas en Android Emulator (10.0.2.2 -> localhost de la PC)
    const val EMULATOR_BASE_URL = "http://10.0.2.2:3000/"

    // URL pública en caso de despliegue en la nube (Render / Railway)
    const val CLOUD_BASE_URL = "https://limaenaccion-api.onrender.com/"

    // URL activa inicial apuntando a la red local / emulador
    var baseUrl: String = LOCAL_LAN_URL
        private set

    private val defaultHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .writeTimeout(6, TimeUnit.SECONDS)
            .build()
    }

    private val probeHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .connectTimeout(1500, TimeUnit.MILLISECONDS)
            .readTimeout(2000, TimeUnit.MILLISECONDS)
            .build()
    }

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

    fun createService(
        url: String = baseUrl,
        client: OkHttpClient = defaultHttpClient
    ): ApiService {
        val sanitizedUrl = if (url.endsWith("/")) url else "$url/"
        return Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Detecta automáticamente cuál endpoint de Node.js está activo
     * (Cable USB adb reverse, Red Wi-Fi LAN, Emulador Android o Nube) y lo fija como activo.
     */
    suspend fun autoSelectReachableBackend(): String? = withContext(Dispatchers.IO) {
        val candidates = listOf(
            LOCAL_USB_URL,
            LOCAL_LAN_URL,
            EMULATOR_BASE_URL,
            CLOUD_BASE_URL
        )
        for (candidate in candidates) {
            try {
                val probeService = createService(url = candidate, client = probeHttpClient)
                val response = probeService.testConnection()
                setBaseUrl(candidate)
                Log.d("API_NODE", "✅ Servidor Node.js detectado en $candidate: ${response.mensaje}")
                return@withContext candidate
            } catch (_: Exception) {
                // Continúa probando el siguiente candidato
            }
        }
        Log.w("API_NODE", "⚠️ Ningún servidor Node.js respondió en USB/LAN/Emulador/Nube.")
        null
    }
}
