package com.limaenaccion.feature_profile.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.dao.ContactoDao
import com.limaenaccion.core.data.local.entities.ContactoEntity
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.network.model.ContactoDto
import com.limaenaccion.core.data.network.model.RegisterRequest
import com.limaenaccion.core.data.session.SessionManager
import com.limaenaccion.feature_auth.data.repository.UsuarioRepository
import com.limaenaccion.feature_auth.data.repository.UsuarioRepositoryImpl
import com.limaenaccion.feature_profile.data.model.ContactRelationship
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class AddAuxiliaryContactUiState(
    val fullName: String = "",
    val relationship: ContactRelationship = ContactRelationship.FAMILIAR,
    val phoneNumber: String = "",
    val isPrimaryContact: Boolean = true,
    val contactSaved: Boolean = false,
    val maxContactsReached: Boolean = false,
    val errorMessage: String? = null
) {
    // RNF / Criterio de aceptación: Nombre válido y número móvil peruano de 9 dígitos iniciando en 9
    val isFormValid: Boolean
        get() = fullName.trim().length >= 3 && phoneNumber.length == 9 && phoneNumber.startsWith("9")
}

class AddAuxiliaryContactViewModel @JvmOverloads constructor(
    application: Application,
    private val contactoDao: ContactoDao = AppDatabase.getInstance(application).contactoDao(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(
        usuarioDao = AppDatabase.getInstance(application).usuarioDao(),
        contactoDao = AppDatabase.getInstance(application).contactoDao()
    )
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(AddAuxiliaryContactUiState())
    val uiState: StateFlow<AddAuxiliaryContactUiState> = _uiState.asStateFlow()

    init {
        checkContactLimit()
    }

    private fun checkContactLimit() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = contactoDao.contarContactos()
            if (count >= 3) {
                withContext(Dispatchers.Main) {
                    _uiState.value = _uiState.value.copy(
                        maxContactsReached = true,
                        errorMessage = "Has alcanzado el límite de 3 contactos de confianza (Pref #12)."
                    )
                }
            }
        }
    }

    fun onFullNameChange(value: String) {
        _uiState.value = _uiState.value.copy(fullName = value, errorMessage = null)
    }

    fun onRelationshipChange(value: ContactRelationship) {
        _uiState.value = _uiState.value.copy(relationship = value)
    }

    fun onPhoneNumberChange(value: String) {
        val digitsOnly = value.filter { it.isDigit() }.take(9) // celular 9 dígitos
        _uiState.value = _uiState.value.copy(phoneNumber = digitsOnly, errorMessage = null)
    }

    fun onIsPrimaryContactChange(value: Boolean) {
        _uiState.value = _uiState.value.copy(isPrimaryContact = value)
    }

    fun onSaveClicked() {
        val state = _uiState.value
        if (!state.isFormValid || state.maxContactsReached) return

        viewModelScope.launch(Dispatchers.IO) {
            val count = contactoDao.contarContactos()
            if (count >= 3) {
                withContext(Dispatchers.Main) {
                    _uiState.value = state.copy(
                        maxContactsReached = true,
                        errorMessage = "Límite alcanzado: máximo 3 contactos de auxilio."
                    )
                }
                return@launch
            }

            val currentUser = SessionManager.currentUser.value
            val userEmail = currentUser?.correo?.trim()?.lowercase() ?: ""

            val entity = ContactoEntity(
                nombre = state.fullName.trim(),
                telefono = state.phoneNumber.trim(),
                parentesco = state.relationship.label,
                esPrincipal = state.isPrimaryContact,
                correoUsuario = userEmail
            )

            // 1. Persistencia local en Room (SQLite)
            if (state.isPrimaryContact) {
                contactoDao.desmarcarPrincipales()
            }
            contactoDao.guardarContacto(entity)

            // 2. Sincronización en Firebase Cloud Firestore y Azure Backend
            val allContacts = contactoDao.obtenerListaTodos()
            if (userEmail.isNotBlank()) {
                try {
                    usuarioRepository.actualizarContactosAuxilio(userEmail, allContacts)
                    Log.d("ADD_CONTACT", "✅ Contacto guardado y sincronizado con Firebase Firestore")
                } catch (e: Exception) {
                    Log.w("ADD_CONTACT", "Error al sincronizar con Firestore: ${e.message}")
                }

                try {
                    RetrofitClient.instance.updateProfile(
                        RegisterRequest(
                            email = userEmail,
                            correo = userEmail,
                            contactosAuxilio = allContacts.map {
                                ContactoDto(
                                    id = it.id.toString(),
                                    nombre = it.nombre,
                                    telefono = it.telefono,
                                    parentesco = it.parentesco,
                                    esPrincipal = it.esPrincipal
                                )
                            }
                        )
                    )
                    Log.d("ADD_CONTACT", "✅ Contacto guardado y sincronizado con Azure Backend")
                } catch (e: Exception) {
                    Log.w("ADD_CONTACT", "Error al sincronizar con Backend: ${e.message}")
                }
            }

            withContext(Dispatchers.Main) {
                _uiState.value = state.copy(contactSaved = true)
            }
        }
    }
}
