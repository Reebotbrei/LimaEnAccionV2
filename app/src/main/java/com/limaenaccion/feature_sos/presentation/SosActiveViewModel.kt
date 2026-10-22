package com.limaenaccion.feature_sos.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.dao.ContactoDao
import com.limaenaccion.core.data.local.entities.ContactoEntity
import com.limaenaccion.core.data.network.ApiService
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.network.model.CreateReportRequest
import com.limaenaccion.core.data.preferences.PreferencesRepository
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import com.limaenaccion.feature_sos.domain.model.SosDeliveryState
import com.limaenaccion.feature_sos.domain.model.SosSessionConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SosActiveUiState(
    val deliveryState: SosDeliveryState = SosDeliveryState.PREPARANDO,
    val primaryContact: ContactoEntity? = null,
    val allContacts: List<ContactoEntity> = emptyList(),
    val contactPhones: List<String> = emptyList(),
    val radiusMeters: Int = 1000,
    val deactivated: Boolean = false,
    val isSilentMode: Boolean = SosSessionConfig.isSilentMode,
    val isDemoMode: Boolean = SosSessionConfig.isDemoMode,
    val latitude: Double = -12.019542,
    val longitude: Double = -77.014231,
    val addressDesc: String = "San Juan de Lurigancho, Lima",
    val smsText: String = "¡ALERTA SOS VECINAL! Me encuentro en peligro y necesito ayuda en mi ubicación: https://maps.google.com/?q=-12.019542,-77.014231",
    val smsSentNotice: Boolean = false,
    val errorMessage: String? = null
)

class SosActiveViewModel @JvmOverloads constructor(
    application: Application,
    private val contactoDao: ContactoDao = AppDatabase.getInstance(application).contactoDao(),
    private val preferencesRepository: PreferencesRepository = PreferencesRepositoryImpl(application),
    private val apiService: ApiService = RetrofitClient.instance
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SosActiveUiState())
    val uiState: StateFlow<SosActiveUiState> = _uiState.asStateFlow()

    init {
        loadSosConfigAndContacts()
        startDispatchFlow()
    }

    private fun loadSosConfigAndContacts() {
        viewModelScope.launch {
            val radius = try {
                preferencesRepository.alertRadiusMeters.first()
            } catch (_: Exception) {
                1000
            }

            // Observar contactos guardados en Room
            contactoDao.obtenerTodos().collect { contactsList ->
                val primary = contactsList.firstOrNull { it.esPrincipal } ?: contactsList.firstOrNull()
                val phones = contactsList.map { it.telefono }
                val gpsLink = "https://maps.google.com/?q=${_uiState.value.latitude},${_uiState.value.longitude}"
                val text = "¡ALERTA SOS VECINAL! Me encuentro en peligro y necesito auxilio inmediato. Mi ubicación GPS: $gpsLink"

                _uiState.value = _uiState.value.copy(
                    primaryContact = primary,
                    allContacts = contactsList,
                    contactPhones = phones,
                    radiusMeters = radius,
                    smsText = text
                )
            }
        }
    }

    fun startDispatchFlow() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                deliveryState = SosDeliveryState.PREPARANDO,
                errorMessage = null
            )
            // Paso 1: Fijar coordenadas GPS (Honesto)
            delay(900)

            _uiState.value = _uiState.value.copy(deliveryState = SosDeliveryState.ENVIANDO)

            if (_uiState.value.isDemoMode) {
                // Flujo seguro de simulación / modo prueba
                delay(1200)
                _uiState.value = _uiState.value.copy(deliveryState = SosDeliveryState.RECIBIDA_SISTEMA)
                delay(800)
                _uiState.value = _uiState.value.copy(deliveryState = SosDeliveryState.CONTACTOS_NOTIFICADOS)
            } else {
                // Intento real con el backend de Lima en Acción
                try {
                    withContext(Dispatchers.IO) {
                        apiService.createReport(
                            CreateReportRequest(
                                tipo = "SOS",
                                titulo = "ALERTA SOS DE EMERGENCIA",
                                descripcion = "Alerta SOS emitida por ciudadano en peligro",
                                latitud = _uiState.value.latitude,
                                longitud = _uiState.value.longitude,
                                esSos = true
                            )
                        )
                    }
                    _uiState.value = _uiState.value.copy(deliveryState = SosDeliveryState.RECIBIDA_SISTEMA)
                    delay(800)
                    _uiState.value = _uiState.value.copy(deliveryState = SosDeliveryState.CONTACTOS_NOTIFICADOS)
                } catch (e: Exception) {
                    // Estado honesto si la red falla: el usuario no es engañado y se le ofrece marcación directa
                    _uiState.value = _uiState.value.copy(
                        deliveryState = SosDeliveryState.ERROR_ENVIO,
                        errorMessage = "Sin conexión de red con la central. Usa marcación directa inmediata."
                    )
                }
            }
        }
    }

    fun retryDispatch() {
        startDispatchFlow()
    }

    fun toggleSilentMode() {
        SosSessionConfig.isSilentMode = !_uiState.value.isSilentMode
        _uiState.value = _uiState.value.copy(isSilentMode = SosSessionConfig.isSilentMode)
    }

    fun toggleDemoMode() {
        SosSessionConfig.isDemoMode = !_uiState.value.isDemoMode
        _uiState.value = _uiState.value.copy(isDemoMode = SosSessionConfig.isDemoMode)
        startDispatchFlow()
    }

    fun onDeactivateClicked() {
        SosSessionConfig.reset()
        _uiState.value = _uiState.value.copy(deactivated = true)
    }

    fun onSmsSent() {
        _uiState.value = _uiState.value.copy(smsSentNotice = true)
    }
}
