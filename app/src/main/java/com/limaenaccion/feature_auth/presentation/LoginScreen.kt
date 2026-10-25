package com.limaenaccion.feature_auth.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.ui.components.AppLogo
import com.limaenaccion.core.ui.components.BrandLayout
import com.limaenaccion.core.ui.components.LogoVariant
import com.limaenaccion.core.ui.components.PrimaryButton
import com.limaenaccion.core.ui.components.ServerConfigCard
import com.limaenaccion.core.util.LocalAppStrings
import kotlinx.coroutines.TimeoutCancellationException
import kotlinx.coroutines.withTimeout

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    onForgotPasswordClick: () -> Unit,
    onRegisterClick: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()
    val haptic = LocalHapticFeedback.current

    LaunchedEffect(uiState.loginSuccess) {
        if (uiState.loginSuccess) onLoginSuccess()
    }

    Surface(
        modifier = Modifier.fillMaxSize(),
        color = MaterialTheme.colorScheme.background
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            // ── Logo e identidad (Pulsación sostenida por 5 segundos abre configuración técnica) ──
            Box(
                modifier = Modifier.pointerInput(Unit) {
                    awaitEachGesture {
                        awaitFirstDown(requireUnconsumed = false)
                        try {
                            withTimeout(5000L) {
                                while (true) {
                                    val event = awaitPointerEvent()
                                    if (event.changes.all { !it.pressed }) {
                                        break
                                    }
                                }
                            }
                        } catch (e: TimeoutCancellationException) {
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onShowServerConfig()
                        }
                    }
                }
            ) {
                AppLogo(
                    variant = LogoVariant.FULL_BRAND,
                    layout = BrandLayout.ICON_TOP,
                    size = 76.dp,
                    useSquircleInFullBrand = true
                )
            }
            Spacer(modifier = Modifier.height(10.dp))
            Text(
                strings.loginSubtitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(28.dp))

            Text(strings.loginEmailLabel, style = MaterialTheme.typography.labelSmall, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = { Text("vecino.lima@gmail.com") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                singleLine = true,
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Text(strings.loginPasswordLabel, style = MaterialTheme.typography.labelSmall, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.password,
                onValueChange = viewModel::onPasswordChange,
                placeholder = { Text("••••••••") },
                leadingIcon = { Icon(Icons.Filled.Lock, contentDescription = null) },
                trailingIcon = {
                    IconButton(onClick = viewModel::onTogglePasswordVisibility) {
                        Icon(
                            imageVector = if (uiState.isPasswordVisible) Icons.Filled.VisibilityOff else Icons.Filled.Visibility,
                            contentDescription = if (uiState.isPasswordVisible) "Ocultar" else "Mostrar"
                        )
                    }
                },
                visualTransformation = if (uiState.isPasswordVisible) VisualTransformation.None else PasswordVisualTransformation(),
                singleLine = true,
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))
            TextButton(onClick = onForgotPasswordClick, modifier = Modifier.align(Alignment.End)) {
                Text(strings.loginForgotPasswordBtn, color = MaterialTheme.colorScheme.primary)
            }

            // ── Opción de Recordar Sesión ──
            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { viewModel.onRememberSessionChange(!uiState.rememberSession) }
                    .padding(vertical = 4.dp)
            ) {
                Checkbox(
                    checked = uiState.rememberSession,
                    onCheckedChange = viewModel::onRememberSessionChange
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = if (strings.loginEmailLabel == "Correo electrónico") "Recordar sesión" else "Remember session",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (uiState.generalError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.generalError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            if (uiState.isLoading) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    CircularProgressIndicator(modifier = Modifier.size(36.dp))
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = if (strings.loginEmailLabel == "Correo electrónico") "Verificando credenciales..." else "Verifying credentials...",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                PrimaryButton(
                    text = strings.loginSubmitBtn,
                    onClick = viewModel::onLoginClicked,
                    enabled = !uiState.isLoading
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
            Row {
                Text(strings.loginNoAccountPrompt, color = MaterialTheme.colorScheme.onSurfaceVariant)
                Text(
                    strings.loginRegisterLink,
                    color = MaterialTheme.colorScheme.primary,
                    fontWeight = FontWeight.Bold,
                    modifier = Modifier.clickable(onClick = onRegisterClick)
                )
            }
        }
    }

    // ── Diálogo modal flotante de desarrollador (sólo accesible mediante pulsación larga en el logo) ──
    if (uiState.showServerConfig) {
        Dialog(
            onDismissRequest = viewModel::onDismissServerConfig,
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp)
            ) {
                ServerConfigCard(
                    currentUrl = uiState.serverUrl,
                    onUrlConfirmed = viewModel::onServerUrlChanged,
                    onDismiss = viewModel::onDismissServerConfig
                )
            }
        }
    }
}
