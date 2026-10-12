package com.limaenaccion.feature_settings.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.preferences.PreferencesRepository
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

data class SettingsUiState(
    val pushNotificationsEnabled: Boolean = true,
    val sosSoundEnabled: Boolean = true,
    val registeredZone: String = "Caja de Agua, SJL",
    val appVersion: String = "v2.1.4",
    val showLogoutDialog: Boolean = false
)

class SettingsViewModel @JvmOverloads constructor(
    application: Application,
    private val preferencesRepository: PreferencesRepository = PreferencesRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            val notifications = preferencesRepository.notificationsActive.first()
            val sosSound = preferencesRepository.overrideSilentModeForSos.first()
            _uiState.value = _uiState.value.copy(
                pushNotificationsEnabled = notifications,
                sosSoundEnabled = sosSound
            )
        }
    }

    fun onTogglePushNotifications(value: Boolean) {
        _uiState.value = _uiState.value.copy(pushNotificationsEnabled = value)
        viewModelScope.launch { preferencesRepository.setNotificationsActive(value) }
    }

    fun onToggleSosSound(value: Boolean) {
        _uiState.value = _uiState.value.copy(sosSoundEnabled = value)
        viewModelScope.launch { preferencesRepository.setOverrideSilentModeForSos(value) }
    }

    fun onLogoutClicked() { _uiState.value = _uiState.value.copy(showLogoutDialog = true) }
    fun onDismissLogoutDialog() { _uiState.value = _uiState.value.copy(showLogoutDialog = false) }
}