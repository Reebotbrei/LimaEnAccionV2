package com.limaenaccion.core.data.preferences

import kotlinx.coroutines.flow.Flow

// Pref #14 - Contacto de Auxilio Principal
data class PrimaryContactPrefs(
    val name: String,
    val phoneNumber: String
)

interface PreferencesRepository {

    // Pref #4 — Omitir Pantalla de Carga (CA-02)
    val skipLoadingScreen: Flow<Boolean>
    suspend fun setSkipLoadingScreen(skip: Boolean)

    // Pref #14 — Contacto de Auxilio Principal
    val primaryAuxiliaryContact: Flow<PrimaryContactPrefs?>
    suspend fun setPrimaryAuxiliaryContact(contact: PrimaryContactPrefs)
    suspend fun clearPrimaryAuxiliaryContact()

    // Pref #15 — Activación SOS por Botón Físico.
    // (No implementado todavía, Anthony lo va a hacer por chistoso :v)
    val sosPhysicalButtonEnabled: Flow<Boolean>
    suspend fun setSosPhysicalButtonEnabled(enabled: Boolean)
}