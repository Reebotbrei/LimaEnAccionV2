package com.limaenaccion.feature_profile.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.preferences.PrimaryContactPrefs
import com.limaenaccion.core.data.preferences.PreferencesRepository
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import com.limaenaccion.feature_profile.data.model.ContactRelationship
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class AddAuxiliaryContactUiState(
    val fullName: String = "",
    val relationship: ContactRelationship = ContactRelationship.FAMILIAR,
    val phoneNumber: String = "",
    val isPrimaryContact: Boolean = true,
    val contactSaved: Boolean = false
) {
    // Validación mínima para no dejar campos vacíos
    val isFormValid: Boolean
        get() = fullName.isNotBlank() && phoneNumber.length >= 6
}

class AddAuxiliaryContactViewModel @JvmOverloads constructor(
    application: Application,
    private val preferencesRepository: PreferencesRepository = PreferencesRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AddAuxiliaryContactUiState())
    val uiState: StateFlow<AddAuxiliaryContactUiState> = _uiState.asStateFlow()

    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value)
    }

    fun onRelationshipChange(value: ContactRelationship) {
        _uiState.value = _uiState.value.copy(relationship = value)
    }

    fun onPhoneNumberChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(9) // celular 9 dígitos
        _uiState.value = _uiState.value.copy(phoneNumber = digitsOnly)
    }

    fun onIsPrimaryContactChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(isPrimaryContact = value)
    }

    fun onSaveClicked() {
        val state = _uiState.value
        if (!state.isFormValid) return

        viewModelScope.launch {
            // Si el usuario marcó este contacto como principal, se persiste en DataStore (Pref #14)
            if (state.isPrimaryContact) {
                preferencesRepository.setPrimaryAuxiliaryContact(
                    PrimaryContactPrefs(
                        name = state.fullName,
                        phoneNumber = state.phoneNumber
                    )
                )
            }
            _uiState.value = state.copy(contactSaved = true)
        }
    }
}