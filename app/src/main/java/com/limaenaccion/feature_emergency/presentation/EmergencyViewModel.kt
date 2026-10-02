package com.limaenaccion.feature_emergency.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.util.UiState
import com.limaenaccion.feature_emergency.data.model.EmergencyContact
import com.limaenaccion.feature_emergency.data.repository.EmergencyRepository
import com.limaenaccion.feature_emergency.data.repository.MockEmergencyRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class EmergencyViewModel(
    private val repository: EmergencyRepository = MockEmergencyRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<EmergencyContact>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<EmergencyContact>>> = _uiState.asStateFlow()

    init {
        loadEmergencyDirectory()
    }

    fun loadEmergencyDirectory() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val contacts = repository.getEmergencyDirectory()
                _uiState.value = UiState.Success(contacts)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(
                    e.message ?: "No se pudo cargar el directorio de emergencia"
                )
            }
        }
    }
}