package com.limaenaccion.feature_reports.presentation

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.util.ImageCompressor
import com.limaenaccion.feature_reports.data.model.ReportTimeframe
import com.limaenaccion.feature_reports.data.model.ReportType
import com.limaenaccion.feature_reports.data.repository.ReportRepository
import com.limaenaccion.feature_reports.data.repository.ReportRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportUiState(
    val selectedType: ReportType? = null,
    val timeframe: ReportTimeframe = ReportTimeframe.AHORA,
    val location: String = "Av. Próceres de la Independencia 133, SJL",
    val isCustomLocation: Boolean = false,
    val description: String = "",
    val isAnonymous: Boolean = false,
    val photoTaken: Boolean = false,
    val photoSizeMb: Double = 0.45,
    val photoBitmap: Bitmap? = null,
    val photoBase64: String? = null,
    val hasVoiceNote: Boolean = false,
    val isPublishing: Boolean = false,
    val published: Boolean = false,
    val errorMessage: String? = null,
    val hasDraftSaved: Boolean = false
) {
    val canPublish: Boolean get() = selectedType != null && location.isNotBlank()
    val isDescriptionValid: Boolean get() = true // En reporte rápido, la descripción y foto son opcionales
}

class ReportViewModel @JvmOverloads constructor(
    application: Application,
    private val reportRepository: ReportRepository = ReportRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun onTypeSelected(type: ReportType) {
        _uiState.value = _uiState.value.copy(selectedType = type, hasDraftSaved = true)
    }

    fun onTimeframeChanged(timeframe: ReportTimeframe) {
        _uiState.value = _uiState.value.copy(timeframe = timeframe, hasDraftSaved = true)
    }

    fun onLocationChanged(newLocation: String) {
        _uiState.value = _uiState.value.copy(
            location = newLocation,
            isCustomLocation = true,
            hasDraftSaved = true
        )
    }

    fun onResetLocationToGps() {
        _uiState.value = _uiState.value.copy(
            location = "Av. Próceres de la Independencia 133, SJL (GPS actual)",
            isCustomLocation = false,
            hasDraftSaved = true
        )
    }

    fun onToggleAnonymous() {
        _uiState.value = _uiState.value.copy(
            isAnonymous = !_uiState.value.isAnonymous,
            hasDraftSaved = true
        )
    }

    fun onPhotoCaptured(bitmap: Bitmap?) {
        if (bitmap != null) {
            val bytes = ImageCompressor.compressBitmap(bitmap)
            val base64 = ImageCompressor.toBase64String(bitmap)
            val sizeMb = ImageCompressor.calculateSizeMb(bytes)
            _uiState.value = _uiState.value.copy(
                photoTaken = true,
                photoBitmap = bitmap,
                photoBase64 = base64,
                photoSizeMb = sizeMb,
                hasDraftSaved = true
            )
        } else {
            _uiState.value = _uiState.value.copy(photoTaken = true, hasDraftSaved = true)
        }
    }

    fun onRetakePhoto() {
        _uiState.value = _uiState.value.copy(
            photoTaken = false,
            photoBitmap = null,
            photoBase64 = null,
            hasDraftSaved = true
        )
    }

    fun onRemovePhoto() {
        _uiState.value = _uiState.value.copy(
            photoTaken = false,
            photoBitmap = null,
            photoBase64 = null,
            hasDraftSaved = true
        )
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value, hasDraftSaved = true)
    }

    fun onToggleVoiceNote() {
        _uiState.value = _uiState.value.copy(
            hasVoiceNote = !_uiState.value.hasVoiceNote,
            hasDraftSaved = true
        )
    }

    fun resetReport() {
        _uiState.value = ReportUiState()
    }

    fun onPublishClicked() {
        val state = _uiState.value
        if (!state.canPublish || state.isPublishing) return

        _uiState.value = state.copy(isPublishing = true, errorMessage = null)

        viewModelScope.launch {
            val type = state.selectedType ?: ReportType.ROBO
            val title = if (state.isAnonymous) {
                "[Anónimo] ${type.label} · ${state.timeframe.label}"
            } else {
                "${type.label} · ${state.timeframe.label}"
            }
            val finalDescription = if (state.description.isNotBlank()) {
                "${state.description}\nUbicación reportada: ${state.location}"
            } else {
                "Alerta de ${type.label} reportada en ${state.location}. Ocurrencia: ${state.timeframe.label}."
            }

            val latitud = -12.019542
            val longitud = -77.014231

            val result = reportRepository.publishReport(
                type = type,
                title = title,
                description = finalDescription,
                latitude = latitud,
                longitude = longitud,
                photoBase64 = state.photoBase64
            )

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isPublishing = false,
                        published = true,
                        hasDraftSaved = false
                    )
                },
                onFailure = { error ->
                    _uiState.value = _uiState.value.copy(
                        isPublishing = false,
                        errorMessage = error.message
                    )
                }
            )
        }
    }
}
