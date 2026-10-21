package com.limaenaccion.main

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.navigation.NavGraph
import com.limaenaccion.core.navigation.Screen
import com.limaenaccion.core.ui.theme.LimaEnAccionTheme
import com.limaenaccion.core.util.LocalAppStrings
import com.limaenaccion.core.util.getAppStrings
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { !mainViewModel.isReady }

        // Prueba asíncrona no bloqueante de conexión con el backend
        testBackendConnection()

        setContent {
            val skipLoadingScreen by mainViewModel.skipLoadingScreen.collectAsState()
            val currentTheme by mainViewModel.theme.collectAsState()
            val currentFontSize by mainViewModel.fontSize.collectAsState()
            val currentLanguage by mainViewModel.language.collectAsState()

            if (skipLoadingScreen != null) {
                LimaEnAccionTheme(
                    theme = currentTheme,
                    fontSizeSp = currentFontSize
                ) {
                    CompositionLocalProvider(LocalAppStrings provides getAppStrings(currentLanguage)) {
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
    }

    private fun testBackendConnection() {
        lifecycleScope.launch {
            try {
                val response = RetrofitClient.instance.testConnection()
                Log.d("API_TEST", "¡Éxito total! Mensaje del servidor: ${response.mensaje}")
            } catch (e: Exception) {
                Log.w("API_TEST", "Servidor backend no disponible en esta red o puerto: ${e.message}")
            }
        }
    }
}
