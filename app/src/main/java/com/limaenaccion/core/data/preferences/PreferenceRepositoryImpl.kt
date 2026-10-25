package com.limaenaccion.core.data.preferences

import android.content.Context
import android.content.SharedPreferences
import com.limaenaccion.core.data.preferences.model.DoNotDisturbPrefs
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * Implementación de [PreferencesRepository] utilizando la API nativa [SharedPreferences] de Android.
 * Mantiene flujos reactivos [StateFlow] sincronizados mediante [SharedPreferences.OnSharedPreferenceChangeListener]
 * para respetar la arquitectura MVVM + Clean Architecture en toda la aplicación.
 */
class PreferencesRepositoryImpl(context: Context) : PreferencesRepository {

    private val sharedPreferences: SharedPreferences =
        context.applicationContext.getSharedPreferences(PREFS_FILE_NAME, Context.MODE_PRIVATE)

    companion object {
        const val PREFS_FILE_NAME = "lima_en_accion_shared_prefs"

        private const val KEY_TEMA = "tema"
        private const val KEY_IDIOMA = "idioma"
        private const val KEY_TAMANO_FUENTE = "tamano_fuente"
        private const val KEY_SKIP_LOADING_SCREEN = "skip_loading_screen"
        private const val KEY_UBICACION_PERMITIDA = "ubicacion_permitida"
        private const val KEY_TELEFONO_VISIBLE = "telefono_visible"
        private const val KEY_NOTIFICACIONES_ACTIVAS = "notificaciones_activas"
        private const val KEY_RADIO_ALERTAS_METROS = "radio_alertas_metros"
        private const val KEY_TIPOS_INCIDENTE = "tipos_incidente"
        private const val KEY_NO_MOLESTAR = "no_molestar"
        private const val KEY_HORA_INICIO = "hora_inicio"
        private const val KEY_HORA_FIN = "hora_fin"
        private const val KEY_ANULAR_SILENCIO_SOS = "anular_silencio_sos"
        private const val KEY_SOS_PHYSICAL_BUTTON_ENABLED = "sos_physical_button_enabled"

        private val DEFAULT_INCIDENT_TYPES = setOf(
            "ROBO",
            "ACTIVIDAD_INUSUAL",
            "SOSPECHOSO",
            "EMERGENCIA_MEDICA",
            "INCENDIO",
            "ACCIDENTE",
            "VANDALISMO"
        )
    }

    private val _theme = MutableStateFlow(readTheme())
    override val theme: Flow<String> = _theme.asStateFlow()

    private val _language = MutableStateFlow(readLanguage())
    override val language: Flow<String> = _language.asStateFlow()

    private val _fontSize = MutableStateFlow(readFontSize())
    override val fontSize: Flow<Float> = _fontSize.asStateFlow()

    private val _skipLoadingScreen = MutableStateFlow(readSkipLoadingScreen())
    override val skipLoadingScreen: Flow<Boolean> = _skipLoadingScreen.asStateFlow()

    private val _locationPermissionGranted = MutableStateFlow(readLocationPermissionGranted())
    override val locationPermissionGranted: Flow<Boolean> = _locationPermissionGranted.asStateFlow()

    private val _phoneVisible = MutableStateFlow(readPhoneVisible())
    override val phoneVisible: Flow<Boolean> = _phoneVisible.asStateFlow()

    private val _notificationsActive = MutableStateFlow(readNotificationsActive())
    override val notificationsActive: Flow<Boolean> = _notificationsActive.asStateFlow()

    private val _alertRadiusMeters = MutableStateFlow(readAlertRadiusMeters())
    override val alertRadiusMeters: Flow<Int> = _alertRadiusMeters.asStateFlow()

    private val _incidentTypesFilter = MutableStateFlow(readIncidentTypesFilter())
    override val incidentTypesFilter: Flow<Set<String>> = _incidentTypesFilter.asStateFlow()

    private val _doNotDisturb = MutableStateFlow(readDoNotDisturb())
    override val doNotDisturb: Flow<DoNotDisturbPrefs> = _doNotDisturb.asStateFlow()

    private val _overrideSilentModeForSos = MutableStateFlow(readOverrideSilentModeForSos())
    override val overrideSilentModeForSos: Flow<Boolean> = _overrideSilentModeForSos.asStateFlow()

    private val _sosPhysicalButtonEnabled = MutableStateFlow(readSosPhysicalButtonEnabled())
    override val sosPhysicalButtonEnabled: Flow<Boolean> = _sosPhysicalButtonEnabled.asStateFlow()

    // Referencia fuerte al listener para evitar recolección de basura en el WeakHashMap de SharedPreferences
    private val preferenceChangeListener =
        SharedPreferences.OnSharedPreferenceChangeListener { _, key ->
            when (key) {
                KEY_TEMA -> _theme.value = readTheme()
                KEY_IDIOMA -> _language.value = readLanguage()
                KEY_TAMANO_FUENTE -> _fontSize.value = readFontSize()
                KEY_SKIP_LOADING_SCREEN -> _skipLoadingScreen.value = readSkipLoadingScreen()
                KEY_UBICACION_PERMITIDA -> _locationPermissionGranted.value = readLocationPermissionGranted()
                KEY_TELEFONO_VISIBLE -> _phoneVisible.value = readPhoneVisible()
                KEY_NOTIFICACIONES_ACTIVAS -> _notificationsActive.value = readNotificationsActive()
                KEY_RADIO_ALERTAS_METROS -> _alertRadiusMeters.value = readAlertRadiusMeters()
                KEY_TIPOS_INCIDENTE -> _incidentTypesFilter.value = readIncidentTypesFilter()
                KEY_NO_MOLESTAR, KEY_HORA_INICIO, KEY_HORA_FIN -> _doNotDisturb.value = readDoNotDisturb()
                KEY_ANULAR_SILENCIO_SOS -> _overrideSilentModeForSos.value = readOverrideSilentModeForSos()
                KEY_SOS_PHYSICAL_BUTTON_ENABLED -> _sosPhysicalButtonEnabled.value = readSosPhysicalButtonEnabled()
                null -> refreshAllFlows()
            }
        }

    init {
        sharedPreferences.registerOnSharedPreferenceChangeListener(preferenceChangeListener)
    }

    private fun readTheme(): String =
        sharedPreferences.getString(KEY_TEMA, "Sistema") ?: "Sistema"

    private fun readLanguage(): String =
        sharedPreferences.getString(KEY_IDIOMA, "ES") ?: "ES"

    private fun readFontSize(): Float =
        sharedPreferences.getFloat(KEY_TAMANO_FUENTE, 16f)

    private fun readSkipLoadingScreen(): Boolean =
        sharedPreferences.getBoolean(KEY_SKIP_LOADING_SCREEN, false)

    private fun readLocationPermissionGranted(): Boolean =
        sharedPreferences.getBoolean(KEY_UBICACION_PERMITIDA, false)

    private fun readPhoneVisible(): Boolean =
        sharedPreferences.getBoolean(KEY_TELEFONO_VISIBLE, true)

    private fun readNotificationsActive(): Boolean =
        sharedPreferences.getBoolean(KEY_NOTIFICACIONES_ACTIVAS, true)

    private fun readAlertRadiusMeters(): Int =
        sharedPreferences.getInt(KEY_RADIO_ALERTAS_METROS, 1000)

    private fun readIncidentTypesFilter(): Set<String> =
        sharedPreferences.getStringSet(KEY_TIPOS_INCIDENTE, DEFAULT_INCIDENT_TYPES)?.toSet()
            ?: DEFAULT_INCIDENT_TYPES

    private fun readDoNotDisturb(): DoNotDisturbPrefs = DoNotDisturbPrefs(
        enabled = sharedPreferences.getBoolean(KEY_NO_MOLESTAR, false),
        startHour = sharedPreferences.getInt(KEY_HORA_INICIO, 22),
        endHour = sharedPreferences.getInt(KEY_HORA_FIN, 7)
    )

    private fun readOverrideSilentModeForSos(): Boolean =
        sharedPreferences.getBoolean(KEY_ANULAR_SILENCIO_SOS, true)

    private fun readSosPhysicalButtonEnabled(): Boolean =
        sharedPreferences.getBoolean(KEY_SOS_PHYSICAL_BUTTON_ENABLED, false)

    private fun refreshAllFlows() {
        _theme.value = readTheme()
        _language.value = readLanguage()
        _fontSize.value = readFontSize()
        _skipLoadingScreen.value = readSkipLoadingScreen()
        _locationPermissionGranted.value = readLocationPermissionGranted()
        _phoneVisible.value = readPhoneVisible()
        _notificationsActive.value = readNotificationsActive()
        _alertRadiusMeters.value = readAlertRadiusMeters()
        _incidentTypesFilter.value = readIncidentTypesFilter()
        _doNotDisturb.value = readDoNotDisturb()
        _overrideSilentModeForSos.value = readOverrideSilentModeForSos()
        _sosPhysicalButtonEnabled.value = readSosPhysicalButtonEnabled()
    }

    override suspend fun setTheme(theme: String) {
        sharedPreferences.edit().putString(KEY_TEMA, theme).apply()
        _theme.value = theme
    }

    override suspend fun setLanguage(language: String) {
        sharedPreferences.edit().putString(KEY_IDIOMA, language).apply()
        _language.value = language
    }

    override suspend fun setFontSize(sizeSp: Float) {
        sharedPreferences.edit().putFloat(KEY_TAMANO_FUENTE, sizeSp).apply()
        _fontSize.value = sizeSp
    }

    override suspend fun setSkipLoadingScreen(skip: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SKIP_LOADING_SCREEN, skip).apply()
        _skipLoadingScreen.value = skip
    }

    override suspend fun setLocationPermissionGranted(granted: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_UBICACION_PERMITIDA, granted).apply()
        _locationPermissionGranted.value = granted
    }

    override suspend fun setPhoneVisible(visible: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_TELEFONO_VISIBLE, visible).apply()
        _phoneVisible.value = visible
    }

    override suspend fun setNotificationsActive(active: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_NOTIFICACIONES_ACTIVAS, active).apply()
        _notificationsActive.value = active
    }

    override suspend fun setAlertRadiusMeters(radiusMeters: Int) {
        sharedPreferences.edit().putInt(KEY_RADIO_ALERTAS_METROS, radiusMeters).apply()
        _alertRadiusMeters.value = radiusMeters
    }

    override suspend fun setIncidentTypesFilter(types: Set<String>) {
        sharedPreferences.edit().putStringSet(KEY_TIPOS_INCIDENTE, types.toSet()).apply()
        _incidentTypesFilter.value = types.toSet()
    }

    override suspend fun setDoNotDisturb(prefs: DoNotDisturbPrefs) {
        sharedPreferences.edit()
            .putBoolean(KEY_NO_MOLESTAR, prefs.enabled)
            .putInt(KEY_HORA_INICIO, prefs.startHour)
            .putInt(KEY_HORA_FIN, prefs.endHour)
            .apply()
        _doNotDisturb.value = prefs
    }

    override suspend fun setOverrideSilentModeForSos(override: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_ANULAR_SILENCIO_SOS, override).apply()
        _overrideSilentModeForSos.value = override
    }

    override suspend fun setSosPhysicalButtonEnabled(enabled: Boolean) {
        sharedPreferences.edit().putBoolean(KEY_SOS_PHYSICAL_BUTTON_ENABLED, enabled).apply()
        _sosPhysicalButtonEnabled.value = enabled
    }

    override suspend fun clearAllPreferences() {
        sharedPreferences.edit().clear().apply()
        refreshAllFlows()
    }
}
