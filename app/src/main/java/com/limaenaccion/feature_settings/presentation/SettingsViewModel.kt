package com.limaenaccion.feature_settings.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.dao.ContactoDao
import com.limaenaccion.core.data.local.dao.ReporteDao
import com.limaenaccion.core.data.preferences.DoNotDisturbPrefs
import com.limaenaccion.core.data.preferences.PreferencesRepository
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class SettingsUiState(
    // Pref #1 - Tema ("Sistema", "Claro", "Oscuro", "Daltonico")
    val theme: String = "Sistema",
    // Pref #2 - Idioma ("ES", "EN")
    val language: String = "ES",
    // Pref #3 - Tamaño de fuente (12sp a 30sp)
    val fontSizeSp: Float = 16f,
    // Pref #4 - Omitir pantalla de carga
    val skipLoadingScreen: Boolean = false,
    // Pref #5 - Permiso de ubicación GPS
    val locationPermissionGranted: Boolean = false,
    // Pref #6 - Visibilidad del teléfono ante vecinos
    val phoneVisible: Boolean = true,
    // Pref #7 - Notificaciones push maestras
    val pushNotificationsEnabled: Boolean = true,
    // Pref #8 - Radio de alertas (500m, 1000m, 3000m)
    val alertRadiusMeters: Int = 1000,
    // Pref #9 - Filtro por incidentes
    val incidentTypesFilter: Set<String> = setOf("ROBO", "SOSPECHOSO", "EMERGENCIA_MEDICA", "INCENDIO", "ACCIDENTE", "VANDALISMO"),
    // Pref #10 - Modo No Molestar
    val doNotDisturb: DoNotDisturbPrefs = DoNotDisturbPrefs(),
    // Pref #11 - Notificación de borrado de caché
    val cacheClearedMessage: String? = null,
    // Pref #13 - Anular modo silencioso para SOS
    val overrideSilentModeForSos: Boolean = true,
    // Pref #14 - Nombre del contacto de auxilio principal
    val primaryContactName: String? = null,
    // Información general
    val registeredZone: String = "Caja de Agua, SJL",
    val appVersion: String = "v2.2.0",
    // Diálogos de confirmación
    val showLogoutDialog: Boolean = false,
    val showDeleteAccountDialog: Boolean = false,
    val showClearCacheDialog: Boolean = false
)

class SettingsViewModel @JvmOverloads constructor(
    application: Application,
    private val preferencesRepository: PreferencesRepository = PreferencesRepositoryImpl(application),
    private val contactoDao: ContactoDao = AppDatabase.getInstance(application).contactoDao(),
    private val reporteDao: ReporteDao = AppDatabase.getInstance(application).reporteDao()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(SettingsUiState())
    val uiState: StateFlow<SettingsUiState> = _uiState.asStateFlow()

    init {
        loadAllPreferences()
    }

    private fun loadAllPreferences() {
        viewModelScope.launch {
            val theme = preferencesRepository.theme.first()
            val language = preferencesRepository.language.first()
            val fontSize = preferencesRepository.fontSize.first()
            val skipLoading = preferencesRepository.skipLoadingScreen.first()
            val location = preferencesRepository.locationPermissionGranted.first()
            val phoneVisible = preferencesRepository.phoneVisible.first()
            val push = preferencesRepository.notificationsActive.first()
            val radius = preferencesRepository.alertRadiusMeters.first()
            val types = preferencesRepository.incidentTypesFilter.first()
            val dnd = preferencesRepository.doNotDisturb.first()
            val overrideSilent = preferencesRepository.overrideSilentModeForSos.first()

            val primaryContact = withContext(Dispatchers.IO) {
                contactoDao.obtenerContactoPrincipal().first()
            }

            _uiState.value = _uiState.value.copy(
                theme = theme,
                language = language,
                fontSizeSp = fontSize,
                skipLoadingScreen = skipLoading,
                locationPermissionGranted = location,
                phoneVisible = phoneVisible,
                pushNotificationsEnabled = push,
                alertRadiusMeters = radius,
                incidentTypesFilter = types,
                doNotDisturb = dnd,
                overrideSilentModeForSos = overrideSilent,
                primaryContactName = primaryContact?.let { "${it.nombre} (${it.telefono})" }
            )
        }
    }

    // Pref #1: Tema
    fun onThemeChange(newTheme: String) {
        _uiState.value = _uiState.value.copy(theme = newTheme)
        viewModelScope.launch { preferencesRepository.setTheme(newTheme) }
    }

    // Pref #2: Idioma
    fun onLanguageChange(newLang: String) {
        _uiState.value = _uiState.value.copy(language = newLang)
        viewModelScope.launch { preferencesRepository.setLanguage(newLang) }
    }

    // Pref #3: Tamaño de Fuente
    fun onFontSizeChange(newSizeSp: Float) {
        _uiState.value = _uiState.value.copy(fontSizeSp = newSizeSp)
        viewModelScope.launch { preferencesRepository.setFontSize(newSizeSp) }
    }

    // Pref #4: Omitir Pantalla de Carga
    fun onToggleSkipLoadingScreen(skip: Boolean) {
        _uiState.value = _uiState.value.copy(skipLoadingScreen = skip)
        viewModelScope.launch { preferencesRepository.setSkipLoadingScreen(skip) }
    }

    // Pref #6: Visibilidad del Teléfono
    fun onTogglePhoneVisible(visible: Boolean) {
        _uiState.value = _uiState.value.copy(phoneVisible = visible)
        viewModelScope.launch { preferencesRepository.setPhoneVisible(visible) }
    }

    // Pref #7: Notificaciones Push
    fun onTogglePushNotifications(active: Boolean) {
        _uiState.value = _uiState.value.copy(pushNotificationsEnabled = active)
        viewModelScope.launch { preferencesRepository.setNotificationsActive(active) }
    }

    // Pref #8: Radio de Alertas
    fun onAlertRadiusChange(radiusMeters: Int) {
        _uiState.value = _uiState.value.copy(alertRadiusMeters = radiusMeters)
        viewModelScope.launch { preferencesRepository.setAlertRadiusMeters(radiusMeters) }
    }

    // Pref #9: Filtro por Incidente
    fun onToggleIncidentType(type: String) {
        val currentSet = _uiState.value.incidentTypesFilter.toMutableSet()
        if (currentSet.contains(type)) {
            if (currentSet.size > 1) currentSet.remove(type) // Mantener al menos 1
        } else {
            currentSet.add(type)
        }
        _uiState.value = _uiState.value.copy(incidentTypesFilter = currentSet)
        viewModelScope.launch { preferencesRepository.setIncidentTypesFilter(currentSet) }
    }

    // Pref #10: Modo No Molestar
    fun onToggleDoNotDisturb(enabled: Boolean) {
        val updated = _uiState.value.doNotDisturb.copy(enabled = enabled)
        _uiState.value = _uiState.value.copy(doNotDisturb = updated)
        viewModelScope.launch { preferencesRepository.setDoNotDisturb(updated) }
    }

    fun onDoNotDisturbHoursChange(startHour: Int, endHour: Int) {
        val updated = _uiState.value.doNotDisturb.copy(startHour = startHour, endHour = endHour)
        _uiState.value = _uiState.value.copy(doNotDisturb = updated)
        viewModelScope.launch { preferencesRepository.setDoNotDisturb(updated) }
    }

    // Pref #11: Borrado de Caché
    fun onShowClearCacheDialog() { _uiState.value = _uiState.value.copy(showClearCacheDialog = true) }
    fun onDismissClearCacheDialog() { _uiState.value = _uiState.value.copy(showClearCacheDialog = false) }

    fun onConfirmClearCache() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                reporteDao.eliminarTodos()
                getApplication<Application>().cacheDir.deleteRecursively()
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        showClearCacheDialog = false,
                        cacheClearedMessage = "Caché de mapas y reportes temporales liberada correctamente."
                    )
                }
            } catch (e: Exception) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        showClearCacheDialog = false,
                        cacheClearedMessage = "Error al limpiar caché: ${e.message}"
                    )
                }
            }
        }
    }

    fun onDismissCacheMessage() {
        _uiState.value = _uiState.value.copy(cacheClearedMessage = null)
    }

    // Pref #13: Anular Modo Silencioso para SOS
    fun onToggleOverrideSilentMode(override: Boolean) {
        _uiState.value = _uiState.value.copy(overrideSilentModeForSos = override)
        viewModelScope.launch { preferencesRepository.setOverrideSilentModeForSos(override) }
    }

    // Sesión
    fun onLogoutClicked() { _uiState.value = _uiState.value.copy(showLogoutDialog = true) }
    fun onDismissLogoutDialog() { _uiState.value = _uiState.value.copy(showLogoutDialog = false) }

    // Pref #16: Eliminación de Cuenta (Ley N° 29733)
    fun onDeleteAccountClicked() { _uiState.value = _uiState.value.copy(showDeleteAccountDialog = true) }
    fun onDismissDeleteAccountDialog() { _uiState.value = _uiState.value.copy(showDeleteAccountDialog = false) }

    fun onConfirmDeleteAccount(onAccountDeleted: () -> Unit) {
        viewModelScope.launch(Dispatchers.IO) {
            // Purgar todos los datos locales y preferencias cumpliendo Ley N° 29733
            preferencesRepository.clearAllPreferences()
            reporteDao.eliminarTodos()
            contactoDao.eliminarTodos()
            getApplication<Application>().cacheDir.deleteRecursively()

            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value.copy(showDeleteAccountDialog = false)
                onAccountDeleted()
            }
        }
    }
}
