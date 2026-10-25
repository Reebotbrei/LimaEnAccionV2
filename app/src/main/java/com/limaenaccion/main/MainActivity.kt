package com.limaenaccion.main

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.lifecycleScope
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.session.SessionManager
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

        // Regla "No recordar sesión": al iniciar en frío la app, cualquier sesión en memoria se limpia
        if (savedInstanceState == null) {
            SessionManager.clearSession()
        }

        splashScreen.setKeepOnScreenCondition { !mainViewModel.isReady }

        // Detección asíncrona automática del backend Node.js (USB / Wi-Fi LAN / Emulador / Nube)
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
                        Surface(
                            modifier = Modifier.fillMaxSize(),
                            color = MaterialTheme.colorScheme.background
                        ) {
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
    }

    private fun testBackendConnection() {
        lifecycleScope.launch {
            RetrofitClient.autoSelectReachableBackend()
        }
    }
}
