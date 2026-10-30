package com.limaenaccion.feature_auth.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.platform.LocalHapticFeedback
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
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
import kotlinx.coroutines.delay

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

    val logoInteractionSource = remember { MutableInteractionSource() }
    val isLogoPressed by logoInteractionSource.collectIsPressedAsState()
    var logoTapCount by remember { mutableStateOf(0) }
    var lastTapTime by remember { mutableStateOf(0L) }

    // ── Gesto 1: Sostener presionado durante 5 segundos continuos ──
    LaunchedEffect(isLogoPressed) {
        if (isLogoPressed) {
            delay(5000L)
            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
            viewModel.onShowServerConfig()
        }
    }

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
            // ── Logo e identidad (5 seg presionado O 5 toques rápidos abre ajustes de desarrollador) ──
            Box(
                modifier = Modifier
                    .clickable(
                        interactionSource = logoInteractionSource,
                        indication = null
                    ) {
                        val now = System.currentTimeMillis()
                        if (now - lastTapTime < 600) {
                            logoTapCount++
                        } else {
                            logoTapCount = 1
                        }
                        lastTapTime = now
                        if (logoTapCount >= 5) {
                            logoTapCount = 0
                            haptic.performHapticFeedback(HapticFeedbackType.LongPress)
                            viewModel.onShowServerConfig()
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

            // ── Bloqueo por 5 intentos fallidos ──
            if (uiState.isLockedOut) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.errorContainer,
                    border = BorderStroke(1.5.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 20.dp)
                ) {
                    Column(
                        modifier = Modifier.padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.error,
                            modifier = Modifier.size(36.dp)
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(
                            text = "Credenciales Incorrectas.. recuperar Cuenta",
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                        Text(
                            text = "Superaste los 5 intentos de inicio de sesión. Por seguridad tu acceso ha sido suspendido temporalmente. Restablece tu contraseña mediante tu correo registrado.",
                            color = MaterialTheme.colorScheme.onErrorContainer,
                            style = MaterialTheme.typography.bodySmall,
                            textAlign = TextAlign.Center
                        )
                        Spacer(modifier = Modifier.height(14.dp))
                        Button(
                            onClick = onForgotPasswordClick,
                            colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier.fillMaxWidth()
                        ) {
                            Text("Recuperar Cuenta Ahora", fontWeight = FontWeight.Bold)
                        }
                    }
                }
            } else if (uiState.remainingAttempts in 1..4) {
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error.copy(alpha = 0.5f)),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 12.dp)
                ) {
                    Text(
                        text = "⚠️ Te quedan ${uiState.remainingAttempts} intento(s) antes del bloqueo de seguridad",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Text(strings.loginEmailLabel, style = MaterialTheme.typography.labelSmall, modifier = Modifier.fillMaxWidth())
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = { Text("vecino.lima@gmail.com") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                singleLine = true,
                enabled = !uiState.isLockedOut && !uiState.isLoading,
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
                enabled = !uiState.isLockedOut && !uiState.isLoading,
                isError = uiState.passwordError != null,
                supportingText = uiState.passwordError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(4.dp))
            TextButton(
                onClick = onForgotPasswordClick,
                modifier = Modifier.align(Alignment.End)
            ) {
                Text(strings.loginForgotPasswordBtn, color = MaterialTheme.colorScheme.primary)
            }

            if (uiState.generalError != null && !uiState.isLockedOut) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = uiState.generalError ?: "",
                    color = MaterialTheme.colorScheme.error,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

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
                    enabled = !uiState.isLoading && !uiState.isLockedOut
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

    // ── Diálogo flotante modal "¿Desea Recordar la sesión? SI / NO" ──
    if (uiState.showRememberSessionDialog) {
        AlertDialog(
            onDismissRequest = { viewModel.onConfirmRememberSession(false) },
            icon = {
                Icon(
                    Icons.Filled.Lock,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(28.dp)
                )
            },
            title = {
                Text(
                    text = "¿Desea Recordar la sesión?",
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleMedium,
                    textAlign = TextAlign.Center
                )
            },
            text = {
                Text(
                    text = "Al seleccionar 'SÍ', mantendrás tu cuenta iniciada en este dispositivo.\n\nAl seleccionar 'NO', si sales de la aplicación o presionas volver, tu sesión se cerrará automáticamente por seguridad.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            },
            confirmButton = {
                Button(
                    onClick = { viewModel.onConfirmRememberSession(true) },
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.primary)
                ) {
                    Text("SÍ", fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                OutlinedButton(
                    onClick = { viewModel.onConfirmRememberSession(false) }
                ) {
                    Text("NO", fontWeight = FontWeight.Bold)
                }
            }
        )
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
