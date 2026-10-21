package com.limaenaccion.feature_sos.presentation

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Message
import androidx.compose.material.icons.filled.Bolt
import androidx.compose.material.icons.filled.Phone
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
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.util.LocalAppStrings

@Composable
fun SosActiveScreen(
    onNavigateToEmergencyDirectory: () -> Unit,
    onDeactivated: () -> Unit,
    viewModel: SosActiveViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current
    val context = LocalContext.current

    LaunchedEffect(uiState.deactivated) { if (uiState.deactivated) onDeactivated() }

    Box(modifier = Modifier.fillMaxSize().background(Color(0xFFDC2626)), contentAlignment = Alignment.Center) {
        Column(horizontalAlignment = Alignment.CenterHorizontally, modifier = Modifier.padding(24.dp)) {
            Box(modifier = Modifier.size(72.dp).clip(CircleShape).background(Color.White.copy(alpha = 0.2f)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(36.dp))
            }
            Spacer(modifier = Modifier.height(16.dp))
            Text(strings.sosActiveTitle, color = Color.White, style = MaterialTheme.typography.headlineMedium, fontWeight = FontWeight.Bold, textAlign = TextAlign.Center)
            Spacer(modifier = Modifier.height(12.dp))
            Text(strings.sosActiveRadiusDesc(uiState.radiusMeters), color = Color.White, textAlign = TextAlign.Center)
            Text("${uiState.notifiedNeighbors} vecinos notificados · Hace 0 seg", color = Color.White.copy(alpha = 0.85f), style = MaterialTheme.typography.labelSmall)

            Spacer(modifier = Modifier.height(24.dp))

            // Pref #12: Envío de SMS con ubicación GPS a contactos de auxilio sin app
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
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB91C1C)),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.AutoMirrored.Filled.Message, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.sendGpsSmsBtn, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = onNavigateToEmergencyDirectory,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF991B1B)),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) {
                Icon(Icons.Filled.Phone, contentDescription = null, tint = Color.White)
                Spacer(modifier = Modifier.width(8.dp))
                Text(strings.contactAuthorityBtn, color = Color.White, fontWeight = FontWeight.Bold)
            }

            Spacer(modifier = Modifier.height(12.dp))
            Button(
                onClick = viewModel::onDeactivateClicked,
                colors = ButtonDefaults.buttonColors(containerColor = Color.White),
                modifier = Modifier.fillMaxWidth().height(52.dp)
            ) { Text(strings.deactivateAlertBtn, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold) }

            Spacer(modifier = Modifier.height(16.dp))
            Text("Policía Nacional: 105 · SAMU: 106", color = Color.White.copy(alpha = 0.8f), style = MaterialTheme.typography.labelSmall)
        }
    }
}
