package com.limaenaccion.feature_profile.presentation

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.entities.UsuarioEntity
import com.limaenaccion.core.data.preferences.PreferencesRepositoryImpl
import com.limaenaccion.core.data.session.AvatarOption
import com.limaenaccion.core.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ProfileUiState(
    val fullName: String = "",
    val dni: String = "",
    val district: String = "",
    val avatar: AvatarOption = AvatarOption.DEFAULT,
    val photoBitmap: Bitmap? = null
)

class ProfileViewModel(application: Application) : AndroidViewModel(application) {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        // Si el usuario en memoria está vacío al entrar al perfil, restaurarlo solo si hay sesión activa recordada
        if (SessionManager.currentUser.value == null) {
            viewModelScope.launch {
                withContext(Dispatchers.IO) {
                    try {
                        val prefs = PreferencesRepositoryImpl(getApplication())
                        val remember = prefs.rememberSession.first()
                        val uid = prefs.activeSessionUid.first()
                        if (remember && uid.isNotBlank()) {
                            val dao = AppDatabase.getInstance(getApplication()).usuarioDao()
                            var local: UsuarioEntity? = dao.obtenerPorCorreo(uid) ?: dao.obtenerPorDni(uid)
                            if (local == null) {
                                val cache = dao.obtenerUsuario(uid)
                                if (cache != null) {
                                    local = UsuarioEntity(
                                        correo = cache.email,
                                        dni = cache.dni,
                                        nombreCompleto = cache.fullName,
                                        genero = cache.gender,
                                        telefono = cache.phoneNumber,
                                        distrito = cache.district,
                                        urbanizacion = cache.neighborhood,
                                        contrasena = cache.password,
                                        fotoPerfil = cache.profilePhotoUrl
                                    )
                                }
                            }

                            if (local != null) {
                                SessionManager.setCurrentUser(local)
                                Log.d("PROFILE_VIEWMODEL", "✅ Usuario restaurado desde Room: ${local.correo} (${local.nombreCompleto})")
                            }
                        }
                    } catch (e: Exception) {
                        Log.w("PROFILE_VIEWMODEL", "Error restaurando usuario: ${e.message}")
                    }
                }
            }
        }

        viewModelScope.launch {
            combine(
                SessionManager.currentUser,
                SessionManager.currentAvatar,
                SessionManager.customPhotoBitmap
            ) { user, avatar, bitmap ->
                ProfileUiState(
                    fullName = user?.nombreCompleto?.takeIf { it.isNotBlank() } ?: "Ciudadano",
                    dni = user?.dni ?: "",
                    district = user?.distrito?.takeIf { it.isNotBlank() } ?: "Lima",
                    avatar = avatar,
                    photoBitmap = bitmap
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
