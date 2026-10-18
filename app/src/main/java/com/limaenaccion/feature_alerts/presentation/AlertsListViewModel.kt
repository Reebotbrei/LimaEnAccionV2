package com.limaenaccion.feature_alerts.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.util.UiState
import com.limaenaccion.feature_alerts.data.model.Incident
import com.limaenaccion.feature_alerts.data.repository.IncidentRepository
import com.limaenaccion.feature_alerts.data.repository.IncidentRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class AlertsListViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: IncidentRepository = IncidentRepositoryImpl(application)
) : AndroidViewModel(application) {

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