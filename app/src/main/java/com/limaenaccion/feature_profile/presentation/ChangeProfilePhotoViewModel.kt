package com.limaenaccion.feature_profile.presentation

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

enum class AvatarOption { DEFAULT, AVATAR_1, AVATAR_2, AVATAR_3 }

data class ChangeProfilePhotoUiState(
    val selectedAvatar: AvatarOption = AvatarOption.DEFAULT,
    val savedPhoto: Boolean = false
)

class ChangeProfilePhotoViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(ChangeProfilePhotoUiState())
    val uiState: StateFlow<ChangeProfilePhotoUiState> = _uiState.asStateFlow()

    fun onAvatarSelected(avatar: AvatarOption) { _uiState.value = _uiState.value.copy(selectedAvatar = avatar) }

    fun onSaveClicked() {
        // Solo confirmacion visual.
        _uiState.value = _uiState.value.copy(savedPhoto = true)
    }
}