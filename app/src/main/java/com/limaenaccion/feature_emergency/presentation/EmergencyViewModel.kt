package com.limaenaccion.feature_emergency.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.preferences.PreferencesRepository
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import com.limaenaccion.core.util.UiState
import com.limaenaccion.feature_emergency.data.model.EmergencyContact
import com.limaenaccion.feature_emergency.data.model.EmergencyContactType
import com.limaenaccion.feature_emergency.data.repository.EmergencyRepository
import com.limaenaccion.feature_emergency.data.repository.MockEmergencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class EmergencyViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: EmergencyRepository = MockEmergencyRepository(),
    private val preferencesRepository: PreferencesRepository = PreferencesRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<UiState<List<EmergencyContact>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<EmergencyContact>>> = _uiState.asStateFlow()

    init {
        loadEmergencyDirectory()
    }

    fun loadEmergencyDirectory() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val institutionalContacts = repository.getEmergencyDirectory()

                // Leer el contacto principal guardado en DataStore (Pref #14)
                val primaryContact = preferencesRepository.primaryAuxiliaryContact.first()

                val fullList = buildList {
                    // RN-03: El contacto de auxilio principal va primero en la lista
                    if (primaryContact != null) {
                        add(
                            EmergencyContact(
                                id = "primary_auxiliary",
                                name = primaryContact.name,
                                phoneNumber = primaryContact.phoneNumber,
                                type = EmergencyContactType.CONTACTO_PRINCIPAL,
                                isCustom = true
                            )
                        )
                    }
                    addAll(institutionalContacts)
                }

                _uiState.value = UiState.Success(fullList)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "No se pudo cargar el directorio de emergencia"
                )
            }
        }
    }
}