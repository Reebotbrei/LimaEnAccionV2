package com.limaenaccion.feature_reports.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.feature_reports.data.model.ReportType
import com.limaenaccion.feature_reports.data.model.Reporte
import com.limaenaccion.feature_reports.data.model.Ubicacion
import com.limaenaccion.feature_reports.data.repository.ReportRepository
import com.limaenaccion.feature_reports.data.repository.ReportRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportUiState(
    val selectedType: ReportType? = null,
    val photoTaken: Boolean = false,
    val photoSizeMb: Double = 2.4, // Tamaño simulado, fijo
    val description: String = "",
    val hasVoiceNote: Boolean = false,
    val location: String = "Av Lima 133, Caja de Agua",
    val published: Boolean = false,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val createdReportId: String? = null
) {
    val isDescriptionValid: Boolean get() = description.isNotBlank()
}

class ReportViewModel(
    private val repository: ReportRepository = ReportRepositoryImpl()
) : ViewModel() {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun onTypeSelected(type: ReportType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    fun onPhotoCaptured() {
        _uiState.value = _uiState.value.copy(photoTaken = true)
    }

    fun onRetakePhoto() {
        _uiState.value = _uiState.value.copy(photoTaken = false)
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun onToggleVoiceNote() {
        _uiState.value = _uiState.value.copy(hasVoiceNote = !_uiState.value.hasVoiceNote)
    }

    fun onPublishClicked(usuarioId: String = "anonimo") {
        val currentState = _uiState.value
        if (!currentState.isDescriptionValid) return

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                isLoading = true,
                errorMessage = null
            )

            val nuevoReporte = Reporte(
                usuarioId = usuarioId,
                tipo = currentState.selectedType?.name ?: "GENERAL",
                titulo = currentState.selectedType?.name ?: "Incidencia Urbana",
                descripcion = currentState.description,
                ubicacion = Ubicacion(lat = -12.0163, lng = -77.0044), // Coordenadas de referencia (Caja de Agua)
                fotoUrl = if (currentState.photoTaken) "simulated_photo_url" else null,
                esSos = false
            )

            val resultado = repository.crearReporte(nuevoReporte)

            resultado.onSuccess { generatedId ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    published = true,
                    createdReportId = generatedId
                )
            }.onFailure { exception ->
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    errorMessage = exception.localizedMessage ?: "Error al guardar el reporte en la nube"
                )
            }
        }
    }

    fun resetState() {
        _uiState.value = ReportUiState()
    }
}