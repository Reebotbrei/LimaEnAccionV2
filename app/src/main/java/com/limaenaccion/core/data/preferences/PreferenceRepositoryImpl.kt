package com.limaenaccion.core.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.floatPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "lima_en_accion_prefs")

class PreferencesRepositoryImpl(private val context: Context) : PreferencesRepository {

    private object Keys {
        val TEMA = stringPreferencesKey("tema")
        val IDIOMA = stringPreferencesKey("idioma")
        val TAMANO_FUENTE = floatPreferencesKey("tamano_fuente")
        val SKIP_LOADING_SCREEN = booleanPreferencesKey("skip_loading_screen")
        val UBICACION_PERMITIDA = booleanPreferencesKey("ubicacion_permitida")
        val TELEFONO_VISIBLE = booleanPreferencesKey("telefono_visible")
        val NOTIFICACIONES_ACTIVAS = booleanPreferencesKey("notificaciones_activas")
        val RADIO_ALERTAS_METROS = intPreferencesKey("radio_alertas_metros")
        val TIPOS_INCIDENTE = stringSetPreferencesKey("tipos_incidente")
        val NO_MOLESTAR = booleanPreferencesKey("no_molestar")
        val HORA_INICIO = intPreferencesKey("hora_inicio")
        val HORA_FIN = intPreferencesKey("hora_fin")
        val ANULAR_SILENCIO_SOS = booleanPreferencesKey("anular_silencio_sos")
        val PRIMARY_CONTACT_NAME = stringPreferencesKey("primary_contact_name")
        val PRIMARY_CONTACT_PHONE = stringPreferencesKey("primary_contact_phone")
        val SOS_PHYSICAL_BUTTON_ENABLED = booleanPreferencesKey("sos_physical_button_enabled")
    }

    override val theme: Flow<String> =
        context.dataStore.data.map { prefs -> prefs[Keys.TEMA] ?: "Sistema" }

    override suspend fun setTheme(theme: String) {
        context.dataStore.edit { prefs -> prefs[Keys.TEMA] = theme }
    }

    override val language: Flow<String> =
        context.dataStore.data.map { prefs -> prefs[Keys.IDIOMA] ?: "ES" }

    override suspend fun setLanguage(language: String) {
        context.dataStore.edit { prefs -> prefs[Keys.IDIOMA] = language }
    }

    override val fontSize: Flow<Float> =
        context.dataStore.data.map { prefs -> prefs[Keys.TAMANO_FUENTE] ?: 16f }

    override suspend fun setFontSize(sizeSp: Float) {
        context.dataStore.edit { prefs -> prefs[Keys.TAMANO_FUENTE] = sizeSp }
    }

    override val skipLoadingScreen: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.SKIP_LOADING_SCREEN] ?: false }

    override suspend fun setSkipLoadingScreen(skip: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SKIP_LOADING_SCREEN] = skip }
    }

    override val locationPermissionGranted: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.UBICACION_PERMITIDA] ?: false }

    override suspend fun setLocationPermissionGranted(granted: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.UBICACION_PERMITIDA] = granted }
    }

    override val phoneVisible: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.TELEFONO_VISIBLE] ?: true }

    override suspend fun setPhoneVisible(visible: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.TELEFONO_VISIBLE] = visible }
    }

    override val notificationsActive: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.NOTIFICACIONES_ACTIVAS] ?: true }

    override suspend fun setNotificationsActive(active: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.NOTIFICACIONES_ACTIVAS] = active }
    }

    override val alertRadiusMeters: Flow<Int> =
        context.dataStore.data.map { prefs -> prefs[Keys.RADIO_ALERTAS_METROS] ?: 1000 }

    override suspend fun setAlertRadiusMeters(radiusMeters: Int) {
        context.dataStore.edit { prefs -> prefs[Keys.RADIO_ALERTAS_METROS] = radiusMeters }
    }

    override val incidentTypesFilter: Flow<Set<String>> =
        context.dataStore.data.map { prefs ->
            prefs[Keys.TIPOS_INCIDENTE] ?: setOf("ROBO", "SOSPECHOSO", "EMERGENCIA_MEDICA", "INCENDIO", "ACCIDENTE", "VANDALISMO")
        }

    override suspend fun setIncidentTypesFilter(types: Set<String>) {
        context.dataStore.edit { prefs -> prefs[Keys.TIPOS_INCIDENTE] = types }
    }

    override val doNotDisturb: Flow<DoNotDisturbPrefs> =
        context.dataStore.data.map { prefs ->
            DoNotDisturbPrefs(
                enabled = prefs[Keys.NO_MOLESTAR] ?: false,
                startHour = prefs[Keys.HORA_INICIO] ?: 22,
                endHour = prefs[Keys.HORA_FIN] ?: 7
            )
        }

    override suspend fun setDoNotDisturb(prefs: DoNotDisturbPrefs) {
        context.dataStore.edit { p ->
            p[Keys.NO_MOLESTAR] = prefs.enabled
            p[Keys.HORA_INICIO] = prefs.startHour
            p[Keys.HORA_FIN] = prefs.endHour
        }
    }

    override val overrideSilentModeForSos: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.ANULAR_SILENCIO_SOS] ?: true }

    override suspend fun setOverrideSilentModeForSos(override: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.ANULAR_SILENCIO_SOS] = override }
    }

    override val primaryAuxiliaryContact: Flow<PrimaryContactPrefs?> =
        context.dataStore.data.map { prefs ->
            val name = prefs[Keys.PRIMARY_CONTACT_NAME]
            val phone = prefs[Keys.PRIMARY_CONTACT_PHONE]
            if (name != null && phone != null) PrimaryContactPrefs(name, phone) else null
        }

    override suspend fun setPrimaryAuxiliaryContact(contact: PrimaryContactPrefs) {
        context.dataStore.edit { prefs ->
            prefs[Keys.PRIMARY_CONTACT_NAME] = contact.name
            prefs[Keys.PRIMARY_CONTACT_PHONE] = contact.phoneNumber
        }
    }

    override suspend fun clearPrimaryAuxiliaryContact() {
        context.dataStore.edit { prefs ->
            prefs.remove(Keys.PRIMARY_CONTACT_NAME)
            prefs.remove(Keys.PRIMARY_CONTACT_PHONE)
        }
    }

    override val sosPhysicalButtonEnabled: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.SOS_PHYSICAL_BUTTON_ENABLED] ?: false }

    override suspend fun setSosPhysicalButtonEnabled(enabled: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SOS_PHYSICAL_BUTTON_ENABLED] = enabled }
    }
}