package com.limaenaccion.feature_profile.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class EditPersonalDataUiState(
    val fullName: String = "Breider David Catashunga Peña",
    val dni: String = "74881168",
    val gender: String = "Masculino",
    val email: String = "brei@gmail.com",
    val phoneNumber: String = "+51 915 375 393",
    val savedChanges: Boolean = false
)

class EditPersonalDataViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(EditPersonalDataUiState())
    val uiState: StateFlow<EditPersonalDataUiState> = _uiState.asStateFlow()

    fun onFullNameChange(v: String) { _uiState.value = _uiState.value.copy(fullName = v) }
    fun onGenderChange(v: String) { _uiState.value = _uiState.value.copy(gender = v) }
    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v) }
    fun onPhoneNumberChange(v: String) { _uiState.value = _uiState.value.copy(phoneNumber = v) }

    fun onSaveClicked() {
        // Solo confirma visualmente el guardado.
        _uiState.value = _uiState.value.copy(savedChanges = true)
    }
}