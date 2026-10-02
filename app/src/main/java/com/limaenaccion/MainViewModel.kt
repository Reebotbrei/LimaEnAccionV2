package com.limaenaccion

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

class MainViewModel @JvmOverloads constructor(
    application: Application,
    private val preferencesRepository: PreferencesRepository = PreferencesRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _skipLoadingScreen = MutableStateFlow<Boolean?>(null)
    val skipLoadingScreen: StateFlow<Boolean?> = _skipLoadingScreen.asStateFlow()

    val isReady: Boolean get() = _skipLoadingScreen.value != null

    init {
        viewModelScope.launch {
            _skipLoadingScreen.value = preferencesRepository.skipLoadingScreen.first()
        }
    }
}