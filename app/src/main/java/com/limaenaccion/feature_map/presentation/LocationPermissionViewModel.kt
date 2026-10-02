package com.limaenaccion.feature_map.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class LocationPermissionUiState {
    NOT_REQUESTED, GRANTED, DENIED
}

/**
 * El ViewModel NO toca Activity/Context (eso rompería MVVM). Solo guarda
 * el resultado que le reporta la Screen después de usar el
 * ActivityResultContract de Android.
 */
class LocationPermissionViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(LocationPermissionUiState.NOT_REQUESTED)
    val uiState: StateFlow<LocationPermissionUiState> = _uiState.asStateFlow()

    fun onPermissionResult(granted: Boolean) {
        _uiState.value = if (granted) LocationPermissionUiState.GRANTED else LocationPermissionUiState.DENIED
    }
}