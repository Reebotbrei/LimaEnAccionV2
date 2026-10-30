package com.limaenaccion.feature_auth.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.gson.Gson
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.network.ApiService
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.network.model.ApiErrorBody
import com.limaenaccion.core.data.network.model.ResetPasswordRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import java.io.IOException

data class NewPasswordUiState(
    val newPassword: String = "",
    val confirmPassword: String = "",
    val isNewPasswordVisible: Boolean = false,
    val isConfirmPasswordVisible: Boolean = false,
    val newPasswordError: String? = null,
    val confirmPasswordError: String? = null,
    val generalError: String? = null,
    val isSaving: Boolean = false,
    val passwordSaved: Boolean = false
)

class NewPasswordViewModel @JvmOverloads constructor(
    application: Application,
    private val apiServiceProvider: () -> ApiService = { RetrofitClient.instance }
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(NewPasswordUiState())
    val uiState: StateFlow<NewPasswordUiState> = _uiState.asStateFlow()
    private val gson = Gson()
    private val usuarioDao = AppDatabase.getInstance(application).usuarioDao()

    private var targetEmail: String = ""
    private var verificationCode: String = ""

    fun initCredentials(email: String, code: String) {
        targetEmail = email
        verificationCode = code
    }

    fun onNewPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(newPassword = value, newPasswordError = null, generalError = null)
    }

    fun onConfirmPasswordChange(value: String) {
        _uiState.value = _uiState.value.copy(confirmPassword = value, confirmPasswordError = null, generalError = null)
    }

    fun onToggleNewPasswordVisibility() {
        _uiState.value = _uiState.value.copy(isNewPasswordVisible = !_uiState.value.isNewPasswordVisible)
    }

    fun onToggleConfirmPasswordVisibility() {
        _uiState.value = _uiState.value.copy(isConfirmPasswordVisible = !_uiState.value.isConfirmPasswordVisible)
    }

    fun onSaveClicked() {
        val state = _uiState.value
        val newPasswordError = if (state.newPassword.length < 6) "Mínimo 6 caracteres" else null
        val confirmPasswordError = when {
            state.confirmPassword.isBlank() -> "Confirma tu contraseña"
            state.confirmPassword != state.newPassword -> "Las contraseñas no coinciden"
            else -> null
        }
        if (newPasswordError != null || confirmPasswordError != null) {
            _uiState.value = state.copy(newPasswordError = newPasswordError, confirmPasswordError = confirmPasswordError)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isSaving = true, generalError = null)
            try {
                val request = ResetPasswordRequest(
                    email = targetEmail,
                    code = verificationCode,
                    newPassword = state.newPassword
                )
                apiServiceProvider().resetPassword(request)

                // Actualizar en SQLite Room local
                withContext(Dispatchers.IO) {
                    try {
                        usuarioDao.actualizarContrasena(targetEmail, state.newPassword)
                    } catch (_: Exception) {}
                }

                _uiState.value = _uiState.value.copy(isSaving = false, passwordSaved = true)
            } catch (e: HttpException) {
                val errorMsg = parseErrorMessage(e) ?: "No se pudo actualizar la contraseña. Verifica el código."
                _uiState.value = _uiState.value.copy(isSaving = false, generalError = errorMsg)
            } catch (e: IOException) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    generalError = "Sin conexión al servidor. Verifica tu internet."
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isSaving = false,
                    generalError = e.message ?: "Error inesperado al restablecer la contraseña."
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
