package com.limaenaccion.feature_sos.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.dao.ContactoDao
import com.limaenaccion.core.data.local.entities.ContactoEntity
import com.limaenaccion.core.data.network.ApiService
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.network.model.CreateReportRequest
import com.limaenaccion.core.data.preferences.PreferencesRepository
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import com.limaenaccion.core.data.session.SessionManager
import com.limaenaccion.core.util.DeviceLocationProvider
import com.limaenaccion.feature_sos.domain.model.SosDeliveryState
import com.limaenaccion.feature_sos.domain.model.SosSessionConfig
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
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
    val latitude: Double = DeviceLocationProvider.DEFAULT_LIMA_LAT,
    val longitude: Double = DeviceLocationProvider.DEFAULT_LIMA_LNG,
    val addressDesc: String = "Detectando ubicación GPS en tiempo real...",
    val smsText: String = "¡ALERTA SOS VECINAL! Me encuentro en peligro y necesito ayuda en mi ubicación.",
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
                val text = "¡ALERTA SOS VECINAL! Me encuentro en peligro y necesito auxilio inmediato. Mi ubicación GPS: $gpsLink (${_uiState.value.addressDesc})"

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

            // Paso 1: Captura activa del sensor GPS en hardware real
            val loc = DeviceLocationProvider.getBestLocation(getApplication())
            val gpsLink = "https://maps.google.com/?q=${loc.latitude},${loc.longitude}"
            val text = "¡ALERTA SOS VECINAL! Me encuentro en peligro y necesito auxilio inmediato. Mi ubicación GPS: $gpsLink (${loc.address})"

            _uiState.value = _uiState.value.copy(
                latitude = loc.latitude,
                longitude = loc.longitude,
                addressDesc = loc.address,
                smsText = text,
                deliveryState = SosDeliveryState.ENVIANDO
            )

            if (_uiState.value.isDemoMode) {
                // Flujo seguro de simulación / modo prueba
                delay(1200)
                _uiState.value = _uiState.value.copy(deliveryState = SosDeliveryState.RECIBIDA_SISTEMA)
                delay(800)
                _uiState.value = _uiState.value.copy(deliveryState = SosDeliveryState.CONTACTOS_NOTIFICADOS)
            } else {
                // 1. Transmitir en vivo a Firebase Cloud Firestore
                try {
                    withContext(Dispatchers.IO) {
                        val firestore = FirebaseFirestore.getInstance()
                        val sosId = "sos_${System.currentTimeMillis()}"
                        val currentUser = SessionManager.currentUser.value
                        val userEmail = currentUser?.correo ?: "vecino_sos@limaenaccion.pe"
                        val userName = currentUser?.nombreCompleto ?: "Ciudadano en Alerta"

                        val reportDoc = hashMapOf(
                            "id" to sosId,
                            "usuarioId" to userEmail,
                            "usuarioNombre" to userName,
                            "esAnonimo" to false,
                            "tipo" to "SOS",
                            "titulo" to "ALERTA SOS DE EMERGENCIA",
                            "descripcion" to "Alerta SOS emitida por ciudadano en peligro en ${loc.address}",
                            "latitud" to loc.latitude,
                            "longitud" to loc.longitude,
                            "fotoUrl" to "",
                            "fotoBase64" to "",
                            "creadoEn" to System.currentTimeMillis(),
                            "venceEn" to (System.currentTimeMillis() + 6 * 3600000L),
                            "estado" to "REPORTADO",
                            "esSos" to true,
                            "witnessCount" to 1,
                            "falseReportsCount" to 0,
                            "commentsCount" to 0
                        )
                        firestore.collection("reportes").document(sosId).set(reportDoc).await()
                        Log.d("SOS_VM", "✅ [FIRESTORE] Alerta SOS transmitida en vivo con GPS real: ${loc.latitude}, ${loc.longitude}")
                    }
                } catch (e: Exception) {
                    Log.w("SOS_VM", "Aviso Firestore SOS: ${e.message}")
                }

                // 2. Transmisión al Backend Node.js en Azure
                try {
                    withContext(Dispatchers.IO) {
                        apiService.createReport(
                            CreateReportRequest(
                                tipo = "SOS",
                                titulo = "ALERTA SOS DE EMERGENCIA",
                                descripcion = "Alerta SOS emitida por ciudadano en peligro en ${loc.address}",
                                latitud = loc.latitude,
                                longitud = loc.longitude,
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
