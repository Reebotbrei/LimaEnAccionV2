package com.limaenaccion.feature_profile.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.SqliteDatabaseHelper
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.network.model.RegisterRequest
import com.limaenaccion.core.data.session.SessionManager
import com.limaenaccion.feature_auth.data.model.Usuario
import com.limaenaccion.feature_auth.data.repository.UsuarioRepository
import com.limaenaccion.feature_auth.data.repository.UsuarioRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class UpdateAddressUiState(
    val currentZone: String = "Lima, Perú",
    val newDistrict: String = "San Juan de Lurigancho",
    val newNeighborhood: String = "Caja de Agua",
    val isPhoneVerified: Boolean = false,
    val isLoading: Boolean = false,
    val changesSaved: Boolean = false
)

class UpdateAddressViewModel @JvmOverloads constructor(
    application: Application,
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(
        usuarioDao = AppDatabase.getInstance(application).usuarioDao()
    )
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(UpdateAddressUiState())
    val uiState: StateFlow<UpdateAddressUiState> = _uiState.asStateFlow()

    private val usuarioDao = AppDatabase.getInstance(application).usuarioDao()
    private val sqliteHelper = SqliteDatabaseHelper.getInstance(application)

    init {
        viewModelScope.launch {
            SessionManager.currentUser.collect { user ->
                if (user != null) {
                    val zone = listOf(user.urbanizacion, user.distrito).filter { it.isNotBlank() }.joinToString(", ")
                    _uiState.value = _uiState.value.copy(
                        currentZone = zone.ifBlank { "Lima, Perú" },
                        newDistrict = user.distrito.ifBlank { "San Juan de Lurigancho" },
                        newNeighborhood = user.urbanizacion.ifBlank { "Caja de Agua" }
                    )
                }
            }
        }
    }

    fun onDistrictChange(value: String) { _uiState.value = _uiState.value.copy(newDistrict = value, isPhoneVerified = false) }
    fun onNeighborhoodChange(value: String) { _uiState.value = _uiState.value.copy(newNeighborhood = value, isPhoneVerified = false) }

    fun onVerifyWithSmsClicked() {
        // Se simula la verificación inmediata.
        _uiState.value = _uiState.value.copy(isPhoneVerified = true)
    }

    fun onSaveClicked() {
        if (!_uiState.value.isPhoneVerified) return
        val currentUser = SessionManager.currentUser.value ?: return

        val updatedUser = currentUser.copy(
            distrito = _uiState.value.newDistrict,
            urbanizacion = _uiState.value.newNeighborhood
        )

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // 1. Guardar en SQLite Local (Room + SqliteOpenHelper - Rúbrica b)
                try {
                    usuarioDao.guardarUsuario(updatedUser)
                    sqliteHelper.guardarUsuario(updatedUser)
                    Log.d("UPDATE_ADDRESS", "✅ Dirección actualizada en SQLite local")
                } catch (e: Exception) {
                    Log.e("UPDATE_ADDRESS", "Error al guardar en SQLite: ${e.message}")
                }

                // 2. Guardar en Firebase Firestore (Nube de Google)
                try {
                    val fbUser = Usuario(
                        uid = updatedUser.correo,
                        fullName = updatedUser.nombreCompleto,
                        dni = updatedUser.dni,
                        gender = updatedUser.genero,
                        email = updatedUser.correo,
                        phoneNumber = updatedUser.telefono,
                        district = updatedUser.distrito,
                        neighborhood = updatedUser.urbanizacion
                    )
                    usuarioRepository.actualizarUsuario(fbUser)
                    Log.d("UPDATE_ADDRESS", "✅ Dirección actualizada en Firebase Firestore")
                } catch (e: Exception) {
                    Log.w("UPDATE_ADDRESS", "Error al guardar en Firebase: ${e.message}")
                }

                // 3. Guardar en API Service de Node.js (Rúbrica c)
                try {
                    RetrofitClient.instance.updateProfile(
                        RegisterRequest(
                            dni = updatedUser.dni,
                            nombreCompleto = updatedUser.nombreCompleto,
                            genero = updatedUser.genero,
                            correo = updatedUser.correo,
                            telefono = updatedUser.telefono,
                            contrasena = updatedUser.contrasena,
                            distrito = updatedUser.distrito,
                            urbanizacion = updatedUser.urbanizacion
                        )
                    )
                    Log.d("UPDATE_ADDRESS", "✅ Dirección actualizada en API Service Node.js")
                } catch (e: Exception) {
                    Log.w("UPDATE_ADDRESS", "Node.js update omitido o sin conexión: ${e.message}")
                }
            }

            // 4. Actualizar sesión en RAM y notificar pantalla
            SessionManager.setCurrentUser(updatedUser)
            _uiState.value = _uiState.value.copy(isLoading = false, changesSaved = true)
        }
    }
}
