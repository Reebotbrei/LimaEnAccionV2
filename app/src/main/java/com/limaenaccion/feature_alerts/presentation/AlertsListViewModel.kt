package com.limaenaccion.feature_alerts.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.preferences.PreferencesRepository
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import com.limaenaccion.core.util.UiState
import com.limaenaccion.feature_alerts.data.model.Incident
import com.limaenaccion.feature_alerts.data.model.IncidentStatus
import com.limaenaccion.feature_alerts.data.repository.IncidentRepository
import com.limaenaccion.feature_alerts.data.repository.IncidentRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class AlertFeedTab {
    CERCA_DE_TI,
    RECIENTES
}

class AlertsListViewModel @JvmOverloads constructor(
    application: Application,
    private val repository: IncidentRepository = IncidentRepositoryImpl(application),
    private val preferencesRepository: PreferencesRepository = PreferencesRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<UiState<List<Incident>>>(UiState.Loading)
    val uiState: StateFlow<UiState<List<Incident>>> = _uiState.asStateFlow()

    private val _selectedTab = MutableStateFlow(AlertFeedTab.CERCA_DE_TI)
    val selectedTab: StateFlow<AlertFeedTab> = _selectedTab.asStateFlow()

    private val _witnessedIncidentIds = MutableStateFlow<Set<String>>(emptySet())
    val witnessedIncidentIds: StateFlow<Set<String>> = _witnessedIncidentIds.asStateFlow()

    private val _falsehoodReportedIds = MutableStateFlow<Set<String>>(emptySet())
    val falsehoodReportedIds: StateFlow<Set<String>> = _falsehoodReportedIds.asStateFlow()

    private var cachedAllIncidents: List<Incident> = emptyList()

    init {
        loadIncidents()
        observeRealtimeIncidents()
    }

    private fun observeRealtimeIncidents() {
        viewModelScope.launch {
            repository.observeIncidents().collect { incidentList ->
                cachedAllIncidents = incidentList
                applyFiltersAndTab()
            }
        }
    }

    fun onTabSelected(tab: AlertFeedTab) {
        if (_selectedTab.value == tab) return
        _selectedTab.value = tab
        applyFiltersAndTab()
    }

    fun onToggleWitness(incidentId: String) {
        val current = _witnessedIncidentIds.value.toMutableSet()
        val alreadyWitnessed = current.contains(incidentId)
        if (alreadyWitnessed) {
            current.remove(incidentId)
        } else {
            current.add(incidentId)
        }
        _witnessedIncidentIds.value = current

        viewModelScope.launch {
            repository.toggleWitness(incidentId, alreadyWitnessed)
        }
    }

    fun onReportFalsehood(incidentId: String, reason: String = "Noticia falsa o no ocurrió") {
        val current = _falsehoodReportedIds.value.toMutableSet()
        current.add(incidentId)
        _falsehoodReportedIds.value = current

        viewModelScope.launch {
            repository.reportFalsehood(incidentId, reason)
        }
    }

    fun loadIncidents() {
        viewModelScope.launch {
            _uiState.value = UiState.Loading
            try {
                cachedAllIncidents = repository.getIncidents()
                applyFiltersAndTab()
            } catch (e: Exception) {
                _uiState.value = UiState.Error(e.message ?: "No se pudieron cargar las alertas")
            }
        }
    }

    private fun applyFiltersAndTab() {
        viewModelScope.launch {
            try {
                val radius = preferencesRepository.alertRadiusMeters.first()
                val allowedTypes = preferencesRepository.incidentTypesFilter.first()

                // Filtrar según tipos permitidos (Pref #9) y deduplicar
                val typeFiltered = cachedAllIncidents
                    .deduplicar()
                    .filter { incident ->
                        allowedTypes.isEmpty() || allowedTypes.contains(incident.type.name)
                    }

                // Filtrar y ordenar según la pestaña activa: los últimos reportes siempre salen arriba
                val finalList = when (_selectedTab.value) {
                    AlertFeedTab.CERCA_DE_TI -> {
                        // Dentro del radio de proximidad configurado, los más recientes primero
                        typeFiltered
                            .filter { it.distanceMeters <= radius }
                            .sortedWith(
                                compareByDescending<Incident> { it.timestamp }
                                    .thenBy { it.distanceMeters }
                            )
                    }
                    AlertFeedTab.RECIENTES -> {
                        // Pestaña 'Recientes': Orden cronológico estricto con los más nuevos arriba
                        typeFiltered.sortedWith(
                            compareByDescending<Incident> { it.timestamp }
                                .thenBy { it.distanceMeters }
                        )
                    }
                }

                _uiState.value = UiState.Success(finalList)
            } catch (e: Exception) {
                _uiState.value = UiState.Success(cachedAllIncidents.deduplicar())
            }
        }
    }

    private fun List<Incident>.deduplicar(): List<Incident> {
        val result = mutableListOf<Incident>()
        for (incident in this) {
            val index = result.indexOfFirst { exist ->
                exist.id == incident.id || (
                    exist.type == incident.type &&
                    exist.reportedBy == incident.reportedBy &&
                    exist.title == incident.title &&
                    Math.abs(exist.latitude - incident.latitude) < 0.001 &&
                    Math.abs(exist.longitude - incident.longitude) < 0.001 &&
                    Math.abs(exist.timestamp - incident.timestamp) < 10 * 60 * 1000L
                )
            }
            if (index >= 0) {
                if (incident.photoBase64 != null && result[index].photoBase64 == null) {
                    result[index] = incident
                }
            } else {
                result.add(incident)
            }
        }
        return result
    }
}
