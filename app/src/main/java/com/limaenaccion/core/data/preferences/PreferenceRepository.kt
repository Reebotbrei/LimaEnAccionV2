package com.limaenaccion.core.data.preferences

import kotlinx.coroutines.flow.Flow

interface PreferencesRepository {

    // Pref #1 — Tema ("Sistema", "Claro", "Oscuro", "Daltonico")
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

    // Pref #15 — Activación SOS por Botón Físico
    val sosPhysicalButtonEnabled: Flow<Boolean>
    suspend fun setSosPhysicalButtonEnabled(enabled: Boolean)

    // Pref #16 — Eliminación de Datos / Preferencias (Ley N° 29733)
    suspend fun clearAllPreferences()
}
