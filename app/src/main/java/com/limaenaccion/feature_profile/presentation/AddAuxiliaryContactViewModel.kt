package com.limaenaccion.feature_profile.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.dao.ContactoDao
import com.limaenaccion.core.data.local.entities.ContactoEntity
import com.limaenaccion.feature_profile.data.model.ContactRelationship
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AddAuxiliaryContactUiState(
    val fullName: String = "",
    val relationship: ContactRelationship = ContactRelationship.FAMILIAR,
    val phoneNumber: String = "",
    val isPrimaryContact: Boolean = true,
    val contactSaved: Boolean = false,
    val maxContactsReached: Boolean = false,
    val errorMessage: String? = null
) {
    // Validación mínima para no dejar campos vacíos
    val isFormValid: Boolean
        get() = fullName.isNotBlank() && phoneNumber.length >= 6
}

class AddAuxiliaryContactViewModel @JvmOverloads constructor(
    application: Application,
    private val contactoDao: ContactoDao = AppDatabase.getInstance(application).contactoDao()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AddAuxiliaryContactUiState())
    val uiState: StateFlow<AddAuxiliaryContactUiState> = _uiState.asStateFlow()

    init {
        checkContactLimit()
    }

    private fun checkContactLimit() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = contactoDao.contarContactos()
            if (count >= 3) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        maxContactsReached = true,
                        errorMessage = "Has alcanzado el límite de 3 contactos de confianza (Pref #12)."
                    )
                }
            }
        }
    }

    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value, errorMessage = null)
    }

    fun onRelationshipChange(value: ContactRelationship) {
        _uiState.value = _uiState.value.copy(relationship = value)
    }

    fun onPhoneNumberChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(9) // celular 9 dígitos
        _uiState.value = _uiState.value.copy(phoneNumber = digitsOnly, errorMessage = null)
    }

    fun onIsPrimaryContactChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(isPrimaryContact = value)
    }

    fun onSaveClicked() {
        val state = _uiState.value
        if (!state.isFormValid || state.maxContactsReached) return

        viewModelScope.launch(Dispatchers.IO) {
            val count = contactoDao.contarContactos()
            if (count >= 3) {
                withContext(Dispatchers.Main) {
                    _uiState.value = state.copy(
                        maxContactsReached = true,
                        errorMessage = "Límite alcanzado: máximo 3 contactos de auxilio."
                    )
                }
                return@launch
            }

            val entity = ContactoEntity(
                nombre = state.fullName.trim(),
                telefono = state.phoneNumber.trim(),
                parentesco = state.relationship.label,
                esPrincipal = state.isPrimaryContact
            )

            // Persistencia local en Room (SQLite)
            if (state.isPrimaryContact) {
                contactoDao.desmarcarPrincipales()
            }
            contactoDao.guardarContacto(entity)

            withContext(Dispatchers.Main) {
                _uiState.value = state.copy(contactSaved = true)
            }
        }
    }
}
