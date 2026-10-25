package com.limaenaccion.feature_profile.presentation

import android.graphics.Bitmap
import androidx.lifecycle.ViewModel
import com.limaenaccion.core.data.session.AvatarOption
import com.limaenaccion.core.data.session.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class ChangeProfilePhotoUiState(
    val selectedAvatar: AvatarOption = SessionManager.currentAvatar.value,
    val customPhoto: Bitmap? = SessionManager.customPhotoBitmap.value,
    val savedPhoto: Boolean = false
)

class ChangeProfilePhotoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChangeProfilePhotoUiState())
    val uiState: StateFlow<ChangeProfilePhotoUiState> = _uiState.asStateFlow()

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
        val state = _uiState.value
        SessionManager.updateProfilePhoto(
            avatar = state.selectedAvatar,
            bitmap = state.customPhoto
        )
        _uiState.value = state.copy(savedPhoto = true)
    }
}
