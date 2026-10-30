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
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.entities.UsuarioEntity
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import com.limaenaccion.core.data.session.SessionManager
import com.limaenaccion.core.navigation.NavGraph
import com.limaenaccion.core.navigation.Screen
import com.limaenaccion.core.ui.theme.LimaEnAccionTheme
import com.limaenaccion.core.util.LocalAppStrings
import com.limaenaccion.core.util.getAppStrings
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

            // Restaurar sesión del usuario si está activado "Recordar sesión"
            val remember = prefs.rememberSession.first()
            val sessionUid = prefs.activeSessionUid.first()
            if (remember) {
                withContext(Dispatchers.IO) {
                    try {
                        val db = AppDatabase.getInstance(applicationContext)
                        val dao = db.usuarioDao()
                        var restoredEntity: UsuarioEntity? = null

                        if (sessionUid.isNotBlank()) {
                            restoredEntity = dao.obtenerPorCorreo(sessionUid)
                                ?: dao.obtenerPorDni(sessionUid)
                        }

                        if (restoredEntity == null && sessionUid.isNotBlank()) {
                            val cacheUser = dao.obtenerUsuario(sessionUid)
                            if (cacheUser != null) {
                                restoredEntity = UsuarioEntity(
                                    correo = cacheUser.email,
                                    dni = cacheUser.dni,
                                    nombreCompleto = cacheUser.fullName,
                                    genero = cacheUser.gender,
                                    telefono = cacheUser.phoneNumber,
                                    distrito = cacheUser.district,
                                    urbanizacion = cacheUser.neighborhood,
                                    contrasena = cacheUser.password,
                                    fotoPerfil = cacheUser.profilePhotoUrl
                                )
                            }
                        }

                        // Si por UID no encontró, buscar el último registrado o en caché de la app
                        if (restoredEntity == null) {
                            val primerCache = dao.obtenerPrimerUsuarioCache()
                            if (primerCache != null) {
                                restoredEntity = UsuarioEntity(
                                    correo = primerCache.email,
                                    dni = primerCache.dni,
                                    nombreCompleto = primerCache.fullName,
                                    genero = primerCache.gender,
                                    telefono = primerCache.phoneNumber,
                                    distrito = primerCache.district,
                                    urbanizacion = primerCache.neighborhood,
                                    contrasena = primerCache.password,
                                    fotoPerfil = primerCache.profilePhotoUrl
                                )
                            } else {
                                restoredEntity = dao.obtenerUltimoUsuarioRegistrado()
                            }
                        }

                        if (restoredEntity != null) {
                            SessionManager.setCurrentUser(restoredEntity)
                            Log.i("LIMA_SESSION", "✅ Sesión recordada restaurada para: ${restoredEntity.correo} (${restoredEntity.nombreCompleto})")
                        } else {
                            Log.w("LIMA_SESSION", "⚠️ No se encontró usuario para restaurar sesión recordada.")
                        }
                    } catch (e: Exception) {
                        Log.w("LIMA_SESSION", "Error al restaurar sesión recordada: ${e.message}")
                    }
                }
            } else if (!remember && savedInstanceState == null) {
                SessionManager.clearSession()
            }
        }

        splashScreen.setKeepOnScreenCondition { !mainViewModel.isReady }

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
                            // Siempre pasa por Splash -> Pantalla de Emergencia
                            val startDestination = when {
                                skipLoadingScreen == true -> Screen.Emergency.route
                                else -> Screen.Splash.route
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
