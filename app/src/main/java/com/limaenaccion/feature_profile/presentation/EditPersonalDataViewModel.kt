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

data class EditPersonalDataUiState(
    val fullName: String = "",
    val dni: String = "",
    val gender: String = "Masculino",
    val email: String = "",
    val phoneNumber: String = "",
    val isLoading: Boolean = false,
    val savedChanges: Boolean = false
)

class EditPersonalDataViewModel @JvmOverloads constructor(
    application: Application,
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(
        usuarioDao = AppDatabase.getInstance(application).usuarioDao()
    )
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(EditPersonalDataUiState())
    val uiState: StateFlow<EditPersonalDataUiState> = _uiState.asStateFlow()

    private val usuarioDao = AppDatabase.getInstance(application).usuarioDao()
    private val sqliteHelper = SqliteDatabaseHelper.getInstance(application)

    init {
        viewModelScope.launch {
            SessionManager.currentUser.collect { user ->
                if (user != null) {
                    _uiState.value = _uiState.value.copy(
                        fullName = user.nombreCompleto,
                        dni = user.dni,
                        gender = user.genero.ifBlank { "Masculino" },
                        email = user.correo,
                        phoneNumber = if (user.telefono.isNotBlank() && !user.telefono.startsWith("+51")) {
                            "+51 ${user.telefono}"
                        } else {
                            user.telefono
                        }
                    )
                }
            }
        }
    }

    fun onFullNameChange(v: String) { _uiState.value = _uiState.value.copy(fullName = v) }
    fun onGenderChange(v: String) { _uiState.value = _uiState.value.copy(gender = v) }
    fun onEmailChange(v: String) { _uiState.value = _uiState.value.copy(email = v) }
    fun onPhoneNumberChange(v: String) { _uiState.value = _uiState.value.copy(phoneNumber = v) }

    fun onSaveClicked() {
        val currentUser = SessionManager.currentUser.value ?: return
        val phoneClean = _uiState.value.phoneNumber.replace("+51", "").trim()
        val updatedUser = currentUser.copy(
            nombreCompleto = _uiState.value.fullName.trim(),
            genero = _uiState.value.gender,
            correo = _uiState.value.email.trim(),
            telefono = phoneClean
        )

        _uiState.value = _uiState.value.copy(isLoading = true)

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                // 1. Guardar en SQLite Local (Room + SqliteOpenHelper - Rúbrica b)
                try {
                    usuarioDao.guardarUsuario(updatedUser)
                    sqliteHelper.guardarUsuario(updatedUser)
                    Log.d("EDIT_PROFILE", "✅ Guardado en SQLite local")
                } catch (e: Exception) {
                    Log.e("EDIT_PROFILE", "Error al guardar en SQLite: ${e.message}")
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
                        neighborhood = updatedUser.urbanizacion,
                        profilePhotoUrl = updatedUser.fotoPerfil
                    )
                    usuarioRepository.actualizarUsuario(fbUser)
                    Log.d("EDIT_PROFILE", "✅ Guardado en Firebase Firestore")
                } catch (e: Exception) {
                    Log.w("EDIT_PROFILE", "Error al guardar en Firebase: ${e.message}")
                }

                // 3. Guardar en API Service de Node.js (Rúbrica c)
                try {
                    RetrofitClient.instance.updateProfile(
                        RegisterRequest(
                            fullName = updatedUser.nombreCompleto,
                            nombreCompleto = updatedUser.nombreCompleto,
                            dni = updatedUser.dni,
                            gender = updatedUser.genero,
                            genero = updatedUser.genero,
                            email = updatedUser.correo,
                            correo = updatedUser.correo,
                            phoneNumber = updatedUser.telefono,
                            telefono = updatedUser.telefono,
                            contrasena = updatedUser.contrasena,
                            password = updatedUser.contrasena,
                            distrito = updatedUser.distrito,
                            district = updatedUser.distrito,
                            urbanizacion = updatedUser.urbanizacion,
                            neighborhood = updatedUser.urbanizacion,
                            fotoPerfil = updatedUser.fotoPerfil,
                            profilePhotoUrl = updatedUser.fotoPerfil
                        )
                    )
                    Log.d("EDIT_PROFILE", "✅ Guardado en API Service Node.js")
                } catch (e: Exception) {
                    Log.w("EDIT_PROFILE", "Node.js update omitido o sin conexión: ${e.message}")
                }
            }

            // 4. Actualizar sesión en RAM y notificar pantalla
            SessionManager.setCurrentUser(updatedUser)
            _uiState.value = _uiState.value.copy(isLoading = false, savedChanges = true)
        }
    }
}
