package com.limaenaccion.feature_auth.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val showCodeDialog: Boolean = false,
    val codeDigits: List<String> = List(4) { "" },
    val codeError: String? = null,
    val codeVerified: Boolean = false
)

class ForgotPasswordViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = null)
    }

    fun onSendCodeClicked() {
        val email = _uiState.value.email
        val error = when {
            email.isBlank() -> "Ingresa tu correo"
            !EMAIL_REGEX.matches(email) -> "Correo inválido"
            else -> null
        }
        if (error != null) {
            _uiState.value = _uiState.value.copy(emailError = error)
            return
        }
        // Etapa 1: sin backend — se simula el envío del código y se abre el modal.
        _uiState.value = _uiState.value.copy(
            showCodeDialog = true,
            codeDigits = List(4) { "" },
            codeError = null
        )
    }

    fun onDigitChange(index: Int, digit: String) {
        val updated = _uiState.value.codeDigits.toMutableList()
        updated[index] = digit
        _uiState.value = _uiState.value.copy(codeDigits = updated, codeError = null)
    }

    fun onResendCodeClicked() {
        _uiState.value = _uiState.value.copy(codeDigits = List(4) { "" }, codeError = null)
    }

    fun onDismissCodeDialog() {
        _uiState.value = _uiState.value.copy(showCodeDialog = false, codeDigits = List(4) { "" }, codeError = null)
    }

    fun onVerifyCodeClicked() {
        val code = _uiState.value.codeDigits.joinToString("")
        if (code.length < 4) {
            _uiState.value = _uiState.value.copy(codeError = "Ingresa el código completo")
            return
        }
        // Etapa 1: cualquier código de 4 dígitos se acepta (sin backend todavía).
        _uiState.value = _uiState.value.copy(codeVerified = true, showCodeDialog = false)
    }
}