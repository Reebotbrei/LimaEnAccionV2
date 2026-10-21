package com.limaenaccion.feature_sos.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.dao.ContactoDao
import com.limaenaccion.core.data.preferences.PreferencesRepository
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SosActiveUiState(
    val notifiedNeighbors: Int = 12,
    val radiusMeters: Int = 1000,
    val deactivated: Boolean = false,
    val contactPhones: List<String> = emptyList(),
    val smsText: String = "¡ALERTA SOS VECINAL! Me encuentro en peligro y necesito ayuda en mi ubicación: https://maps.google.com/?q=-12.019542,-77.014231",
    val smsSentNotice: Boolean = false
)

class SosActiveViewModel @JvmOverloads constructor(
    application: Application,
    private val contactoDao: ContactoDao = AppDatabase.getInstance(application).contactoDao(),
    private val preferencesRepository: PreferencesRepository = PreferencesRepositoryImpl(application)
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SosActiveUiState())
    val uiState: StateFlow<SosActiveUiState> = _uiState.asStateFlow()

    init {
        loadSosConfig()
    }

    private fun loadSosConfig() {
        viewModelScope.launch {
            val radius = preferencesRepository.alertRadiusMeters.first()
            val contacts = withContext(Dispatchers.IO) { contactoDao.obtenerListaTodos() }
            val phones = contacts.map { it.telefono }
            _uiState.value = _uiState.value.copy(
                radiusMeters = radius,
                contactPhones = phones
            )
        }
    }

    fun onDeactivateClicked() {
        _uiState.value = _uiState.value.copy(deactivated = true)
    }

    fun onSmsSent() {
        _uiState.value = _uiState.value.copy(smsSentNotice = true)
    }
}
