package com.limaenaccion

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import com.limaenaccion.core.navigation.NavGraph
import com.limaenaccion.core.navigation.Screen
import com.limaenaccion.core.ui.theme.LimaEnAccionTheme

class MainActivity : ComponentActivity() {

    private val mainViewModel: MainViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)

        splashScreen.setKeepOnScreenCondition { !mainViewModel.isReady }

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
}

