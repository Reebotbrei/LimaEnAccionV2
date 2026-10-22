package com.limaenaccion.feature_sos.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.util.LocalAppStrings

@Composable
fun SosActivationScreen(
    onActivated: () -> Unit,
    onCancelled: () -> Unit,
    viewModel: SosActivationViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()

    LaunchedEffect(uiState.activated) { if (uiState.activated) onActivated() }
    LaunchedEffect(uiState.cancelled) { if (uiState.cancelled) onCancelled() }

    // Fondo: si modo silencioso está activo, fondo pizarra oscuro anti-delación
    val backgroundColor = if (uiState.isSilentMode) Color(0xFF0F172A) else Color(0xFF064E3B)
    val circleBackground = if (uiState.isSilentMode) Color(0xFF1E293B) else Color(0xFF047857)

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
            .statusBarsPadding()
            .navigationBarsPadding(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(scrollState)
                .padding(horizontal = 24.dp, vertical = 32.dp),
            verticalArrangement = Arrangement.SpaceBetween
        ) {
            // Cabecera e Indicadores de Modo
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                if (uiState.isDemoMode) {
                    Surface(
                        color = Color(0xFFF59E0B),
                        shape = RoundedCornerShape(16.dp),
                        modifier = Modifier.padding(bottom = 12.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                        ) {
                            Icon(Icons.Filled.Science, contentDescription = null, tint = Color.Black, modifier = Modifier.size(16.dp))
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                strings.sosDemoBadge,
                                color = Color.Black,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.ExtraBold
                            )
                        }
                    }
                }

                Text(
                    "ACTIVANDO ALERTA SOS EN",
                    color = Color.White,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.2.sp
                )

                Spacer(modifier = Modifier.height(6.dp))
                Text(
                    strings.sosDistrictFreeNotice,
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.bodySmall,
                    textAlign = TextAlign.Center
                )
            }

            // Contador Central
            Box(
                modifier = Modifier
                    .size(180.dp)
                    .clip(CircleShape)
                    .background(circleBackground)
                    .border(4.dp, Color.White.copy(alpha = 0.4f), CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    uiState.secondsRemaining.toString(),
                    color = Color.White,
                    fontSize = 76.sp,
                    fontWeight = FontWeight.Black
                )
            }

            // Opciones y Botón Cancelar
            Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.fillMaxWidth()) {
                // Selector rápido de Modo Silencioso y Modo Prueba
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    // Chip Modo Silencioso
                    Surface(
                        color = if (uiState.isSilentMode) Color(0xFF334155) else Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.toggleSilentMode() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.AutoMirrored.Filled.VolumeOff,
                                contentDescription = null,
                                tint = if (uiState.isSilentMode) Color(0xFF38BDF8) else Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (uiState.isSilentMode) "Silencioso: ON" else "Modo Silencioso",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }

                    // Chip Modo Prueba
                    Surface(
                        color = if (uiState.isDemoMode) Color(0xFFB45309) else Color.White.copy(alpha = 0.12f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.toggleDemoMode() }
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.Center,
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 8.dp)
                        ) {
                            Icon(
                                Icons.Filled.Science,
                                contentDescription = null,
                                tint = if (uiState.isDemoMode) Color(0xFFFDE68A) else Color.White.copy(alpha = 0.8f),
                                modifier = Modifier.size(18.dp)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                if (uiState.isDemoMode) "Prueba: ON" else "Modo Prueba",
                                color = Color.White,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.SemiBold
                            )
                        }
                    }
                }

                Spacer(modifier = Modifier.height(20.dp))

                // Botón Cancelar con 1 toque garantizado
                Button(
                    onClick = viewModel::onCancelClicked,
                    colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                    shape = RoundedCornerShape(14.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(54.dp)
                ) {
                    Icon(Icons.Filled.Close, contentDescription = null, tint = Color(0xFFDC2626))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        strings.sosCancelTap,
                        color = Color(0xFFDC2626),
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.titleMedium
                    )
                }

                Spacer(modifier = Modifier.height(12.dp))

                Text(
                    "Al finalizar la cuenta, se activará la transmisión de auxilio y el panel de marcación rápida.",
                    color = Color.White.copy(alpha = 0.75f),
                    style = MaterialTheme.typography.labelSmall,
                    textAlign = TextAlign.Center,
                    modifier = Modifier.padding(horizontal = 16.dp)
                )
            }
        }
    }
}
