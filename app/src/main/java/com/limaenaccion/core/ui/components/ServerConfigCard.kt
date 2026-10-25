package com.limaenaccion.core.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Cloud
import androidx.compose.material.icons.filled.CloudOff
import androidx.compose.material.icons.filled.Computer
import androidx.compose.material.icons.filled.PhoneAndroid
import androidx.compose.material.icons.filled.Usb
import androidx.compose.material.icons.filled.Wifi
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Tarjeta de configuración del servidor backend.
 * Permite elegir entre presets conocidos o ingresar manualmente una IP/URL.
 * Se usa en LoginScreen y SettingsScreen.
 *
 * @param currentUrl URL actualmente configurada
 * @param onUrlConfirmed Callback cuando el usuario confirma una nueva URL
 * @param onDismiss Callback para cerrar la tarjeta
 */
@Composable
fun ServerConfigCard(
    currentUrl: String,
    onUrlConfirmed: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var inputUrl by remember { mutableStateOf(currentUrl) }

    val presets = listOf(
        Triple("Render (Nube)", Icons.Filled.Cloud, "https://limaenaccion-api.onrender.com/"),
        Triple("Emulador Android", Icons.Filled.Computer, "http://10.0.2.2:3000/"),
        Triple("Red Wi-Fi Local (PC)", Icons.Filled.Wifi, "http://192.168.18.27:3000/"),
        Triple("Cable USB (adb reverse)", Icons.Filled.Usb, "http://127.0.0.1:3000/")
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = MaterialTheme.colorScheme.surface,
        border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
        tonalElevation = 4.dp,
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {

            // Cabecera
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Filled.PhoneAndroid,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    "Configuración del Servidor",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                "Selecciona un preset o ingresa la IP/URL manualmente",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Presets rápidos
            presets.forEach { (label, icon, url) ->
                val isSelected = inputUrl.trimEnd('/') == url.trimEnd('/')
                OutlinedButton(
                    onClick = {
                        inputUrl = url
                    },
                    colors = ButtonDefaults.outlinedButtonColors(
                        containerColor = if (isSelected)
                            MaterialTheme.colorScheme.primaryContainer
                        else
                            Color.Transparent
                    ),
                    border = BorderStroke(
                        1.dp,
                        if (isSelected) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.outlineVariant
                    ),
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 3.dp)
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(label, style = MaterialTheme.typography.bodyMedium, modifier = Modifier.weight(1f))
                        if (isSelected) {
                            Icon(
                                Icons.Filled.Check,
                                contentDescription = "Seleccionado",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Campo de texto manual
            Text(
                "URL / IP personalizada:",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = inputUrl,
                onValueChange = { inputUrl = it },
                placeholder = { Text("ej. http://192.168.18.27:3000/", fontSize = 12.sp) },
                singleLine = true,
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Uri,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { onUrlConfirmed(inputUrl) }
                ),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(16.dp))

            // Botones de acción
            Row(
                horizontalArrangement = Arrangement.End,
                modifier = Modifier.fillMaxWidth()
            ) {
                TextButton(onClick = onDismiss) {
                    Text("Cancelar")
                }
                Spacer(modifier = Modifier.width(8.dp))
                Button(
                    onClick = { onUrlConfirmed(inputUrl) },
                    enabled = inputUrl.isNotBlank()
                ) {
                    Icon(Icons.Filled.Check, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Guardar")
                }
            }
        }
    }
}

/**
 * Chip pequeño que muestra el estado del servidor activo.
 * Se usa en LoginScreen y SettingsScreen como indicador visual sutil.
 */
@Composable
fun ServerStatusChip(
    serverUrl: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCloud = serverUrl.contains("onrender") || serverUrl.contains("ngrok")
    val isLocal = serverUrl.contains("10.0.2.2") || serverUrl.contains("192.168") || serverUrl.contains("127.0.0.1")

    val label = when {
        serverUrl.contains("127.0.0.1") -> "⚡ USB Directo"
        serverUrl.contains("10.0.2.2") -> "⚡ Auto (Emulador)"
        serverUrl.contains("192.168") -> "⚡ Auto (Wi-Fi Local)"
        isCloud -> "☁️ Nube (Render)"
        else -> "🌐 Automático"
    }

    val containerColor = when {
        isCloud -> MaterialTheme.colorScheme.secondaryContainer
        isLocal -> MaterialTheme.colorScheme.tertiaryContainer
        else -> MaterialTheme.colorScheme.surfaceVariant
    }

    val icon = when {
        isCloud -> Icons.Filled.Cloud
        isLocal -> Icons.Filled.Wifi
        else -> Icons.Filled.CloudOff
    }

    AssistChip(
        onClick = onClick,
        label = { Text(label, style = MaterialTheme.typography.labelSmall) },
        leadingIcon = {
            Icon(icon, contentDescription = null, modifier = Modifier.size(14.dp))
        },
        colors = AssistChipDefaults.assistChipColors(containerColor = containerColor),
        modifier = modifier
    )
}
