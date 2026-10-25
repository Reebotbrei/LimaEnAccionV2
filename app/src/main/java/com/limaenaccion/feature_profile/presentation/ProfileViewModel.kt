package com.limaenaccion.feature_profile.presentation

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.limaenaccion.core.data.session.AvatarOption
import com.limaenaccion.core.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch

data class ProfileUiState(
    val fullName: String = "Breider David Catashunga Peña",
    val dni: String = "74881168",
    val district: String = "San Juan de Lurigancho",
    val avatar: AvatarOption = AvatarOption.DEFAULT,
    val photoBitmap: Bitmap? = null
)

class ProfileViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ProfileUiState())
    val uiState: StateFlow<ProfileUiState> = _uiState.asStateFlow()

    init {
        viewModelScope.launch {
            combine(
                SessionManager.currentUser,
                SessionManager.currentAvatar,
                SessionManager.customPhotoBitmap
            ) { user, avatar, bitmap ->
                ProfileUiState(
                    fullName = user?.nombreCompleto ?: "Breider David Catashunga Peña",
                    dni = user?.dni ?: "74881168",
                    district = user?.distrito ?: "San Juan de Lurigancho",
                    avatar = avatar,
                    photoBitmap = bitmap
                )
            }.collect { newState ->
                _uiState.value = newState
            }
        }
    }
}
