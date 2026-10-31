package com.limaenaccion.feature_profile.presentation

import android.app.Application
import android.graphics.Bitmap
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.SqliteDatabaseHelper
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.network.model.RegisterRequest
import com.limaenaccion.core.data.session.AvatarOption
import com.limaenaccion.core.data.session.SessionManager
import com.limaenaccion.core.util.ImageCompressor
import com.limaenaccion.feature_auth.data.model.Usuario
import com.limaenaccion.feature_auth.data.repository.UsuarioRepository
import com.limaenaccion.feature_auth.data.repository.UsuarioRepositoryImpl
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class ChangeProfilePhotoUiState(
    val selectedAvatar: AvatarOption = SessionManager.currentAvatar.value,
    val customPhoto: Bitmap? = SessionManager.customPhotoBitmap.value,
    val savedPhoto: Boolean = false,
    val isSaving: Boolean = false
)

class ChangeProfilePhotoViewModel @JvmOverloads constructor(
    application: Application,
    private val usuarioRepository: UsuarioRepository = UsuarioRepositoryImpl(
        usuarioDao = AppDatabase.getInstance(application).usuarioDao(),
        contactoDao = AppDatabase.getInstance(application).contactoDao()
    )
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow(ChangeProfilePhotoUiState())
    val uiState: StateFlow<ChangeProfilePhotoUiState> = _uiState.asStateFlow()

    private val usuarioDao = AppDatabase.getInstance(application).usuarioDao()
    private val sqliteHelper = SqliteDatabaseHelper.getInstance(application)

    fun onAvatarSelected(avatar: AvatarOption) {
        _uiState.value = _uiState.value.copy(
            selectedAvatar = avatar,
            customPhoto = null
        )
    }

    fun onCustomPhotoSelected(bitmap: Bitmap) {
        _uiState.value = _uiState.value.copy(
            customPhoto = bitmap
        )
    }

    fun onSaveClicked() {
        if (_uiState.value.isSaving) return
        val state = _uiState.value
        _uiState.value = state.copy(isSaving = true)

        val photoString = if (state.customPhoto != null) {
            ImageCompressor.toBase64String(state.customPhoto)
        } else {
            "avatar:${state.selectedAvatar.name}"
        }

        SessionManager.updateProfilePhoto(
            avatar = state.selectedAvatar,
            bitmap = state.customPhoto
        )

        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                var currentUser = SessionManager.currentUser.value
                if (currentUser == null) {
                    currentUser = usuarioDao.obtenerUltimoUsuarioRegistrado()
                }

                if (currentUser != null) {
                    val updatedUser = currentUser.copy(fotoPerfil = photoString)
                    SessionManager.setCurrentUser(updatedUser)

                    // 1. SQLite Local (Room y SqliteOpenHelper - Rúbrica b)
                    try {
                        usuarioDao.guardarUsuario(updatedUser)
                        sqliteHelper.guardarUsuario(updatedUser)
                        Log.d("PROFILE_PHOTO", "✅ Foto de perfil guardada en SQLite local")
                    } catch (e: Exception) {
                        Log.e("PROFILE_PHOTO", "Error guardando foto en SQLite: ${e.message}")
                    }

                    // 2. Firebase Cloud Firestore y Backend Node.js
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
                            profilePhotoUrl = photoString
                        )
                        usuarioRepository.actualizarUsuario(fbUser)
                        Log.d("PROFILE_PHOTO", "✅ Foto de perfil sincronizada con Firebase y Backend")
                    } catch (e: Exception) {
                        Log.w("PROFILE_PHOTO", "Error guardando foto en Firestore: ${e.message}")
                    }

                    // 3. Fallback directo al Backend Node.js / Azure
                    try {
                        RetrofitClient.instance.updateProfile(
                            RegisterRequest(
                                id = updatedUser.correo,
                                email = updatedUser.correo,
                                correo = updatedUser.correo,
                                dni = updatedUser.dni,
                                fullName = updatedUser.nombreCompleto,
                                nombreCompleto = updatedUser.nombreCompleto,
                                fotoPerfil = photoString,
                                profilePhotoUrl = photoString
                            )
                        )
                        Log.d("PROFILE_PHOTO", "✅ Foto de perfil guardada en Backend Azure")
                    } catch (e: Exception) {
                        Log.w("PROFILE_PHOTO", "Error guardando foto en Backend Azure: ${e.message}")
                    }
                }
            }

            // Esperar que termine el guardado antes de avisar a la UI para volver atrás
            _uiState.value = _uiState.value.copy(savedPhoto = true, isSaving = false)
        }
    }
}
