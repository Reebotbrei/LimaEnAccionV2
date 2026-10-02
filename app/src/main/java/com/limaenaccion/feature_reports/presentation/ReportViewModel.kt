package com.limaenaccion.feature_reports.presentation

import androidx.lifecycle.ViewModel
import com.limaenaccion.feature_reports.data.model.ReportType
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ReportUiState(
    val selectedType: ReportType? = null,
    val photoTaken: Boolean = false,
    val photoSizeMb: Double = 2.4, // Tamaño simulado, fijo
    val description: String = "",
    val hasVoiceNote: Boolean = false,
    val location: String = "Av Lima 133, Caja de Agua",
    val published: Boolean = false
) {
    val isDescriptionValid: Boolean get() = description.isNotBlank()
}

class ReportViewModel : ViewModel() {

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

    fun onPublishClicked() {
        // Se simula la publicación.
        if (_uiState.value.isDescriptionValid) {
            _uiState.value = _uiState.value.copy(published = true)
        }
    }
}