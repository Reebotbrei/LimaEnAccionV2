package com.limaenaccion.feature_settings.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ContactPhone
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.ui.components.DangerButton
import com.limaenaccion.core.ui.components.MenuRow
import com.limaenaccion.core.ui.components.ServerConfigCard
import com.limaenaccion.core.ui.components.ServerStatusChip
import com.limaenaccion.core.ui.components.SettingSegmentedRow
import com.limaenaccion.core.ui.components.SettingSliderRow
import com.limaenaccion.core.ui.components.SettingSwitchRow
import com.limaenaccion.core.util.LocalAppStrings

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun SettingsScreen(
    onLogoutConfirmed: () -> Unit,
    onManageContacts: () -> Unit = {},
    viewModel: SettingsViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(scrollState)
            .padding(16.dp)
    ) {
        Text(strings.settingsTitle, style = MaterialTheme.typography.titleLarge, fontWeight = FontWeight.Bold)
        Spacer(modifier = Modifier.height(16.dp))

        // Mensaje de caché liberada si aplica (Pref #11)
        if (uiState.cacheClearedMessage != null) {
            Surface(
                color = MaterialTheme.colorScheme.primaryContainer,
                shape = RoundedCornerShape(16.dp),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth().padding(12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        uiState.cacheClearedMessage ?: "",
                        style = MaterialTheme.typography.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = viewModel::onDismissCacheMessage) {
                        Text("OK")
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
        }

        // ==========================================
        // 1. APARIENCIA Y ACCESIBILIDAD (Pref #1, #2, #3, #4)
        // ==========================================
        SectionHeader(strings.sectionAppearance)

        // Pref #1: Tema (Claro, Oscuro, Daltónico, Sistema)
        SettingSegmentedRow(
            title = strings.appTheme,
            options = listOf("Sistema", "Claro", "Oscuro", "Daltonico"),
            selectedOption = uiState.theme,
            onOptionSelected = viewModel::onThemeChange,
            labelProvider = {
                when (it) {
                    "Sistema" -> strings.themeSystem
                    "Claro" -> strings.themeLight
                    "Oscuro" -> strings.themeDark
                    "Daltonico" -> strings.themeColorblind
                    else -> it
                }
            }
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Pref #2: Idioma
        SettingSegmentedRow(
            title = strings.interfaceLanguage,
            options = listOf("ES", "EN"),
            selectedOption = uiState.language,
            onOptionSelected = viewModel::onLanguageChange,
            labelProvider = { if (it == "ES") strings.langSpanish else strings.langEnglish }
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Pref #3: Tamaño de Fuente (Slider de 12sp a 30sp)
        SettingSliderRow(
            title = strings.fontSize,
            valueLabel = "${uiState.fontSizeSp.toInt()} sp",
            value = uiState.fontSizeSp,
            valueRange = 12f..30f,
            steps = 17,
            onValueChange = viewModel::onFontSizeChange
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Pref #4: Omitir Pantalla de Carga
        SettingSwitchRow(
            title = strings.skipWelcome,
            subtitle = strings.skipWelcomeDesc,
            checked = uiState.skipLoadingScreen,
            onCheckedChange = viewModel::onToggleSkipLoadingScreen
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 2. ALERTAS Y NOTIFICACIONES (Pref #7, #8, #9, #10)
        // ==========================================
        SectionHeader(strings.sectionAlerts)

        // Pref #7: Notificaciones push maestras
        SettingSwitchRow(
            title = strings.pushNotifications,
            subtitle = strings.pushNotificationsDesc,
            checked = uiState.pushNotificationsEnabled,
            onCheckedChange = viewModel::onTogglePushNotifications
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Pref #8: Radio de Alertas (500m, 1km, 3km)
        SettingSegmentedRow(
            title = strings.alertRadius,
            options = listOf(500, 1000, 3000),
            selectedOption = uiState.alertRadiusMeters,
            onOptionSelected = viewModel::onAlertRadiusChange,
            labelProvider = { if (it < 1000) "${it}m" else "${it / 1000}km" }
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Pref #9: Filtro por Incidente (Casillas de verificación)
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(modifier = Modifier.fillMaxWidth().padding(14.dp)) {
                Text(strings.incidentFilterTitle, style = MaterialTheme.typography.bodyLarge)
                Text(
                    strings.incidentFilterDesc,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(10.dp))
                val incidentTypes = listOf(
                    "ROBO" to strings.incidentRobbery,
                    "SOSPECHOSO" to strings.incidentSuspicious,
                    "EMERGENCIA_MEDICA" to strings.incidentMedical,
                    "INCENDIO" to strings.incidentFire,
                    "ACCIDENTE" to strings.incidentAccident,
                    "VANDALISMO" to strings.incidentVandalism
                )
                FlowRow(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    incidentTypes.forEach { (typeKey, label) ->
                        val selected = uiState.incidentTypesFilter.contains(typeKey)
                        FilterChip(
                            selected = selected,
                            onClick = { viewModel.onToggleIncidentType(typeKey) },
                            label = { Text(label, style = MaterialTheme.typography.labelSmall) }
                        )
                    }
                }
            }
        }
        Spacer(modifier = Modifier.height(10.dp))

        // Pref #10: Modo "No Molestar" (Horario programado)
        SettingSwitchRow(
            title = strings.dndTitle,
            subtitle = if (uiState.doNotDisturb.enabled) {
                strings.dndActiveSchedule(uiState.doNotDisturb.startHour, uiState.doNotDisturb.endHour)
            } else {
                strings.dndDesc
            },
            checked = uiState.doNotDisturb.enabled,
            onCheckedChange = viewModel::onToggleDoNotDisturb
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 3. SEGURIDAD Y SOS (Pref #6, #13, #14, #5)
        // ==========================================
        SectionHeader(strings.sectionSecurity)

        // Pref #6: Visibilidad del Teléfono
        SettingSwitchRow(
            title = strings.phoneVisibility,
            subtitle = strings.phoneVisibilityDesc,
            checked = uiState.phoneVisible,
            onCheckedChange = viewModel::onTogglePhoneVisible
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Pref #13: Anular Modo Silencioso para SOS
        SettingSwitchRow(
            title = strings.overrideSilentSos,
            subtitle = strings.overrideSilentSosDesc,
            checked = uiState.overrideSilentModeForSos,
            onCheckedChange = viewModel::onToggleOverrideSilentMode
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Pref #14 & #12: Contacto de auxilio principal y directorio
        MenuRow(
            icon = Icons.Filled.ContactPhone,
            title = strings.primaryContactLabel,
            subtitle = uiState.primaryContactName ?: strings.primaryContactNone,
            onClick = onManageContacts
        )
        Spacer(modifier = Modifier.height(10.dp))

        // Pref #5: Permiso de Ubicación GPS
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
            shadowElevation = 1.dp,
            modifier = Modifier.fillMaxWidth()
        ) {
            Row(
                modifier = Modifier.fillMaxWidth().padding(14.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(strings.locationPermission, style = MaterialTheme.typography.bodyLarge)
                    Text(strings.locationDesc, style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                }
                Text(
                    if (uiState.locationPermissionGranted) strings.locationGranted else strings.locationPending,
                    color = if (uiState.locationPermissionGranted) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.bodyMedium
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 4. ALMACENAMIENTO Y CACHÉ (Pref #11)
        // ==========================================
        SectionHeader(strings.sectionStorage)

        MenuRow(
            icon = Icons.Filled.CleaningServices,
            title = strings.clearCacheTitle,
            subtitle = strings.clearCacheDesc,
            onClick = viewModel::onShowClearCacheDialog
        )

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 5. CONEXIÓN AL SERVIDOR BACKEND
        // ==========================================
        SectionHeader("Servidor Backend")
        ServerStatusChip(
            serverUrl = uiState.serverUrl,
            onClick = viewModel::onShowServerConfig
        )
        Spacer(modifier = Modifier.height(8.dp))
        if (uiState.showServerConfig) {
            ServerConfigCard(
                currentUrl = uiState.serverUrl,
                onUrlConfirmed = viewModel::onServerUrlChanged,
                onDismiss = viewModel::onDismissServerConfig
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        Spacer(modifier = Modifier.height(24.dp))

        // ==========================================
        // 6. CUENTA Y DATOS PERSONALES (Ley N° 29733) (Pref #16)
        // ==========================================
        SectionHeader(strings.sectionAccount)

        InfoRow(strings.registeredZoneLabel, uiState.registeredZone)
        InfoRow(strings.appVersionLabel, uiState.appVersion)

        Spacer(modifier = Modifier.height(12.dp))
        DangerButton(
            text = strings.logoutBtn,
            onClick = viewModel::onLogoutClicked
        )

        Spacer(modifier = Modifier.height(12.dp))

        // Pref #16: Eliminación de Cuenta Ley N° 29733
        DangerButton(
            text = strings.deleteAccountBtn,
            onClick = viewModel::onDeleteAccountClicked,
            containerColor = Color(0xFFFDE8E8),
            contentColor = Color(0xFF991B1B)
        )

        Spacer(modifier = Modifier.height(24.dp))
    }

    // DIÁLOGOS DE CONFIRMACIÓN

    // Diálogo Cerrar Sesión
    if (uiState.showLogoutDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissLogoutDialog,
            title = { Text(strings.logoutDialogTitle) },
            text = { Text(strings.logoutDialogText) },
            confirmButton = {
                TextButton(onClick = { viewModel.onDismissLogoutDialog(); onLogoutConfirmed() }) {
                    Text(strings.logoutConfirmBtn, color = Color(0xFFDC2626), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = viewModel::onDismissLogoutDialog) { Text(strings.cancelBtn) } }
        )
    }

    // Diálogo Borrar Caché (Pref #11)
    if (uiState.showClearCacheDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissClearCacheDialog,
            title = { Text(strings.clearCacheDialogTitle) },
            text = { Text(strings.clearCacheDialogText) },
            confirmButton = {
                TextButton(onClick = viewModel::onConfirmClearCache) {
                    Text(strings.clearCacheBtn, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = viewModel::onDismissClearCacheDialog) { Text(strings.cancelBtn) } }
        )
    }

    // Diálogo Eliminación de Cuenta - Ley N° 29733 (Pref #16)
    if (uiState.showDeleteAccountDialog) {
        AlertDialog(
            onDismissRequest = viewModel::onDismissDeleteAccountDialog,
            title = { Text(strings.deleteDialogTitle, color = Color(0xFF991B1B), fontWeight = FontWeight.Bold) },
            text = { Text(strings.deleteDialogText) },
            confirmButton = {
                TextButton(onClick = { viewModel.onConfirmDeleteAccount(onLogoutConfirmed) }) {
                    Text(strings.deleteConfirmBtn, color = Color(0xFF991B1B), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = { TextButton(onClick = viewModel::onDismissDeleteAccountDialog) { Text(strings.cancelBtn) } }
        )
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.labelSmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant,
        fontWeight = FontWeight.Bold
    )
    Spacer(modifier = Modifier.height(8.dp))
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(label, color = MaterialTheme.colorScheme.onSurfaceVariant)
        Text(value, fontWeight = FontWeight.Bold)
    }
}
