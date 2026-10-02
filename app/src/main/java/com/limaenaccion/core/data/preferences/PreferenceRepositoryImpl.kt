package com.limaenaccion.core.data.preferences

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore by preferencesDataStore(name = "lima_en_accion_prefs")

class PreferencesRepositoryImpl(private val context: Context) : PreferencesRepository {

    private object Keys {
        val SKIP_LOADING_SCREEN = booleanPreferencesKey("skip_loading_screen")
        val PRIMARY_CONTACT_NAME = stringPreferencesKey("primary_contact_name")
        val PRIMARY_CONTACT_PHONE = stringPreferencesKey("primary_contact_phone")
        val SOS_PHYSICAL_BUTTON_ENABLED = booleanPreferencesKey("sos_physical_button_enabled")
    }

    override val skipLoadingScreen: Flow<Boolean> =
        context.dataStore.data.map { prefs -> prefs[Keys.SKIP_LOADING_SCREEN] ?: false }

    override suspend fun setSkipLoadingScreen(skip: Boolean) {
        context.dataStore.edit { prefs -> prefs[Keys.SKIP_LOADING_SCREEN] = skip }
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