package com.limaenaccion.feature_sos.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.util.LocalAppStrings
import com.limaenaccion.feature_sos.domain.model.SosDeliveryState

@Composable
fun SosActiveScreen(
    onNavigateToEmergencyDirectory: () -> Unit,
    onDeactivated: () -> Unit,
    viewModel: SosActiveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current
    val context = LocalContext.current
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.deactivated) {
        if (uiState.deactivated) onDeactivated()
    }

    // Adaptación a Modo Silencioso (Pantalla oscura atenuada anti-delación)
    val rootBackground = if (uiState.isSilentMode) Color(0xFF0F172A) else Color(0xFF7F1D1D)
    val cardBackground = if (uiState.isSilentMode) Color(0xFF1E293B) else Color(0xFF991B1B)
    val cardBorder = if (uiState.isSilentMode) Color(0xFF334155) else Color(0xFFB91C1C)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(rootBackground)
            .statusBarsPadding()
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 20.dp, vertical = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Fila Superior de Modos (Silencioso y Prueba)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Chip Modo Silencioso
                Surface(
                    color = if (uiState.isSilentMode) Color(0xFF38BDF8).copy(alpha = 0.2f) else Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    border = androidx.compose.foundation.BorderStroke(
                        1.dp,
                        if (uiState.isSilentMode) Color(0xFF38BDF8) else Color.Transparent
                    ),
                    modifier = Modifier.clickable { viewModel.toggleSilentMode() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.AutoMirrored.Filled.VolumeOff,
                            contentDescription = null,
                            tint = if (uiState.isSilentMode) Color(0xFF38BDF8) else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (uiState.isSilentMode) "Modo Silencioso ACTIVO" else "Modo Silencioso",
                            color = if (uiState.isSilentMode) Color(0xFF38BDF8) else Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }

                // Chip Modo Prueba
                Surface(
                    color = if (uiState.isDemoMode) Color(0xFFF59E0B) else Color.White.copy(alpha = 0.15f),
                    shape = RoundedCornerShape(20.dp),
                    modifier = Modifier.clickable { viewModel.toggleDemoMode() }
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            Icons.Filled.Science,
                            contentDescription = null,
                            tint = if (uiState.isDemoMode) Color.Black else Color.White,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            if (uiState.isDemoMode) "Modo Prueba" else "Simulación",
                            color = if (uiState.isDemoMode) Color.Black else Color.White,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Banner explicativo si Modo Silencioso está activo
            if (uiState.isSilentMode) {
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFF38BDF8).copy(alpha = 0.4f)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = Color(0xFF38BDF8), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            strings.sosSilentModeDesc,
                            color = Color(0xFFE2E8F0),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Banner explicativo si Modo Demo está activo
            if (uiState.isDemoMode) {
                Surface(
                    color = Color(0xFF78350F),
                    shape = RoundedCornerShape(12.dp),
                    border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFF59E0B)),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(Icons.Filled.Science, contentDescription = null, tint = Color(0xFFFDE68A), modifier = Modifier.size(20.dp))
                        Spacer(modifier = Modifier.width(10.dp))
                        Text(
                            strings.sosDemoNotice,
                            color = Color(0xFFFEF3C7),
                            style = MaterialTheme.typography.bodySmall,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
                Spacer(modifier = Modifier.height(14.dp))
            }

            // Cabecera Principal SOS
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.Center,
                modifier = Modifier.fillMaxWidth()
            ) {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(if (uiState.isSilentMode) Color(0xFF334155) else Color.White.copy(alpha = 0.2f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = if (uiState.isSilentMode) Color(0xFF38BDF8) else Color.White,
                        modifier = Modifier.size(28.dp)
                    )
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(
                        "¡ALERTA SOS EN CURSO!",
                        color = Color.White,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Black
                    )
                    Text(
                        strings.sosDistrictFreeNotice,
                        color = Color.White.copy(alpha = 0.85f),
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Tarjeta de Estados Explícitos y Honestos (Entrega Real)
            Card(
                colors = CardDefaults.cardColors(containerColor = cardBackground),
                shape = RoundedCornerShape(16.dp),
                border = androidx.compose.foundation.BorderStroke(1.dp, cardBorder),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "ESTADO HONESTO DE LA ENTREGA",
                        color = Color.White.copy(alpha = 0.75f),
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        letterSpacing = 1.sp
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    // Paso 1: Fijación GPS
                    SosStatusStepRow(
                        icon = Icons.Filled.LocationOn,
                        title = strings.sosStepGps,
                        description = "Coordenadas: ${uiState.latitude}, ${uiState.longitude} (${uiState.addressDesc})",
                        isCompleted = true,
                        isLoading = false,
                        accentColor = Color(0xFF22C55E)
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // Paso 2: Central de Emergencias
                    val serverCompleted = uiState.deliveryState == SosDeliveryState.RECIBIDA_SISTEMA ||
                            uiState.deliveryState == SosDeliveryState.CONTACTOS_NOTIFICADOS
                    val serverLoading = uiState.deliveryState == SosDeliveryState.ENVIANDO
                    val serverError = uiState.deliveryState == SosDeliveryState.ERROR_ENVIO

                    val serverDesc = when {
                        serverError -> uiState.errorMessage ?: strings.sosStatusError
                        serverCompleted -> strings.sosStatusReceived
                        serverLoading -> strings.sosStatusSending
                        else -> strings.sosStatusPreparing
                    }

                    SosStatusStepRow(
                        icon = if (serverError) Icons.Filled.Warning else Icons.Filled.Shield,
                        title = strings.sosStepServer,
                        description = serverDesc,
                        isCompleted = serverCompleted,
                        isLoading = serverLoading,
                        isError = serverError,
                        accentColor = if (serverError) Color(0xFFF87171) else Color(0xFF38BDF8)
                    )

                    if (serverError) {
                        Spacer(modifier = Modifier.height(8.dp))
                        OutlinedButton(
                            onClick = viewModel::retryDispatch,
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                            shape = RoundedCornerShape(8.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(40.dp)
                        ) {
                            Icon(Icons.Filled.Refresh, contentDescription = null, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(strings.sosRetryBtn, style = MaterialTheme.typography.labelMedium)
                        }
                    }

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // Paso 3: Contactos de Auxilio Notificados por SMS
                    val contactsCompleted = uiState.deliveryState == SosDeliveryState.CONTACTOS_NOTIFICADOS
                    SosStatusStepRow(
                        icon = Icons.AutoMirrored.Filled.Message,
                        title = strings.sosStepContacts,
                        description = if (contactsCompleted) {
                            "SMS preparado con coordenadas GPS exactas (${uiState.contactPhones.size} contactos registrados)"
                        } else {
                            "Preparando mensaje satelital y lista de contactos..."
                        },
                        isCompleted = contactsCompleted,
                        isLoading = uiState.deliveryState == SosDeliveryState.RECIBIDA_SISTEMA,
                        accentColor = Color(0xFFFBBF24)
                    )

                    HorizontalDivider(
                        color = Color.White.copy(alpha = 0.15f),
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    // Paso 4: Estado de Atención (Diferenciación honesta)
                    SosStatusStepRow(
                        icon = Icons.Filled.Info,
                        title = strings.sosStepAttention,
                        description = strings.sosStatusAttentionWait,
                        isCompleted = false,
                        isLoading = false,
                        accentColor = Color(0xFF94A3B8)
                    )
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // SECCIÓN: ACCESO DIRECTO A LLAMAR (SIN PASAR POR DIRECTORIO)
            Text(
                "MARCACIÓN RÁPIDA DE EMERGENCIA",
                color = Color.White.copy(alpha = 0.85f),
                style = MaterialTheme.typography.labelSmall,
                fontWeight = FontWeight.Bold,
                letterSpacing = 1.sp,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(bottom = 8.dp)
            )

            // Botón Prioritario: Llamar a Contacto Principal
            val primary = uiState.primaryContact
            val primaryPhone = primary?.telefono ?: ""
            val primaryName = primary?.nombre ?: "Contacto de Auxilio"

            Button(
                onClick = {
                    val phoneToDial = if (primaryPhone.isNotBlank()) primaryPhone else "105"
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:$phoneToDial")
                    }
                    try {
                        context.startActivity(dialIntent)
                    } catch (_: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF16A34A)),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(64.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Call, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            strings.sosDirectCallContact(primaryName),
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            if (primaryPhone.isNotBlank()) "$primaryPhone · Principal" else "Toca para marcar",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Botón Prioritario: Llamar al 105 (Policía Nacional)
            Button(
                onClick = {
                    val dialIntent = Intent(Intent.ACTION_DIAL).apply {
                        data = Uri.parse("tel:105")
                    }
                    try {
                        context.startActivity(dialIntent)
                    } catch (_: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isSilentMode) Color(0xFFB91C1C) else Color(0xFFDC2626)
                ),
                shape = RoundedCornerShape(14.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(40.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Shield, contentDescription = null, tint = Color.White)
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            strings.sosDirectCallPnp,
                            color = Color.White,
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "Central de Radio Patrulla PNP",
                            color = Color.White.copy(alpha = 0.85f),
                            style = MaterialTheme.typography.bodySmall
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Fila de Llamada Rápida: SAMU 106 y Bomberos 116
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Button(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:106") }
                        try { context.startActivity(dialIntent) } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF0284C7)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("SAMU 106", color = Color.White, fontWeight = FontWeight.Bold)
                }

                Button(
                    onClick = {
                        val dialIntent = Intent(Intent.ACTION_DIAL).apply { data = Uri.parse("tel:116") }
                        try { context.startActivity(dialIntent) } catch (_: Exception) {}
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFEA580C)),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                ) {
                    Icon(Icons.Filled.Call, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Bomberos 116", color = Color.White, fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Botón Envío SMS con Ubicación GPS
            Button(
                onClick = {
                    val recipients = uiState.contactPhones.joinToString(";")
                    val smsIntent = Intent(Intent.ACTION_SENDTO).apply {
                        data = Uri.parse("smsto:$recipients")
                        putExtra("sms_body", uiState.smsText)
                    }
                    try {
                        context.startActivity(smsIntent)
                        viewModel.onSmsSent()
                    } catch (_: Exception) {}
                },
                colors = ButtonDefaults.buttonColors(
                    containerColor = if (uiState.isSilentMode) Color(0xFF334155) else Color(0xFF6B21A8)
                ),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Enviar SMS GPS a Contactos (${uiState.contactPhones.size})",
                    color = Color.White,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Enlace al directorio completo si el usuario desea buscar serenazgo distrital
            TextButton(
                onClick = onNavigateToEmergencyDirectory,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(
                    strings.sosFullDirectoryLink,
                    color = Color.White.copy(alpha = 0.9f),
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.SemiBold
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Botón Desactivar Alerta SOS
            Button(
                onClick = viewModel::onDeactivateClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
            ) {
                Text(
                    strings.deactivateAlertBtn,
                    color = Color(0xFFDC2626),
                    fontWeight = FontWeight.ExtraBold,
                    style = MaterialTheme.typography.titleMedium
                )
            }

            Spacer(modifier = Modifier.height(16.dp))
        }
    }
}

@Composable
private fun SosStatusStepRow(
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    title: String,
    description: String,
    isCompleted: Boolean,
    isLoading: Boolean,
    isError: Boolean = false,
    accentColor: Color
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .clip(CircleShape)
                .background(accentColor.copy(alpha = 0.2f)),
            contentAlignment = Alignment.Center
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(18.dp),
                    strokeWidth = 2.dp,
                    color = Color.White
                )
            } else if (isCompleted) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(20.dp)
                )
            } else {
                Icon(
                    icon,
                    contentDescription = null,
                    tint = accentColor,
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                title,
                color = Color.White,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                description,
                color = Color.White.copy(alpha = 0.8f),
                style = MaterialTheme.typography.bodySmall
            )
        }
    }
}
