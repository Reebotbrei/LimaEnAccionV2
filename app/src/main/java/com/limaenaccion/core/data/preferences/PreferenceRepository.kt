package com.limaenaccion.core.data.preferences

import kotlinx.coroutines.flow.Flow

// Pref #14 - Contacto de Auxilio Principal
data class PrimaryContactPrefs(
    val name: String,
    val phoneNumber: String
)

// Pref #10 - Modo No Molestar
data class DoNotDisturbPrefs(
    val enabled: Boolean = false,
    val startHour: Int = 22,
    val endHour: Int = 7
)

interface PreferencesRepository {

    // Pref #1 — Tema ("Sistema", "Claro", "Oscuro")
    val theme: Flow<String>
    suspend fun setTheme(theme: String)

    // Pref #2 — Idioma ("ES", "EN")
    val language: Flow<String>
    suspend fun setLanguage(language: String)

    // Pref #3 — Tamaño de Fuente (12 a 30 sp)
    val fontSize: Flow<Float>
    suspend fun setFontSize(sizeSp: Float)

    // Pref #4 — Omitir Pantalla de Carga (CA-02)
    val skipLoadingScreen: Flow<Boolean>
    suspend fun setSkipLoadingScreen(skip: Boolean)

    // Pref #5 (PC-04) — Permiso / Configuración de Ubicación GPS
    val locationPermissionGranted: Flow<Boolean>
    suspend fun setLocationPermissionGranted(granted: Boolean)

    // Pref #6 — Visibilidad del Teléfono
    val phoneVisible: Flow<Boolean>
    suspend fun setPhoneVisible(visible: Boolean)

    // Pref #7 — Notificaciones Push Activas
    val notificationsActive: Flow<Boolean>
    suspend fun setNotificationsActive(active: Boolean)

    // Pref #8 — Radio de Alertas en metros (500, 1000, 3000)
    val alertRadiusMeters: Flow<Int>
    suspend fun setAlertRadiusMeters(radiusMeters: Int)

    // Pref #9 — Tipos de Incidente permitidos en el filtro
    val incidentTypesFilter: Flow<Set<String>>
    suspend fun setIncidentTypesFilter(types: Set<String>)

    // Pref #10 — Modo No Molestar (activación y rango de horas)
    val doNotDisturb: Flow<DoNotDisturbPrefs>
    suspend fun setDoNotDisturb(prefs: DoNotDisturbPrefs)

    // Pref #13 — Anular Modo Silencioso para SOS
    val overrideSilentModeForSos: Flow<Boolean>
    suspend fun setOverrideSilentModeForSos(override: Boolean)

    // Pref #14 — Contacto de Auxilio Principal
    val primaryAuxiliaryContact: Flow<PrimaryContactPrefs?>
    suspend fun setPrimaryAuxiliaryContact(contact: PrimaryContactPrefs)
    suspend fun clearPrimaryAuxiliaryContact()

    // Pref #15 — Activación SOS por Botón Físico
    //Antohny lo va hacer por chistoso :v
    val sosPhysicalButtonEnabled: Flow<Boolean>
    suspend fun setSosPhysicalButtonEnabled(enabled: Boolean)
}