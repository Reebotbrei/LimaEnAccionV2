package com.limaenaccion.feature_sos.presentation

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

private const val COUNTDOWN_START = 3

data class SosActivationUiState(
    val secondsRemaining: Int = COUNTDOWN_START,
    val activated: Boolean = false,
    val cancelled: Boolean = false
)

class SosActivationViewModel : ViewModel() {

    private val _uiState = MutableStateFlow(SosActivationUiState())
    val uiState: StateFlow<SosActivationUiState> = _uiState.asStateFlow()

    init { startCountdown() }

    private fun startCountdown() {
        viewModelScope.launch {
            for (second in COUNTDOWN_START downTo 1) {
                if (_uiState.value.cancelled) return@launch
                _uiState.value = _uiState.value.copy(secondsRemaining = second)
                delay(1000)
            }
            if (!_uiState.value.cancelled) {
                _uiState.value = _uiState.value.copy(activated = true)
            }
        }
    }

    fun onCancelClicked() {
        _uiState.value = _uiState.value.copy(cancelled = true)
    }
}