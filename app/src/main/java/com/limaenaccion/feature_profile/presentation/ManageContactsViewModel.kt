package com.limaenaccion.feature_profile.presentation

import androidx.lifecycle.ViewModel
import com.limaenaccion.feature_profile.data.model.AuxiliaryContact
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class ManageContactsViewModel : ViewModel() {
    private val _contacts = MutableStateFlow(
        listOf(
            AuxiliaryContact("1", "Rosa Rubio", "Madre", "+51 987 111 222"),
            AuxiliaryContact("2", "Pedro Lozano", "Hermano", "+51 987 333 444")
        )
    )
    val contacts: StateFlow<List<AuxiliaryContact>> = _contacts.asStateFlow()

    fun onDeleteContact(id: String) { _contacts.value = _contacts.value.filterNot { it.id == id } }
}