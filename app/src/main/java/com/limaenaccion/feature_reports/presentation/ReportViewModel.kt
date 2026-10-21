package com.limaenaccion.feature_reports.presentation

import android.app.Application
import android.graphics.Bitmap
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.util.ImageCompressor
import com.limaenaccion.feature_reports.data.model.ReportType
import com.limaenaccion.feature_reports.data.repository.ReportRepository
import com.limaenaccion.feature_reports.data.repository.ReportRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class ReportUiState(
    val selectedType: ReportType? = null,
    val photoTaken: Boolean = false,
    val photoSizeMb: Double = 0.45, // Tamaño real comprimido (< 0.5 MB - RNF-10)
    val photoBitmap: Bitmap? = null, // Imagen real capturada por la cámara
    val photoBase64: String? = null, // Imagen comprimida en Base64 para el backend
    val description: String = "",
    val hasVoiceNote: Boolean = false,
    val location: String = "Av Lima 133, Caja de Agua",
    val isPublishing: Boolean = false,
    val published: Boolean = false,
    val errorMessage: String? = null
) {
    val isDescriptionValid: Boolean get() = description.isNotBlank()
}

class ReportViewModel @JvmOverloads constructor(
    application: Application,
    private val reportRepository: ReportRepository = ReportRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ReportUiState())
    val uiState: StateFlow<ReportUiState> = _uiState.asStateFlow()

    fun onTypeSelected(type: ReportType) {
        _uiState.value = _uiState.value.copy(selectedType = type)
    }

    // Captura con cámara real de Android y compresión RNF-10
    fun onPhotoCaptured(bitmap: Bitmap?) {
        if (bitmap != null) {
            val bytes = ImageCompressor.compressBitmap(bitmap)
            val base64 = ImageCompressor.toBase64String(bitmap)
            val sizeMb = ImageCompressor.calculateSizeMb(bytes)
            _uiState.value = _uiState.value.copy(
                photoTaken = true,
                photoBitmap = bitmap,
                photoBase64 = base64,
                photoSizeMb = sizeMb
            )
        } else {
            _uiState.value = _uiState.value.copy(photoTaken = true)
        }
    }

    fun onRetakePhoto() {
        _uiState.value = _uiState.value.copy(
            photoTaken = false,
            photoBitmap = null,
            photoBase64 = null
        )
    }

    fun onDescriptionChange(value: String) {
        _uiState.value = _uiState.value.copy(description = value)
    }

    fun onToggleVoiceNote() {
        _uiState.value = _uiState.value.copy(hasVoiceNote = !_uiState.value.hasVoiceNote)
    }

    // Publicación a través de ReportRepository (Clean Architecture)
    fun onPublishClicked() {
        val state = _uiState.value
        if (!state.isDescriptionValid || state.isPublishing) return

        _uiState.value = state.copy(isPublishing = true, errorMessage = null)

        viewModelScope.launch {
            val type = state.selectedType ?: ReportType.ROBO
            val title = "Reporte de ${type.label}"
            val description = state.description
            val latitud = -12.019542
            val longitud = -77.014231

            val result = reportRepository.publishReport(
                type = type,
                title = title,
                description = description,
                latitude = latitud,
                longitude = longitud,
                photoBase64 = state.photoBase64
            )

            result.fold(
                onSuccess = {
                    _uiState.value = _uiState.value.copy(
                        isPublishing = false,
                        published = true
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
