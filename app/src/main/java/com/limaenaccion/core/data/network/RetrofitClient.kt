package com.limaenaccion.core.data.network

import android.os.Build
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {

    const val EMULATOR_BASE_URL = "http://10.0.2.2:3000/"
    const val LOCAL_USB_URL = "http://127.0.0.1:3000/"
    const val LOCAL_LAN_URL = "http://192.168.18.27:3000/"
    const val LOCAL_WIFI_BASE_URL = "http://192.168.0.158:3000/"
    const val CLOUD_BASE_URL = "https://lima-en-accion-api-gmf3fxhgg6dwgxhj.eastus-01.azurewebsites.net/"

    const val API_KEY = "LIMA_EN_ACCION_API_KEY_2026"

    fun isEmulator(): Boolean {
        return (Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk" == Build.PRODUCT)
    }

    // IP inicial predeterminada
    var baseUrl: String = if (isEmulator()) EMULATOR_BASE_URL else LOCAL_WIFI_BASE_URL
        private set

    @Volatile
    private var _instance: ApiService? = null

    // Cliente HTTP con inyección automática de x-api-key y logging en vivo en Logcat
    private val okHttpClient: OkHttpClient by lazy {
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val original = chain.request()
                val requestBuilder = original.newBuilder()
                    .header("x-api-key", API_KEY)
                val request = requestBuilder.build()
                Log.d("API_NETWORK", "🚀 [CONSUMIENDO API] ${request.method()} -> ${request.url()}")
                val response = chain.proceed(request)
                Log.d("API_NETWORK", "📥 [RESPUESTA API] Código: ${response.code()} (${if (response.isSuccessful) "OK" else "Error"}) de ${request.url()}")
                response
            }
            .connectTimeout(3, TimeUnit.SECONDS)
            .readTimeout(6, TimeUnit.SECONDS)
            .writeTimeout(6, TimeUnit.SECONDS)
            .build()
    }

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
        Log.d("AUTO_IP", "BaseUrl actualizada a: $sanitizedUrl")
    }

    fun createService(url: String = baseUrl): ApiService {
        val sanitizedUrl = if (url.endsWith("/")) url else "$url/"
        return Retrofit.Builder()
            .baseUrl(sanitizedUrl)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    /**
     * Detección y adaptación automática de IP en segundo plano:
     * Prueba los candidatos más probables según el entorno (Emulador, Celular USB, Wi-Fi LAN o Nube)
     * y fija automáticamente la primera URL que responda.
     */
    suspend fun autoDetectAndAdapt(savedPrefUrl: String? = null): String = withContext(Dispatchers.IO) {
        val candidates = mutableListOf<String>()

        // 1. Si se especificó una preferencia guardada previa
        if (!savedPrefUrl.isNullOrBlank()) {
            val sanitized = if (savedPrefUrl.endsWith("/")) savedPrefUrl else "$savedPrefUrl/"
            candidates.add(sanitized)
        }

        // 2. Entorno según dispositivo: Emulador primero busca 10.0.2.2
        if (isEmulator()) {
            candidates.add(EMULATOR_BASE_URL)
        }

        // 3. Fallbacks de red local Wi-Fi / USB / LAN de desarrollo
        candidates.add(LOCAL_WIFI_BASE_URL)
        candidates.add(LOCAL_USB_URL)
        candidates.add(LOCAL_LAN_URL)
        if (!isEmulator()) {
            candidates.add(EMULATOR_BASE_URL)
        }

        // 4. Servidor en la nube
        candidates.add(CLOUD_BASE_URL)

        val probeClient = OkHttpClient.Builder()
            .connectTimeout(1500, TimeUnit.MILLISECONDS)
            .readTimeout(1500, TimeUnit.MILLISECONDS)
            .build()

        for (candidate in candidates) {
            try {
                val testUrl = if (candidate.endsWith("/")) "${candidate}api/test" else "$candidate/api/test"
                val request = Request.Builder().url(testUrl).get().build()
                val response = probeClient.newCall(request).execute()
                if (response.isSuccessful) {
                    response.close()
                    Log.i("AUTO_IP", "✅ Servidor Node.js detectado automáticamente y activo: $candidate")
                    setBaseUrl(candidate)
                    return@withContext candidate
                }
                response.close()
            } catch (_: Exception) {
                // Siguiente candidato
            }
        }

        val fallbackDefault = if (isEmulator()) EMULATOR_BASE_URL else LOCAL_WIFI_BASE_URL
        setBaseUrl(fallbackDefault)
        Log.i("AUTO_IP", "Entorno configurado automáticamente a $fallbackDefault")
        fallbackDefault
    }

    suspend fun autoSelectReachableBackend(): String? = autoDetectAndAdapt()
}
