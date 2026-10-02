package com.limaenaccion.feature_profile.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.CameraAlt
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.limaenaccion.core.ui.components.PrimaryButton

private val avatarColors = mapOf(
    AvatarOption.DEFAULT to Color(0xFF8B5CF6),
    AvatarOption.AVATAR_1 to Color(0xFF8B5CF6),
    AvatarOption.AVATAR_2 to Color(0xFF6366F1),
    AvatarOption.AVATAR_3 to Color(0xFFEC4899)
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChangeProfilePhotoScreen(
    onNavigateBack: () -> Unit,
    viewModel: ChangeProfilePhotoViewModel = viewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    LaunchedEffect(uiState.savedPhoto) { if (uiState.savedPhoto) onNavigateBack() }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Cambiar foto de perfil") },
                navigationIcon = { IconButton(onClick = onNavigateBack) { Icon(Icons.Filled.ArrowBack, contentDescription = "Volver") } }
            )
        },
        bottomBar = { PrimaryButton(text = "Guardar foto", onClick = viewModel::onSaveClicked, modifier = Modifier.padding(16.dp)) }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize().padding(paddingValues).padding(24.dp), horizontalAlignment = Alignment.CenterHorizontally) {
            Box(modifier = Modifier.size(96.dp).clip(CircleShape).background(avatarColors.getValue(uiState.selectedAvatar)), contentAlignment = Alignment.Center) {
                Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(48.dp))
            }
            Spacer(modifier = Modifier.height(24.dp))
            Text("AVATARES DISPONIBLES", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
            Spacer(modifier = Modifier.height(12.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(16.dp)) {
                listOf(AvatarOption.AVATAR_1, AvatarOption.AVATAR_2, AvatarOption.AVATAR_3).forEach { avatar ->
                    val selected = uiState.selectedAvatar == avatar
                    Box(
                        modifier = Modifier
                            .size(56.dp)
                            .clip(CircleShape)
                            .background(avatarColors.getValue(avatar))
                            .then(if (selected) Modifier.border(2.dp, MaterialTheme.colorScheme.primary, CircleShape) else Modifier)
                            .clickable { viewModel.onAvatarSelected(avatar) },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(Icons.Filled.Person, contentDescription = null, tint = Color.White, modifier = Modifier.size(28.dp))
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
            Surface(
                shape = RoundedCornerShape(50),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                onClick = { }
            ) {
                Row(modifier = Modifier.padding(horizontal = 20.dp, vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.CameraAlt, contentDescription = null, tint = MaterialTheme.colorScheme.primary, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Tomar foto o subir desde galería", color = MaterialTheme.colorScheme.primary)
                }
            }
        }
    }
}