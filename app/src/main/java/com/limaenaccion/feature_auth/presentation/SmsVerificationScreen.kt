package com.limaenaccion.feature_auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SmsVerificationScreen(
    phoneNumber: String,
    onNavigateBack: () -> Unit,
    onVerified: () -> Unit,
    viewModel: SmsVerificationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val focusRequesters = remember { List(6) { FocusRequester() } }

    LaunchedEffect(uiState.verified) {
        if (uiState.verified) onVerified()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Column { Text("Verificación"); Text("Código de seguridad", style = MaterialTheme.typography.labelSmall) } },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                    titleContentColor = MaterialTheme.colorScheme.onPrimary,
                    navigationIconContentColor = MaterialTheme.colorScheme.onPrimary
                )
            )
        },
        bottomBar = {
            PrimaryButton(text = "Verificar y Acceder", onClick = viewModel::onVerifyClicked, modifier = Modifier.padding(16.dp))
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(16.dp))
            Box(
                modifier = Modifier.size(64.dp).clip(CircleShape).background(MaterialTheme.colorScheme.primaryContainer),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Filled.Sms, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text("Verifica tu número", style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                "Hemos enviado un código SMS de 6 dígitos al celular $phoneNumber. Por favor, ingrésalo a continuación.",
                textAlign = TextAlign.Center,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(24.dp))

            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
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
                        textStyle = TextStyle(textAlign = TextAlign.Center, fontSize = 18.sp),
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        isError = uiState.codeError != null,
                        modifier = Modifier.size(48.dp).focusRequester(focusRequesters[index])
                    )
                }
            }

            if (uiState.codeError != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(uiState.codeError!!, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.labelSmall)
            }

            Spacer(modifier = Modifier.height(16.dp))
            if (uiState.canResend) {
                Row {
                    Text("¿No recibiste el código? ", color = MaterialTheme.colorScheme.onSurfaceVariant, style = MaterialTheme.typography.labelSmall)
                    Text(
                        "Reenviar",
                        color = MaterialTheme.colorScheme.primary,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.labelSmall,
                        modifier = Modifier.clickable(onClick = viewModel::onResendClicked)
                    )
                }
            } else {
                Text(
                    "¿No recibiste el código? Reenviar en 0:${uiState.secondsRemaining.toString().padStart(2, '0')}",
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    style = MaterialTheme.typography.labelSmall
                )
            }
        }
    }
}