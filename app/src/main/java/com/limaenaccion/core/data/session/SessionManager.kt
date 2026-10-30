package com.limaenaccion.core.data.session

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import com.limaenaccion.core.data.local.entities.UsuarioEntity
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AvatarOption {
    DEFAULT,
    AVATAR_1,
    AVATAR_2,
    AVATAR_3
}

/**
 * Gestor de sesión efímera en memoria RAM ("No recordar sesión").
 *
 * Por requerimiento de seguridad y rúbrica, la sesión del usuario activo NUNCA se persiste
 * ni en SharedPreferences ni en SQLite. Al cerrarse o reiniciarse la aplicación,
 * la sesión queda limpia y el usuario siempre debe autenticarse nuevamente.
 */
object SessionManager {

    private val _currentUser = MutableStateFlow<UsuarioEntity?>(null)
    val currentUser: StateFlow<UsuarioEntity?> = _currentUser.asStateFlow()

    private val _currentAvatar = MutableStateFlow(AvatarOption.DEFAULT)
    val currentAvatar: StateFlow<AvatarOption> = _currentAvatar.asStateFlow()

    private val _customPhotoBitmap = MutableStateFlow<Bitmap?>(null)
    val customPhotoBitmap: StateFlow<Bitmap?> = _customPhotoBitmap.asStateFlow()

    val isAuthenticatedInMemory: Boolean
        get() = _currentUser.value != null

    fun startTransientSession(user: UsuarioEntity) {
        _currentUser.value = user
        if (user.fotoPerfil.isNotBlank()) {
            applyProfilePhoto(user.fotoPerfil)
        }
    }

    fun setCurrentUser(user: UsuarioEntity) {
        startTransientSession(user)
    }

    fun applyProfilePhoto(photoUrl: String) {
        if (photoUrl.isBlank()) {
            _currentAvatar.value = AvatarOption.DEFAULT
            _customPhotoBitmap.value = null
            return
        }
        if (photoUrl.startsWith("avatar:")) {
            val avatarName = photoUrl.removePrefix("avatar:")
            val option = try { AvatarOption.valueOf(avatarName) } catch (_: Exception) { AvatarOption.DEFAULT }
            _currentAvatar.value = option
            _customPhotoBitmap.value = null
        } else if (photoUrl.startsWith("data:image") || photoUrl.length > 50) {
            val clean = if (photoUrl.contains(",")) photoUrl.substringAfter(",") else photoUrl
            try {
                val bytes = Base64.decode(clean, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)
                if (bitmap != null) {
                    _currentAvatar.value = AvatarOption.DEFAULT
                    _customPhotoBitmap.value = bitmap
                }
            } catch (e: Exception) {
                Log.w("SESSION_MANAGER", "Error al decodificar foto de perfil Base64: ${e.message}")
            }
        }
    }

    fun updateProfilePhoto(avatar: AvatarOption, bitmap: Bitmap? = null) {
        _currentAvatar.value = avatar
        _customPhotoBitmap.value = bitmap
    }

    fun clearSession() {
        _currentUser.value = null
        _currentAvatar.value = AvatarOption.DEFAULT
        _customPhotoBitmap.value = null
    }
}
