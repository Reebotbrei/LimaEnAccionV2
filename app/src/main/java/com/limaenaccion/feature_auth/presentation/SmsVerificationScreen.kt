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
                        Text("Activación por Correo", style = MaterialTheme.typography.labelSmall)
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
                .padding(horizontal = 20.dp, vertical = 20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(8.dp))

            Box(
                modifier = Modifier
                    .size(68.dp)
                    .clip(CircleShape)
                    .background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Filled.MarkEmailRead,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(34.dp)
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                "Verifica tu Correo",
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold
            )

            Spacer(modifier = Modifier.height(8.dp))

            Text(
                text = "Hemos enviado un código de seguridad de 6 dígitos a:\n$email\n\n(Revisa tu bandeja de entrada o spam. Válido por 1 minuto)",
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Casillas amplias de 6 dígitos con diseño moderno y centrado
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
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
                        shape = RoundedCornerShape(12.dp),
                        textStyle = TextStyle(
                            textAlign = TextAlign.Center,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold
                        ),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = MaterialTheme.colorScheme.primary,
                            unfocusedBorderColor = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            focusedContainerColor = MaterialTheme.colorScheme.surface,
                            unfocusedContainerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.25f)
                        ),
                        isError = uiState.codeError != null,
                        modifier = Modifier
                            .weight(1f)
                            .height(64.dp)
                            .focusRequester(focusRequesters[index])
                    )
                }
            }

            if (uiState.codeError != null) {
                Spacer(modifier = Modifier.height(14.dp))
                Surface(
                    shape = RoundedCornerShape(10.dp),
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
                        modifier = Modifier.padding(10.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            if (uiState.canResend) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        "¿No recibiste el correo? ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.bodySmall
                    )
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

            Spacer(modifier = Modifier.height(28.dp))

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
                text = "Modificar datos o corregir correo",
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
