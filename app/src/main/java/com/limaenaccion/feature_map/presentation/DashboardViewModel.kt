package com.limaenaccion.feature_map.presentation

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

enum class DashboardViewMode { MAP, LIST }

data class DashboardUiState(
    val incidentsState: UiState<List<Incident>> = UiState.Loading,
    val viewMode: DashboardViewMode = DashboardViewMode.MAP,
    val selectedIncidentId: String? = null
)

class DashboardViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: IncidentRepository = IncidentRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(DashboardUiState())
    val uiState: StateFlow<DashboardUiState> = _uiState.asStateFlow()

    init {
        loadIncidents()
        observeRealtimeIncidents()
    }

    private fun observeRealtimeIncidents() {
        viewModelScope.launch {
            repository.observeIncidents().collect { incidentList ->
                _uiState.value = _uiState.value.copy(
                    incidentsState = UiState.Success(incidentList)
                )
            }
        }
    }

    fun loadIncidents() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(incidentsState = UiState.Loading)
            try {
                _uiState.value = _uiState.value.copy(incidentsState = UiState.Success(repository.getIncidents()))
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(incidentsState = UiState.Error(e.message ?: "No se pudieron cargar los incidentes"))
            }
        }
    }

    fun onViewModeChange(mode: DashboardViewMode) {
        _uiState.value = _uiState.value.copy(viewMode = mode)
    }

    fun onPinClick(incidentId: String) {
        _uiState.value = _uiState.value.copy(selectedIncidentId = incidentId)
    }

    fun onDismissBottomSheet() {
        _uiState.value = _uiState.value.copy(selectedIncidentId = null)
    }
}