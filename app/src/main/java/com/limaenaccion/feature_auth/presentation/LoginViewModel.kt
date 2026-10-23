package com.limaenaccion.feature_auth.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.network.model.LoginRequest
import kotlinx.coroutines.launch

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

data class LoginUiState(
    val email: String = "",
    val password: String = "",
    val isPasswordVisible: Boolean = false,
    val emailError: String? = null,
    val passwordError: String? = null,
    val loginSuccess: Boolean = false,
    val isLoading: Boolean = false
)

class LoginViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LoginUiState())
    val uiState: StateFlow<LoginUiState> = _uiState.asStateFlow()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value, emailError = null)
    }

    fun onPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(password = value, passwordError = null)
    }

    fun onTogglePasswordVisibility() {
        _uiState.value = _uiState.value.copy(isPasswordVisible = !_uiState.value.isPasswordVisible)
    }

    fun onLoginClicked() {
        val state = _uiState.value
        val emailError = when {
            state.email.isBlank() -> "Ingresa tu correo"
            !EMAIL_REGEX.matches(state.email) -> "Correo inválido"
            else -> null
        }
        val passwordError = if (state.password.isBlank()) "Ingresa tu contraseña" else null

        if (emailError != null || passwordError != null) {
            _uiState.value = state.copy(emailError = emailError, passwordError = passwordError)
            return
        }

        // Consumiendo API Service real para Inicio de Sesión
        viewModelScope.launch {
            _uiState.value = state.copy(isLoading = true)
            try {
                val response = RetrofitClient.instance.login(
                    LoginRequest(
                        email = state.email.trim(),
                        password = state.password
                    )
                )

                if (response.status == "success" && response.data != null) {
                    _uiState.value = state.copy(
                        isLoading = false,
                        loginSuccess = true
                    )
                } else {
                    _uiState.value = state.copy(
                        isLoading = false,
                        passwordError = response.message
                    )
                }
            } catch (e: Exception) {
                _uiState.value = state.copy(
                    isLoading = false,
                    passwordError = "Error al conectar con el servidor: ${e.localizedMessage ?: "Verifica tu conexión"}"
                )
            }
        }
    }

}