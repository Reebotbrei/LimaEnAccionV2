package com.limaenaccion.feature_alerts.presentation

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.util.UiState
import com.limaenaccion.feature_alerts.data.model.Incident
import com.limaenaccion.feature_alerts.data.repository.IncidentRepository
import com.limaenaccion.feature_alerts.data.repository.MockIncidentRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * `savedStateHandle["incidentId"]` se llena automáticamente por Navigation
 * Compose a partir del argumento de ruta — no hace falta pasarlo a mano.
 */
class IncidentDetailViewModel(
    savedStateHandle: SavedStateHandle,
) : ViewModel() {

    private val repository: IncidentRepository = MockIncidentRepository()
    private val _uiState = MutableStateFlow<UiState<Incident>>(UiState.Loading)
    val uiState: StateFlow<UiState<Incident>> = _uiState.asStateFlow()

    init {
        val incidentId: String? = savedStateHandle["incidentId"]
        viewModelScope.launch {
            val incident = incidentId?.let { repository.getIncidentById(it) }
            _uiState.value = if (incident != null) UiState.Success(incident) else UiState.Error("No se encontró el incidente")
        }
    }
}