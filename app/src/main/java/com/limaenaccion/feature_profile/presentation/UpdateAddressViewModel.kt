package com.limaenaccion.feature_profile.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class UpdateAddressUiState(
    val currentZone: String = "Lima, Perú",
    val newDistrict: String = "San Juan de Lurigancho",
    val newNeighborhood: String = "Caja de Agua",
    val isPhoneVerified: Boolean = false,
    val changesSaved: Boolean = false
)

class UpdateAddressViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UpdateAddressUiState())
    val uiState: StateFlow<UpdateAddressUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            SessionManager.currentUser.collect { user ->
                if (user != null) {
                    val zone = listOf(user.urbanizacion, user.distrito).filter { it.isNotBlank() }.joinToString(", ")
                    _uiState.value = _uiState.value.copy(
                        currentZone = zone.ifBlank { "Lima, Perú" },
                        newDistrict = user.distrito.ifBlank { "San Juan de Lurigancho" },
                        newNeighborhood = user.urbanizacion.ifBlank { "Caja de Agua" }
                    )
                }
            }
        }
    }

    fun onDistrictChange(value: String) { _uiState.value = _uiState.value.copy(newDistrict = value, isPhoneVerified = false) }
    fun onNeighborhoodChange(value: String) { _uiState.value = _uiState.value.copy(newNeighborhood = value, isPhoneVerified = false) }

    fun onVerifyWithSmsClicked() {
        // Se simula la verificación inmediata.
        _uiState.value = _uiState.value.copy(isPhoneVerified = true)
    }

    fun onSaveClicked() {
        if (_uiState.value.isPhoneVerified) {
            val currentUser = SessionManager.currentUser.value
            if (currentUser != null) {
                val updatedUser = currentUser.copy(
                    distrito = _uiState.value.newDistrict,
                    urbanizacion = _uiState.value.newNeighborhood
                )
                SessionManager.setCurrentUser(updatedUser)
            }
            _uiState.value = _uiState.value.copy(changesSaved = true)
        }
    }
}
