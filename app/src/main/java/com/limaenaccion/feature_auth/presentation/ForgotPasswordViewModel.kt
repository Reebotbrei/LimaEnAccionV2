package com.limaenaccion.feature_auth.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.limaenaccion.core.data.network.ApiService
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.network.model.ApiErrorBody
import com.limaenaccion.core.data.network.model.ForgotPasswordRequest
import com.limaenaccion.core.data.network.model.VerifyResetCodeRequest
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import retrofit2.HttpException
import java.io.IOException

private val EMAIL_REGEX = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")

data class ForgotPasswordUiState(
    val email: String = "",
    val emailError: String? = null,
    val isLoading: Boolean = false,
    val showCodeDialog: Boolean = false,
    val codeDigits: List<String> = List(6) { "" },
    val isVerifyingCode: Boolean = false,
    val codeError: String? = null,
    val codeVerified: Boolean = false,
    val verifiedCode: String = ""
)

class ForgotPasswordViewModel @JvmOverloads constructor(
    application: Application,
    private val apiServiceProvider: () -> ApiService = { RetrofitClient.instance }
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ForgotPasswordUiState())
    val uiState: StateFlow<ForgotPasswordUiState> = _uiState.asStateFlow()
    private val gson = Gson()

    fun onEmailChange(value: String) {
        _uiState.value = _uiState.value.copy(email = value.trim(), emailError = null)
    }

    fun onSendCodeClicked() {
        val email = _uiState.value.email.trim().lowercase()
        val error = when {
            email.isBlank() -> "Ingresa tu correo electrónico registrado"
            !EMAIL_REGEX.matches(email) -> "Formato de correo inválido"
            else -> null
        }
        if (error != null) {
            _uiState.value = _uiState.value.copy(emailError = error)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, emailError = null)
            try {
                apiServiceProvider().forgotPassword(ForgotPasswordRequest(email))
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    showCodeDialog = true,
                    codeDigits = List(6) { "" },
                    codeError = null
                )
            } catch (e: HttpException) {
                val errorMsg = parseErrorMessage(e) ?: "No encontramos ninguna cuenta vecinal con este correo."
                _uiState.value = _uiState.value.copy(isLoading = false, emailError = errorMsg)
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    emailError = "No se pudo conectar al servidor. Verifica tu conexión a internet."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    emailError = e.message ?: "Ocurrió un error inesperado al enviar el código."
                )
            }
        }
    }

    fun onDigitChange(index: Int, digit: String) {
        val updated = _uiState.value.codeDigits.toMutableList()
        updated[index] = digit
        _uiState.value = _uiState.value.copy(codeDigits = updated, codeError = null)
    }

    fun onResendCodeClicked() {
        val email = _uiState.value.email.trim().lowercase()
        viewModelScope.launch {
            try {
                apiServiceProvider().forgotPassword(ForgotPasswordRequest(email))
                _uiState.value = _uiState.value.copy(
                    codeDigits = List(6) { "" },
                    codeError = "Nuevo código enviado a tu correo"
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(codeError = "Error al reenviar código.")
            }
        }
    }

    fun onDismissCodeDialog() {
        _uiState.value = _uiState.value.copy(
            showCodeDialog = false,
            codeDigits = List(6) { "" },
            codeError = null
        )
    }

    fun onVerifyCodeClicked() {
        val code = _uiState.value.codeDigits.joinToString("")
        if (code.length < 6) {
            _uiState.value = _uiState.value.copy(codeError = "Ingresa los 6 dígitos del código recibido")
            return
        }

        val email = _uiState.value.email.trim().lowercase()
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isVerifyingCode = true, codeError = null)
            try {
                apiServiceProvider().verifyResetCode(VerifyResetCodeRequest(email, code))
                _uiState.value = _uiState.value.copy(
                    isVerifyingCode = false,
                    codeVerified = true,
                    verifiedCode = code,
                    showCodeDialog = false
                )
            } catch (e: HttpException) {
                val errorMsg = parseErrorMessage(e) ?: "Código incorrecto o expirado"
                _uiState.value = _uiState.value.copy(isVerifyingCode = false, codeError = errorMsg)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isVerifyingCode = false,
                    codeError = "No se pudo verificar el código. Intenta nuevamente."
                )
            }
        }
    }

    private fun parseErrorMessage(e: HttpException): String? {
        return try {
            val json = e.response()?.errorBody()?.string()
            val parsed = gson.fromJson(json, ApiErrorBody::class.java)
            parsed?.message
        } catch (_: Exception) {
            null
        }
    }
}
