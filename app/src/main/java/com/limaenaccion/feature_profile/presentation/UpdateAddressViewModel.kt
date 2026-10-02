package com.limaenaccion.feature_profile.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class UpdateAddressUiState(
    val currentZone: String = "Caja de Agua, San Juan de Lurigancho",
    val newDistrict: String = "San Juan de Lurigancho",
    val newNeighborhood: String = "Caja de Agua",
    val isPhoneVerified: Boolean = false,
    val changesSaved: Boolean = false
)

class UpdateAddressViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(UpdateAddressUiState())
    val uiState: StateFlow<UpdateAddressUiState> = _uiState.asStateFlow()

    fun onDistrictChange(value: String) { _uiState.value = _uiState.value.copy(newDistrict = value, isPhoneVerified = false) }
    fun onNeighborhoodChange(value: String) { _uiState.value = _uiState.value.copy(newNeighborhood = value, isPhoneVerified = false) }

    fun onVerifyWithSmsClicked() {
        // Se simula la verificación inmediata.
        _uiState.value = _uiState.value.copy(isPhoneVerified = true)
    }

    fun onSaveClicked() {
        if (_uiState.value.isPhoneVerified) _uiState.value = _uiState.value.copy(changesSaved = true)
    }
}