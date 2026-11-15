package com.limaenaccion.feature_reports.presentation

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.limaenaccion.core.ui.components.PrimaryButton
import com.limaenaccion.core.util.LocalAppStrings
import com.limaenaccion.feature_reports.data.model.ReportTimeframe
import com.limaenaccion.feature_reports.data.model.ReportType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ReportDetailsScreen(
    viewModel: ReportViewModel,
    onNavigateBack: () -> Unit,
    onTakePhoto: () -> Unit,
    onPublished: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val strings = LocalAppStrings.current
    val scrollState = rememberScrollState()
    val context = LocalContext.current

    // Lanzador directo a la cámara real de Android
    val cameraLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.TakePicturePreview()
    ) { bitmap ->
        if (bitmap != null) {
            viewModel.onPhotoCaptured(bitmap)
        }
    }

    // Permiso de cámara en tiempo de ejecución
    val cameraPermissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            try {
                cameraLauncher.launch(null)
            } catch (e: Exception) {
                Log.e("REPORT_PHOTO", "Error abriendo cámara tras conceder permiso: ${e.message}")
                android.widget.Toast.makeText(context, "No se pudo abrir la cámara. Puedes usar la Galería.", android.widget.Toast.LENGTH_SHORT).show()
            }
        } else {
            android.widget.Toast.makeText(context, "Se requiere permiso de cámara para tomar fotos de evidencia.", android.widget.Toast.LENGTH_SHORT).show()
        }
    }

    // Selector de fotos desde la galería multimedia
    val galleryLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            try {
                val bitmap = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                    ImageDecoder.decodeBitmap(ImageDecoder.createSource(context.contentResolver, uri)) { decoder, _, _ ->
                        decoder.isMutableRequired = true
                        decoder.allocator = ImageDecoder.ALLOCATOR_SOFTWARE
                    }
                } else {
                    @Suppress("DEPRECATION")
                    MediaStore.Images.Media.getBitmap(context.contentResolver, uri)
                }
                viewModel.onPhotoCaptured(bitmap)
            } catch (e: Exception) {
                Log.e("REPORT_PHOTO", "Error cargando foto de galería: ${e.message}")
            }
        }
    }

    LaunchedEffect(uiState.published) {
        if (uiState.published) onPublished()
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Detalles del reporte") },
                navigationIcon = {
                    IconButton(onClick = onNavigateBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                shadowElevation = 8.dp,
                color = MaterialTheme.colorScheme.surface
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    PrimaryButton(
                        text = if (uiState.isPublishing) "Publicando en red vecinal..." else strings.reportQuickPublishBtn,
                        onClick = viewModel::onPublishClicked,
                        enabled = uiState.canPublish && !uiState.isPublishing
                    )
                }
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .verticalScroll(scrollState)
                .padding(horizontal = 16.dp, vertical = 8.dp)
        ) {
            // AVISO DE SEGURIDAD CRÍTICO (No te expongas)
            Surface(
                color = Color(0xFFFEF3C7),
                shape = RoundedCornerShape(12.dp),
                border = BorderStroke(1.dp, Color(0xFFF59E0B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Shield,
                        contentDescription = null,
                        tint = Color(0xFFB45309),
                        modifier = Modifier.size(22.dp)
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        strings.reportSafetyNotice,
                        style = MaterialTheme.typography.bodySmall,
                        color = Color(0xFF78350F),
                        fontWeight = FontWeight.Medium
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // 1. ¿QUÉ OCURRIÓ? (Categoría Seleccionada)
            val type = uiState.selectedType ?: ReportType.ROBO
            Text(
                strings.reportCategoryTitle,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = type.color().copy(alpha = 0.1f),
                border = BorderStroke(1.dp, type.color().copy(alpha = 0.5f)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier.padding(12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(type.color().copy(alpha = 0.2f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(type.icon(), contentDescription = null, tint = type.color(), modifier = Modifier.size(20.dp))
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(type.label, style = MaterialTheme.typography.titleSmall, fontWeight = FontWeight.Bold, color = type.color())
                        Text("Categoría seleccionada", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                    TextButton(onClick = onNavigateBack) {
                        Text("Cambiar", style = MaterialTheme.typography.labelMedium)
                    }
                }
            }

            // Guía cívica y ética para Actividad Inusual
            if (type == ReportType.SOSPECHOSO) {
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = MaterialTheme.colorScheme.secondaryContainer.copy(alpha = 0.4f),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(modifier = Modifier.padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Filled.Info, contentDescription = null, tint = MaterialTheme.colorScheme.secondary, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            strings.reportGuidanceUnusual,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 2. ¿CUÁNDO OCURRIÓ? (Ahora vs Pasado)
            Text(
                strings.reportTimeframeTitle,
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(6.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                ReportTimeframe.entries.forEach { tf ->
                    val isSelected = uiState.timeframe == tf
                    Surface(
                        shape = RoundedCornerShape(12.dp),
                        color = if (isSelected) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                        border = BorderStroke(
                            1.dp,
                            if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outlineVariant
                        ),
                        modifier = Modifier
                            .weight(1f)
                            .clickable { viewModel.onTimeframeChanged(tf) }
                    ) {
                        Column(
                            modifier = Modifier.padding(vertical = 10.dp, horizontal = 12.dp),
                            horizontalAlignment = Alignment.Start
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                RadioButton(
                                    selected = isSelected,
                                    onClick = { viewModel.onTimeframeChanged(tf) },
                                    modifier = Modifier.size(20.dp)
                                )
                                Spacer(modifier = Modifier.width(6.dp))
                                Text(
                                    tf.label,
                                    style = MaterialTheme.typography.labelLarge,
                                    fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Normal
                                )
                            }
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                tf.subtitle,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.padding(start = 26.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 3. ¿DÓNDE OCURRIÓ? (Ubicación Ajustable)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    strings.reportLocationTitle,
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold
                )
                TextButton(onClick = viewModel::onResetLocationToGps) {
                    Icon(Icons.Filled.MyLocation, contentDescription = null, modifier = Modifier.size(16.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(strings.reportLocationGpsBtn, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.location,
                onValueChange = viewModel::onLocationChanged,
                placeholder = { Text("Ej. Av. Próceres cruce con Calle 4, SJL") },
                leadingIcon = {
                    Icon(Icons.Filled.LocationOn, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Text(
                "La ubicación del incidente es independiente de tu domicilio personal.",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(start = 4.dp, top = 4.dp)
            )

            Spacer(modifier = Modifier.height(18.dp))

            // 4. DETALLES Y EVIDENCIA (100% Opcional)
            Text(
                "Detalles y evidencia (Opcional)",
                style = MaterialTheme.typography.labelLarge,
                fontWeight = FontWeight.Bold
            )
            Spacer(modifier = Modifier.height(4.dp))
            OutlinedTextField(
                value = uiState.description,
                onValueChange = viewModel::onDescriptionChange,
                placeholder = { Text("Breve descripción de lo sucedido o características (opcional)...") },
                minLines = 2,
                maxLines = 4,
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )

            Spacer(modifier = Modifier.height(10.dp))

            // Evidencia Fotográfica Opcional
            if (uiState.photoTaken && uiState.photoBitmap != null) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Row(
                        modifier = Modifier.padding(10.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Image(
                            bitmap = uiState.photoBitmap!!.asImageBitmap(),
                            contentDescription = "Foto capturada",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .size(48.dp)
                                .clip(RoundedCornerShape(8.dp))
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text("Foto adjunta", style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.Bold)
                            Text("${uiState.photoSizeMb} MB · Comprimida para red", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                        }
                        IconButton(onClick = viewModel::onRemovePhoto) {
                            Icon(Icons.Filled.Close, contentDescription = strings.reportRemovePhotoBtn, tint = MaterialTheme.colorScheme.error)
                        }
                    }
                }
            } else {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    OutlinedButton(
                        onClick = {
                            val hasCamera = ContextCompat.checkSelfPermission(
                                context, Manifest.permission.CAMERA
                            ) == PackageManager.PERMISSION_GRANTED

                            if (hasCamera) {
                                try {
                                    cameraLauncher.launch(null)
                                } catch (e: Exception) {
                                    Log.e("REPORT_PHOTO", "Error abriendo cámara: ${e.message}")
                                    android.widget.Toast.makeText(context, "No se pudo abrir la cámara. Puedes adjuntar desde la Galería.", android.widget.Toast.LENGTH_SHORT).show()
                                }
                            } else {
                                cameraPermissionLauncher.launch(Manifest.permission.CAMERA)
                            }
                        },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.CameraAlt, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Cámara")
                    }

                    OutlinedButton(
                        onClick = { galleryLauncher.launch("image/*") },
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Filled.PhotoLibrary, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(6.dp))
                        Text("Galería")
                    }
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            // Nota de Voz Opcional
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = if (uiState.hasVoiceNote) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surface,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                onClick = viewModel::onToggleVoiceNote,
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Filled.Mic, contentDescription = null, tint = MaterialTheme.colorScheme.primary)
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            if (uiState.hasVoiceNote) "Nota de voz adjunta" else "Grabar nota de voz (Opcional)",
                            fontWeight = FontWeight.Bold,
                            color = MaterialTheme.colorScheme.primary,
                            style = MaterialTheme.typography.bodyMedium
                        )
                        Text("Máximo 60 segundos", style = MaterialTheme.typography.labelSmall, color = MaterialTheme.colorScheme.onSurfaceVariant)
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // 5. PROTECCIÓN DE IDENTIDAD Y TRANSPARENCIA
            Surface(
                shape = RoundedCornerShape(12.dp),
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.outlineVariant),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(14.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                strings.reportAnonymousLabel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Spacer(modifier = Modifier.height(2.dp))
                            Text(
                                strings.reportAnonymousDesc,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Switch(
                            checked = uiState.isAnonymous,
                            onCheckedChange = { viewModel.onToggleAnonymous() }
                        )
                    }

                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        modifier = Modifier.padding(vertical = 10.dp)
                    )

                    Row(verticalAlignment = Alignment.Top) {
                        Icon(
                            Icons.Filled.Lock,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier
                                .size(16.dp)
                                .padding(top = 2.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            strings.reportPublicDataDesc,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}
