package com.limaenaccion.feature_auth.presentation

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.MarkEmailRead
import androidx.compose.material.icons.filled.Sms
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.ui.components.PrimaryButton
import com.limaenaccion.core.util.LocalAppStrings

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsVerificationScreen(
    phoneNumber: String,
    email: String = "",
    simulatedSmsNotice: String? = null,
    onNavigateBack: () -> Unit,
    onVerified: () -> Unit,
    onCancelRegistration: () -> Unit = onNavigateBack,
    onVerifyCode: ((String, (Boolean, String?) -> Unit) -> Unit)? = null,
    onResendCode: (((Boolean, String?) -> Unit) -> Unit)? = null,
    viewModel: SmsVerificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()
    val focusRequesters = remember { List(6) { FocusRequester() } }

    // Intercepta la flecha atrás del celular para volver a la pantalla de registro
    BackHandler(enabled = true) {
        onNavigateBack()
    }

    LaunchedEffect(uiState.verified) {
        if (uiState.verified) onVerified()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Column {
                        Text("Verificación de Seguridad")
                        Text("Activación de Cuenta", style = MaterialTheme.typography.labelSmall)
                    }
                },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .imePadding()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Banner de SMS celular simulado en pantalla
            if (!simulatedSmsNotice.isNullOrBlank()) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.7f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 16.dp)
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Filled.Sms,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(10.dp))
                        Column {
                            Text(
                                text = "SMS Recibido en tu Celular",
                                style = MaterialTheme.typography.labelMedium,
                                fontWeight = FontWeight.Bold,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Text(
                                text = simulatedSmsNotice,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                }
            }

            Box(
                modifier = Modifier
                    .size(60.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.MarkEmailRead, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(30.dp))
            }
            Spacer(modifier = Modifier.height(12.dp))
            Text("Verifica tu Identidad", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(6.dp))
            Text(
                text = "Hemos enviado un código de 6 dígitos a tu celular $phoneNumber ${if (email.isNotBlank()) "y a tu correo Gmail $email" else ""}.\n\nVálido durante 1 minuto (60 segundos).",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(20.dp))

            // Casillas de 6 dígitos
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.padding(vertical = 4.dp)
            ) {
                uiState.codeDigits.forEachIndexed { index, digit ->
                    OutlinedTextField(
                        value = digit,
                        onValueChange = { newValue ->
                            val singleDigit = newValue.filter { it.isDigit() }.take(1)
                            viewModel.onDigitChange(index, singleDigit)
                            if (singleDigit.isNotEmpty() && index < 5) {
                                focusRequesters[index + 1].requestFocus()
                            } else if (singleDigit.isEmpty() && index > 0) {
                                focusRequesters[index - 1].requestFocus()
                            }
                        },
                        singleLine = true,
                        textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 20.sp, fontWeight = FontWeight.Bold),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.codeError != null,
                        modifier = Modifier
                            .size(48.dp)
                            .focusRequester(focusRequesters[index])
                    )
                }
            }

            if (uiState.codeError != null) {
                Spacer(modifier = Modifier.height(12.dp))
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = MaterialTheme.colorScheme.errorContainer.copy(alpha = 0.5f),
                    border = BorderStroke(1.dp, MaterialTheme.colorScheme.error),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(
                        text = "❌ ${uiState.codeError!!}",
                        color = MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.bodySmall,
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center,
                        modifier = Modifier.padding(8.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (uiState.canResend) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("¿No recibiste el código? ", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.bodySmall)
                    Text(
                        "Reenviar código",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.clickable {
                            viewModel.resetDigits()
                            if (onResendCode != null) {
                                onResendCode { success, errorMsg ->
                                    if (!success && errorMsg != null) {
                                        viewModel.setError(errorMsg)
                                    }
                                }
                            }
                        }
                    )
                }
            } else {
                Text(
                    "Reenviar nuevo código en 0:${uiState.secondsRemaining.toString().padStart(2, '0')} min",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.bodySmall,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Botón Principal responsivo dentro del scroll
            PrimaryButton(
                text = if (uiState.isVerifying) "Verificando..." else "Verificar y Crear Cuenta",
                onClick = {
                    val code = uiState.fullCode
                    if (code.length < 6) {
                        viewModel.setError("Ingresa el código completo de 6 dígitos")
                        return@PrimaryButton
                    }
                    if (uiState.secondsRemaining <= 0) {
                        viewModel.setError("El código ha expirado (validez de 1 minuto). Pulsa en 'Reenviar' para generar uno nuevo.")
                        return@PrimaryButton
                    }
                    viewModel.setVerifying(true)
                    if (onVerifyCode != null) {
                        onVerifyCode(code) { success, errorMsg ->
                            viewModel.setVerifying(false)
                            if (success) {
                                viewModel.setVerified(true)
                            } else {
                                viewModel.setError(errorMsg ?: "Código de verificación incorrecto")
                            }
                        }
                    } else {
                        viewModel.setVerified(true)
                    }
                },
                enabled = !uiState.isVerifying,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Enlaces para corregir datos o cancelar
            Text(
                text = "Modificar datos o corregir celular",
                color = MaterialTheme.colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier.clickable(onClick = onNavigateBack)
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = strings.smsCancelRegistrationBtn,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.clickable(onClick = onCancelRegistration)
            )

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}
