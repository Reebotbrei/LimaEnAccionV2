package com.limaenaccion.feature_map.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.preferences.PreferencesRepository
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch

enum class LocationPermissionUiState {
    NOT_REQUESTED, GRANTED, DENIED
}

/**
 * El ViewModel NO toca Activity/Context directamente. Gestiona el estado
 * y persiste el permiso de ubicación en PreferencesRepository (DataStore - Pref #5).
 */
class LocationPermissionViewModel @JvmOverloads constructor(
    application: Application,
    private val preferencesRepository: PreferencesRepository = PreferencesRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(LocationPermissionUiState.NOT_REQUESTED)
    val uiState: StateFlow<LocationPermissionUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            if (preferencesRepository.locationPermissionGranted.first()) {
                _uiState.value = LocationPermissionUiState.GRANTED
            }
        }
    }

    fun onPermissionResult(granted: Boolean) {
        _uiState.value = if (granted) LocationPermissionUiState.GRANTED else LocationPermissionUiState.DENIED
        viewModelScope.launch {
            preferencesRepository.setLocationPermissionGranted(granted)
        }
    }
}