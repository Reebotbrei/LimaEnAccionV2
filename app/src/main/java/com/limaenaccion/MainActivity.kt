package com.limaenaccion

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.limaenaccion.core.navigation.NavGraph
import com.limaenaccion.core.navigation.Screen
import com.limaenaccion.core.ui.theme.LimaEnAccionTheme
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { !mainViewModel.isReady }

        // Ejecutar la prueba de conexión con Node.js al arrancar la app
        testBackendConnection()

        setContent {
            val skipLoadingScreen by mainViewModel.skipLoadingScreen.collectAsState()
            if (skipLoadingScreen != null) {
                LimaEnAccionTheme {
                    NavGraph(
                        startDestination = if (skipLoadingScreen == true) {
                            Screen.Emergency.route
                        } else {
                            Screen.Splash.route
                        }
                    )
                }
            }
        }
    }

    private fun testBackendConnection() {
        RetrofitClient.instance.testConnection().enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                if (response.isSuccessful) {
                    val body = response.body()
                    Log.d("API_TEST", "¡Éxito total! Mensaje del servidor: ${body?.mensaje}")
                } else {
                    Log.e("API_TEST", "Error en la respuesta del servidor: ${response.code()}")
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                Log.e("API_TEST", "Fallo de conexión con el backend: ${t.message}")
            }
        })
    }
}