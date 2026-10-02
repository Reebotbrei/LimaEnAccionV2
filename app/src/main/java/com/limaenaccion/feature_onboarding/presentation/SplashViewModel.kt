package com.limaenaccion.feature_onboarding.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.preferences.PreferencesRepository
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed class SplashUiState {
    object Loading : SplashUiState()
    object ShowSkipDialog : SplashUiState()
    object NavigateToEmergency : SplashUiState()
}

class SplashViewModel @JvmOverloads constructor(
    application: Application,
    private val preferencesRepository: PreferencesRepository = PreferencesRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<SplashUiState>(SplashUiState.Loading)
    val uiState: StateFlow<SplashUiState> = _uiState.asStateFlow()

    private val _dontShowAgainChecked = MutableStateFlow(false)
    val dontShowAgainChecked: StateFlow<Boolean> = _dontShowAgainChecked.asStateFlow()

    init {
        viewModelScope.launch {
            delay(2000) // CA-02: se muestra la carga real por 2s
            _uiState.value = SplashUiState.ShowSkipDialog
        }
    }

    fun onDontShowAgainChanged(checked: Boolean) {
        _dontShowAgainChecked.value = checked
    }

    fun onContinueClicked() {
        viewModelScope.launch {
            preferencesRepository.setSkipLoadingScreen(_dontShowAgainChecked.value)
            _uiState.value = SplashUiState.NavigateToEmergency
        }
    }
}