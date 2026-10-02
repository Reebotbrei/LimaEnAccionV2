package com.limaenaccion.feature_auth.presentation

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Email
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.ui.components.PrimaryButton

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ForgotPasswordScreen(
    onNavigateBack: () -> Unit,
    onCodeVerified: () -> Unit,
    viewModel: ForgotPasswordViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    LaunchedEffect(uiState.codeVerified) {
        if (uiState.codeVerified) onCodeVerified()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Recuperar Contraseña") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.Filled.ArrowBack, contentDescription = "Volver")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            PrimaryButton(
                text = "Enviar código de recuperación",
                onClick = viewModel::onSendCodeClicked,
                modifier = Modifier.padding(16.dp)
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp)) {
            Text("¿Olvidaste tu contraseña?", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            Text(
                "Ingresa tu correo registrado. Te enviaremos un código de seguridad de 4 dígitos para restablecer tu cuenta.",
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))
            Text("Correo electrónico", style = MaterialTheme.typography.labelSmall)
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.email,
                onValueChange = viewModel::onEmailChange,
                placeholder = { Text("correo@ejemplo.com") },
                leadingIcon = { Icon(Icons.Filled.Email, contentDescription = null) },
                singleLine = true,
                isError = uiState.emailError != null,
                supportingText = uiState.emailError?.let { { Text(it) } },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                modifier = Modifier.fillMaxWidth()
            )
        }
    }

    if (uiState.showCodeDialog) {
        ValidateCodeDialog(
            email = uiState.email,
            codeDigits = uiState.codeDigits,
            codeError = uiState.codeError,
            onDigitChange = viewModel::onDigitChange,
            onResendClick = viewModel::onResendCodeClicked,
            onVerifyClick = viewModel::onVerifyCodeClicked,
            onDismiss = viewModel::onDismissCodeDialog
        )
    }
}

@Composable
private fun ValidateCodeDialog(
    email: String,
    codeDigits: List<String>,
    codeError: String?,
    onDigitChange: (Int, String) -> Unit,
    onResendClick: () -> Unit,
    onVerifyClick: () -> Unit,
    onDismiss: () -> Unit
) {
    val focusRequesters = remember { List(4) { FocusRequester() } }

    LaunchedEffect(Unit) {
        focusRequesters[0].requestFocus()
    }

    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(20.dp), color = MaterialTheme.colorScheme.surface) {
            Column(
                modifier = Modifier.padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    "Ingresa el código",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = MaterialTheme.colorScheme.primary
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    "Enviamos un código de 4 dígitos a $email",
                    textAlign = TextAlign.Center,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(20.dp))

                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    codeDigits.forEachIndexed { index, digit ->
                        OutlinedTextField(
                            value = digit,
                            onValueChange = { newValue ->
                                val singleDigit = newValue.filter { it.isDigit() }.take(1)
                                onDigitChange(index, singleDigit)
                                if (singleDigit.isNotEmpty() && index < 3) {
                                    focusRequesters[index + 1].requestFocus()
                                } else if (singleDigit.isEmpty() && index > 0) {
                                    focusRequesters[index - 1].requestFocus()
                                }
                            },
                            singleLine = true,
                            textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 20.sp),
                            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                            isError = codeError != null,
                            modifier = Modifier
                                .size(56.dp)
                                .focusRequester(focusRequesters[index])
                        )
                    }
                }

                if (codeError != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(codeError, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
                }

                Spacer(modifier = Modifier.height(12.dp))
                Row {
                    Text(
                        "¿No recibiste el código? ",
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        style = MaterialTheme.typography.labelSmall
                    )
                    Text(
                        "Reenviar",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.clickable(onClick = onResendClick)
                    )
                }

                Spacer(modifier = Modifier.height(20.dp))
                PrimaryButton(text = "Verificar", onClick = onVerifyClick)
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onDismiss) {
                    Text("Cancelar", color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
            }
        }
    }
}