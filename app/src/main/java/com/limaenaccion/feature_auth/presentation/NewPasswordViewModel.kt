package com.limaenaccion.feature_auth.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NewPasswordUiState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val passwordSaved: Boolean = false
)

class NewPasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(NewPasswordUiState())
    val uiState: StateFlow<NewPasswordUiState> = _uiState.asStateFlow()

    fun onNewPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(newPassword = value, newPasswordError = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, confirmPasswordError = null)
    }

    fun onToggleNewPasswordVisibility() {
        _uiState.value = _uiState.value.copy(isNewPasswordVisible = !_uiState.value.isNewPasswordVisible)
    }

    fun onToggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible)
    }

    fun onSaveClicked() {
        val state = _uiState.value
        val newPasswordError = if (state.newPassword.length < 8) "Mínimo 8 caracteres" else null
        val confirmPasswordError = when {
            state.confirmPassword.isBlank() -> "Confirma tu contraseña"
            state.confirmPassword != state.newPassword -> "Las contraseñas no coinciden"
            else -> null
        }
        if (newPasswordError != null || confirmPasswordError != null) {
            _uiState.value = state.copy(newPasswordError = newPasswordError, confirmPasswordError = confirmPasswordError)
            return
        }
        _uiState.value = state.copy(passwordSaved = true)
    }
}