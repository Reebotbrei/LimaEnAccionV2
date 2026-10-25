package com.limaenaccion.main

import android.os.Bundle
import android.util.Log
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
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import com.limaenaccion.core.data.session.SessionManager
import com.limaenaccion.core.navigation.NavGraph
import com.limaenaccion.core.navigation.Screen
import com.limaenaccion.core.ui.theme.LimaEnAccionTheme
import com.limaenaccion.core.util.LocalAppStrings
import com.limaenaccion.core.util.getAppStrings
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        // Detección y adaptación automática de IP en segundo plano al arrancar la app
        lifecycleScope.launch {
            val prefs = PreferencesRepositoryImpl(applicationContext)
            val savedUrl = prefs.serverBaseUrl.first()
            val detectedUrl = RetrofitClient.autoDetectAndAdapt(savedUrl)
            prefs.setServerBaseUrl(detectedUrl)
            Log.d("LIMA_CONFIG", "Backend URL configurada automáticamente: $detectedUrl")

            // Regla "No recordar sesión": si el usuario no tiene activado "Recordar sesión", la sesión en RAM se limpia
            val remember = prefs.rememberSession.first()
            if (!remember && savedInstanceState == null) {
                SessionManager.clearSession()
            }
        }

        splashScreen.setKeepOnScreenCondition { !mainViewModel.isReady }

        setContent {
            val skipLoadingScreen by mainViewModel.skipLoadingScreen.collectAsState()
            val currentTheme by mainViewModel.theme.collectAsState()
            val currentFontSize by mainViewModel.fontSize.collectAsState()
            val currentLanguage by mainViewModel.language.collectAsState()
            val activeSessionUid by mainViewModel.activeSessionUid.collectAsState()
            val rememberSession by mainViewModel.rememberSession.collectAsState()

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
                            val startDestination = when {
                                rememberSession && activeSessionUid.isNotBlank() ->
                                    Screen.Main.route
                                skipLoadingScreen == true ->
                                    Screen.Emergency.route
                                else ->
                                    Screen.Splash.route
                            }

                            NavGraph(
                                startDestination = startDestination,
                                onLogout = { mainViewModel.clearSession() }
                            )
                        }
                    }
                }
            }
        }
    }
}
