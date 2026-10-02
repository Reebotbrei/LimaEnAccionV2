package com.limaenaccion.feature_alerts.presentation

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

class AlertsListViewModel(
    private val repository: IncidentRepository = MockIncidentRepository()
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState<List<Incident>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Incident>>> = _uiState.asStateFlow()

    init { loadIncidents() }

    fun loadIncidents() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                _uiState.value = UiState.Success(repository.getIncidents())
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "No se pudieron cargar las alertas")
            }
        }
    }
}