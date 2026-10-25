package com.limaenaccion.core.data.session

import android.graphics.Bitmap
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
    }

    fun setCurrentUser(user: UsuarioEntity) {
        startTransientSession(user)
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
