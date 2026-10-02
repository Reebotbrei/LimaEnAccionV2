package com.limaenaccion.feature_sos.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SosActiveUiState(
    val notifiedNeighbors: Int = 12,
    val radiusMeters: Int = 500,
    val deactivated: Boolean = false
)

class SosActiveViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(SosActiveUiState())
    val uiState: StateFlow<SosActiveUiState> = _uiState.asStateFlow()

    fun onDeactivateClicked() {
        _uiState.value = _uiState.value.copy(deactivated = true)
    }
}