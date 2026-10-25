package com.limaenaccion.feature_profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class EditPersonalDataUiState(
    val fullName: String = "",
    val dni: String = "",
    val gender: String = "Masculino",
    val email: String = "",
    val phoneNumber: String = "",
    val savedChanges: Boolean = false
)

class EditPersonalDataViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditPersonalDataUiState())
    val uiState: StateFlow<EditPersonalDataUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            SessionManager.currentUser.collect { user ->
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        fullName = user.nombreCompleto,
                        dni = user.dni,
                        gender = user.genero.ifBlank { "Masculino" },
                        email = user.correo,
                        phoneNumber = if (user.telefono.isNotBlank() && !user.telefono.startsWith("+51")) {
                            "+51 ${user.telefono}"
                        } else {
                            user.telefono
                        }
                    )
                }
            }
        }
    }

    fun onFullNameChange(v: String) { _uiState.value = _uiState.value.copy(fullName = v) }
    fun onGenderChange(v: String) { _uiState.value = _uiState.value.copy(gender = v) }
    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v) }
    fun onPhoneNumberChange(v: String) { _uiState.value = _uiState.value.copy(phoneNumber = v) }

    fun onSaveClicked() {
        val currentUser = SessionManager.currentUser.value
        if (currentUser != null) {
            val phoneClean = _uiState.value.phoneNumber.replace("+51", "").trim()
            val updatedUser = currentUser.copy(
                nombreCompleto = _uiState.value.fullName.trim(),
                genero = _uiState.value.gender,
                correo = _uiState.value.email.trim(),
                telefono = phoneClean
            )
            SessionManager.setCurrentUser(updatedUser)
        }
        _uiState.value = _uiState.value.copy(savedChanges = true)
    }
}
