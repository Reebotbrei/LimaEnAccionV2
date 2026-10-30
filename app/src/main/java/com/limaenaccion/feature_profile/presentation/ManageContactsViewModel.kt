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
import com.limaenaccion.feature_profile.data.model.AuxiliaryContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageContactsViewModel @JvmOverloads constructor(
    application: Application,
    private val contactoDao: ContactoDao = AppDatabase.getInstance(application).contactoDao(),
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(
        usuarioDao = AppDatabase.getInstance(application).usuarioDao(),
        contactoDao = AppDatabase.getInstance(application).contactoDao()
    )
) : AndroidViewModel(application) {

    private val _contacts = MutableStateFlow<List<AuxiliaryContact>>(emptyList())
    val contacts: StateFlow<List<AuxiliaryContact>> = _contacts.asStateFlow()

    init {
        observeContacts()
        sincronizarDesdeNubeSiVacio()
    }

    private fun sincronizarDesdeNubeSiVacio() {
        viewModelScope.launch(Dispatchers.IO) {
            val count = contactoDao.contarContactos()
            val userEmail = SessionManager.currentUser.value?.correo?.trim()?.lowercase() ?: ""
            if (count == 0 && userEmail.isNotBlank()) {
                Log.d("MANAGE_CONTACTS", "Intentando sincronizar contactos desde Firestore para $userEmail")
                usuarioRepository.sincronizarContactosDesdeNube(userEmail)
            }
        }
    }

    private fun observeContacts() {
        viewModelScope.launch {
            contactoDao.obtenerTodos().collect { entityList ->
                _contacts.value = entityList.map {
                    AuxiliaryContact(
                        id = it.id.toString(),
                        name = it.nombre + if (it.esPrincipal) " ★ (Principal)" else "",
                        relationship = it.parentesco,
                        phoneNumber = it.telefono
                    )
                }
            }
        }
    }

    fun onDeleteContact(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val longId = id.toLongOrNull()
            if (longId != null) {
                contactoDao.eliminarPorId(longId)
            } else {
                _contacts.value = _contacts.value.filterNot { it.id == id }
            }

            // Sincronizar actualización con Firebase y Azure
            val userEmail = SessionManager.currentUser.value?.correo?.trim()?.lowercase() ?: ""
            if (userEmail.isNotBlank()) {
                val remaining = contactoDao.obtenerListaTodos()
                try {
                    usuarioRepository.actualizarContactosAuxilio(userEmail, remaining)
                    Log.d("MANAGE_CONTACTS", "✅ Eliminación reflejada en Firestore")
                } catch (e: Exception) {
                    Log.w("MANAGE_CONTACTS", "Error al actualizar contactos en Firestore: ${e.message}")
                }

                try {
                    RetrofitClient.instance.updateProfile(
                        RegisterRequest(
                            email = userEmail,
                            correo = userEmail,
                            contactosAuxilio = remaining.map {
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
                    Log.d("MANAGE_CONTACTS", "✅ Eliminación reflejada en Azure Backend")
                } catch (e: Exception) {
                    Log.w("MANAGE_CONTACTS", "Error al actualizar contactos en Backend: ${e.message}")
                }
            }
        }
    }
}
