package com.limaenaccion.feature_settings.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SettingsUiState(
    val pushNotificationsEnabled: Boolean = true,
    val sosSoundEnabled: Boolean = true,
    val registeredZone: String = "Caja de Agua, SJL",
    val appVersion: String = "v2.1.4",
    val showLogoutDialog: Boolean = false
)

class SettingsViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    fun onTogglePushNotifications(value: Boolean) { _uiState.value = _uiState.value.copy(pushNotificationsEnabled = value) }
    fun onToggleSosSound(value: Boolean) { _uiState.value = _uiState.value.copy(sosSoundEnabled = value) }
    fun onLogoutClicked() { _uiState.value = _uiState.value.copy(showLogoutDialog = true) }
    fun onDismissLogoutDialog() { _uiState.value = _uiState.value.copy(showLogoutDialog = false) }
}