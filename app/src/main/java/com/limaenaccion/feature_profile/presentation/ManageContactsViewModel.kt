package com.limaenaccion.feature_profile.presentation

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.dao.ContactoDao
import com.limaenaccion.core.data.local.entities.ContactoEntity
import com.limaenaccion.feature_profile.data.model.AuxiliaryContact
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ManageContactsViewModel @JvmOverloads constructor(
    application: Application,
    private val contactoDao: ContactoDao = AppDatabase.getInstance(application).contactoDao()
) : AndroidViewModel(application) {

    private val _contacts = MutableStateFlow<List<AuxiliaryContact>>(emptyList())
    val contacts: StateFlow<List<AuxiliaryContact>> = _contacts.asStateFlow()

    init {
        observeContacts()
    }

    private fun observeContacts() {
        viewModelScope.launch {
            contactoDao.obtenerTodos().collect { entityList ->
                if (entityList.isEmpty()) {
                    withContext(Dispatchers.IO) {
                        val count = contactoDao.contarContactos()
                        if (count == 0) {
                            contactoDao.guardarContacto(
                                ContactoEntity(nombre = "Rosa Rubio", telefono = "+51 987 111 222", parentesco = "Madre", esPrincipal = true)
                            )
                            contactoDao.guardarContacto(
                                ContactoEntity(nombre = "Pedro Lozano", telefono = "+51 987 333 444", parentesco = "Hermano", esPrincipal = false)
                            )
                        }
                    }
                } else {
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
    }

    fun onDeleteContact(id: String) {
        viewModelScope.launch(Dispatchers.IO) {
            val longId = id.toLongOrNull()
            if (longId != null) {
                contactoDao.eliminarPorId(longId)
            } else {
                _contacts.value = _contacts.value.filterNot { it.id == id }
            }
        }
    }
}
