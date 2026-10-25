package com.limaenaccion.feature_onboarding.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.ui.components.AppLogo
import com.limaenaccion.core.ui.components.BrandLayout
import com.limaenaccion.core.ui.components.LogoVariant
import com.limaenaccion.core.ui.components.PrimaryButton

@Composable
fun SplashScreen(
    onNavigateToEmergency: () -> Unit,
    viewModel: SplashViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val dontShowAgain by viewModel.dontShowAgainChecked.collectAsState()

    LaunchedEffect(uiState) {
        if (uiState is SplashUiState.NavigateToEmergency) {
            onNavigateToEmergency()
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            AppLogo(
                variant = LogoVariant.FULL_BRAND,
                layout = BrandLayout.TITLE_TOP,
                size = 96.dp,
                useSquircleInFullBrand = false,
                titleFontSize = 36.sp
            )
            Spacer(modifier = Modifier.height(28.dp))
            CircularProgressIndicator(color = MaterialTheme.colorScheme.primary)
        }

        if (uiState is SplashUiState.ShowSkipDialog) {
            SkipLoadingScreenDialog(
                checked = dontShowAgain,
                onCheckedChange = viewModel::onDontShowAgainChanged,
                onContinueClick = viewModel::onContinueClicked
            )
        }
    }
}

@Composable
private fun SkipLoadingScreenDialog(
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    onContinueClick: () -> Unit
) {
    AlertDialog(
        onDismissRequest = { },
        title = { Text("¿Deseas ver esta pantalla de carga nuevamente al iniciar la aplicación?") },
        text = {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Checkbox(checked = checked, onCheckedChange = onCheckedChange)
                Spacer(modifier = Modifier.width(8.dp))
                Text("No volver a mostrar")
            }
        },
        confirmButton = {
            PrimaryButton(text = "Continuar", onClick = onContinueClick, modifier = Modifier.padding(bottom = 8.dp))
        }
    )
}
