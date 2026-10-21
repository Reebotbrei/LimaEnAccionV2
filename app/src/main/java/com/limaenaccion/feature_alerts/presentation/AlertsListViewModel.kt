package com.limaenaccion.feature_alerts.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.preferences.PreferencesRepository
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import com.limaenaccion.core.util.UiState
import com.limaenaccion.feature_alerts.data.model.Incident
import com.limaenaccion.feature_alerts.data.repository.IncidentRepository
import com.limaenaccion.feature_alerts.data.repository.IncidentRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

class AlertsListViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: IncidentRepository = IncidentRepositoryImpl(application),
    private val preferencesRepository: PreferencesRepository = PreferencesRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<UiState<List<Incident>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Incident>>> = _uiState.asStateFlow()

    init { loadIncidents() }

    fun loadIncidents() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                val allIncidents = repository.getIncidents()
                val radius = preferencesRepository.alertRadiusMeters.first()
                val allowedTypes = preferencesRepository.incidentTypesFilter.first()

                // Pref #8 (Radio de distancia) y Pref #9 (Filtro por Tipo de Incidente)
                val filtered = allIncidents.filter { incident ->
                    incident.distanceMeters <= radius &&
                        (allowedTypes.isEmpty() || allowedTypes.contains(incident.type.name))
                }

                _uiState.value = UiState.Success(filtered)
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "No se pudieron cargar las alertas")
            }
        }
    }
}
