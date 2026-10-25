package com.limaenaccion.main

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.preferences.PreferencesRepository
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class MainViewModel @JvmOverloads constructor(
    application: Application,
    private val preferencesRepository: PreferencesRepository = PreferencesRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _skipLoadingScreen = MutableStateFlow<Boolean?>(null)
    val skipLoadingScreen: StateFlow<Boolean?> = _skipLoadingScreen.asStateFlow()

    val theme: StateFlow<String> = preferencesRepository.theme.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "Sistema"
    )

    val fontSize: StateFlow<Float> = preferencesRepository.fontSize.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = 16f
    )

    val language: StateFlow<String> = preferencesRepository.language.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = "ES"
    )

    val activeSessionUid: StateFlow<String> = preferencesRepository.activeSessionUid.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = ""
    )

    val rememberSession: StateFlow<Boolean> = preferencesRepository.rememberSession.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = false
    )

    val isReady: Boolean get() = _skipLoadingScreen.value != null

    init {
        viewModelScope.launch {
            _skipLoadingScreen.value = preferencesRepository.skipLoadingScreen.first()
        }
    }

    fun clearSession() {
        viewModelScope.launch {
            preferencesRepository.clearSession()
        }
    }
}
