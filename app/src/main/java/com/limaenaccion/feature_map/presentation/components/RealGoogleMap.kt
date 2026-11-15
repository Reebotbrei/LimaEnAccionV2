package com.limaenaccion.feature_map.presentation.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.util.Log
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.FloatingActionButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SmallFloatingActionButton
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.MapsInitializer
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.limaenaccion.core.util.DeviceLocationProvider
import com.limaenaccion.feature_alerts.data.model.Incident
import com.limaenaccion.feature_alerts.data.model.IncidentType
import kotlinx.coroutines.launch

/**
 * Componente oficial de Google Maps para Jetpack Compose con protección robusta contra fallos.
 * Conectado en arquitectura MVVM + Clean Architecture / MVI al flujo de reportes en tiempo real.
 */
@Composable
fun RealGoogleMap(
    incidents: List<Incident>,
    onPinClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    defaultLatitude: Double = -12.019542,
    defaultLongitude: Double = -77.014231
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()

    val defaultLatLng = remember { LatLng(defaultLatitude, defaultLongitude) }
    var userLatLng by remember { mutableStateOf(defaultLatLng) }
    var hasCenteredOnUser by remember { mutableStateOf(false) }
    var isMapLoaded by remember { mutableStateOf(false) }

    // Verificación segura de permisos
    fun checkHasFine(): Boolean = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_FINE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    fun checkHasCoarse(): Boolean = ContextCompat.checkSelfPermission(
        context, Manifest.permission.ACCESS_COARSE_LOCATION
    ) == PackageManager.PERMISSION_GRANTED

    var hasFinePermission by remember { mutableStateOf(checkHasFine()) }
    var hasCoarsePermission by remember { mutableStateOf(checkHasCoarse()) }
    val hasAnyLocationPermission = hasFinePermission || hasCoarsePermission

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 15.5f)
    }

    // Inicializar Google Maps Renderer de forma segura
    LaunchedEffect(Unit) {
        try {
            MapsInitializer.initialize(context, MapsInitializer.Renderer.LATEST) {
                Log.d("GOOGLE_MAPS", "MapsInitializer completado con renderer LATEST")
            }
        } catch (_: Exception) {
            try {
                MapsInitializer.initialize(context)
            } catch (e: Exception) {
                Log.w("GOOGLE_MAPS", "Aviso MapsInitializer: ${e.message}")
            }
        }
    }

    // Cache dinámico de íconos seguros (se calculan o recalculan sin provocar NPE)
    var markerIcons by remember { mutableStateOf<Map<IncidentType, BitmapDescriptor>>(emptyMap()) }
    var userPinIcon by remember { mutableStateOf<BitmapDescriptor?>(null) }

    // Generar iconos una vez inicializado el entorno de Maps
    LaunchedEffect(isMapLoaded) {
        try {
            markerIcons = IncidentType.entries.mapNotNull { type ->
                createIncidentBitmapDescriptor(context, type)?.let { icon -> type to icon }
            }.toMap()
            userPinIcon = createUserPinBitmapDescriptor(context)
        } catch (e: Exception) {
            Log.w("GOOGLE_MAPS", "Aviso al precargar iconos de marcadores: ${e.message}")
        }
    }

    // Función para obtener la ubicación real con FusedLocation / LocationManager
    @SuppressLint("MissingPermission")
    fun updateLocationFromDevice(forceAnimate: Boolean = false) {
        if (!checkHasFine() && !checkHasCoarse()) return

        coroutineScope.launch {
            try {
                val best = DeviceLocationProvider.getBestLocation(context)
                if (best.isExactGps || best.latitude != DeviceLocationProvider.DEFAULT_LIMA_LAT) {
                    val pt = LatLng(best.latitude, best.longitude)
                    userLatLng = pt
                    if (!hasCenteredOnUser || forceAnimate) {
                        hasCenteredOnUser = true
                        cameraPositionState.animate(
                            CameraUpdateFactory.newLatLngZoom(pt, 16.5f),
                            800
                        )
                    }
                }
            } catch (e: Exception) {
                Log.w("GOOGLE_MAPS", "Error al centrar en usuario: ${e.message}")
            }
        }

        try {
            // Escucha periódica de GPS para cuando el usuario se desplace
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    if (location.latitude != 0.0 && location.longitude != 0.0) {
                        val pt = LatLng(location.latitude, location.longitude)
                        userLatLng = pt
                        if (!hasCenteredOnUser) {
                            hasCenteredOnUser = true
                            coroutineScope.launch {
                                try {
                                    cameraPositionState.animate(
                                        CameraUpdateFactory.newLatLngZoom(pt, 16.5f),
                                        800
                                    )
                                } catch (_: Exception) {}
                            }
                        }
                    }
                }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            if (checkHasFine()) {
                lm?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 5f, listener)
            }
            if (checkHasCoarse() || checkHasFine()) {
                lm?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 5f, listener)
            }
        } catch (e: Exception) {
            Log.w("GOOGLE_MAPS", "Error obteniendo ubicación GPS: ${e.message}")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        hasFinePermission = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true
        hasCoarsePermission = permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (hasFinePermission || hasCoarsePermission) {
            updateLocationFromDevice(forceAnimate = true)
        }
    }

    LaunchedEffect(Unit) {
        if (checkHasFine() || checkHasCoarse()) {
            hasFinePermission = checkHasFine()
            hasCoarsePermission = checkHasCoarse()
            updateLocationFromDevice()
        } else {
            permissionLauncher.launch(
                arrayOf(
                    Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                )
            )
        }
    }

    val uiSettings = remember {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false,
            compassEnabled = true,
            mapToolbarEnabled = false
        )
    }

    // Solo habilitar isMyLocationEnabled si el permiso está explícitamente confirmado
    val mapProperties = remember(hasAnyLocationPermission) {
        MapProperties(
            isMyLocationEnabled = hasAnyLocationPermission
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings,
            onMapLoaded = {
                isMapLoaded = true
            }
        ) {
            // 1. PIN de respaldo si la capa nativa azul no está disponible
            if (!hasAnyLocationPermission) {
                Marker(
                    state = rememberMarkerState(position = userLatLng),
                    title = "📍 Tu ubicación seleccionada",
                    snippet = "Ubicación inicial de San Juan de Lurigancho",
                    icon = userPinIcon
                )
            }

            // 2. Marcadores de los reportes en tiempo real de Firebase Firestore
            incidents.forEach { incident ->
                if (incident.latitude != 0.0 && incident.longitude != 0.0) {
                    val pos = LatLng(incident.latitude, incident.longitude)
                    val icon = markerIcons[incident.type] ?: userPinIcon

                    Marker(
                        state = rememberMarkerState(key = incident.id, position = pos),
                        title = incident.title,
                        snippet = incident.description,
                        icon = icon,
                        onClick = {
                            onPinClick(incident.id)
                            true
                        }
                    )
                }
            }
        }

        // 3. Controles flotantes interactivos (Zoom +, Zoom - y Mi Ubicación)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        try {
                            cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 300)
                        } catch (_: Exception) {}
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "Acercar mapa")
            }

            SmallFloatingActionButton(
                onClick = {
                    coroutineScope.launch {
                        try {
                            cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 300)
                        } catch (_: Exception) {}
                    }
                },
                containerColor = MaterialTheme.colorScheme.surface,
                contentColor = MaterialTheme.colorScheme.onSurface,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 4.dp)
            ) {
                Icon(Icons.Filled.Remove, contentDescription = "Alejar mapa")
            }

            FloatingActionButton(
                onClick = {
                    if (checkHasFine() || checkHasCoarse()) {
                        updateLocationFromDevice(forceAnimate = true)
                    } else {
                        permissionLauncher.launch(
                            arrayOf(
                                Manifest.permission.ACCESS_FINE_LOCATION,
                                Manifest.permission.ACCESS_COARSE_LOCATION
                            )
                        )
                    }
                },
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = Color.White,
                shape = CircleShape,
                elevation = FloatingActionButtonDefaults.elevation(defaultElevation = 6.dp)
            ) {
                Icon(Icons.Filled.MyLocation, contentDescription = "Mi ubicación exacta")
            }
        }
    }
}

/**
 * Genera el BitmapDescriptor para los pines de incidentes de Google Maps de forma segura contra NPE.
 */
private fun createIncidentBitmapDescriptor(context: Context, incidentType: IncidentType): BitmapDescriptor? {
    return try {
        val density = context.resources.displayMetrics.density
        val sizePx = (38 * density).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)

        // Borde exterior blanco
        paint.color = android.graphics.Color.WHITE
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)

        // Círculo principal con color de la categoría
        val colorArgb = when (incidentType) {
            IncidentType.ROBO -> 0xFFDC2626.toInt()             // Rojo
            IncidentType.INCENDIO -> 0xFFEA580C.toInt()         // Naranja
            IncidentType.EMERGENCIA_MEDICA,
            IncidentType.MEDICO -> 0xFF059669.toInt()           // Verde
            IncidentType.ACCIDENTE -> 0xFFD97706.toInt()         // Ámbar
            IncidentType.VANDALISMO -> 0xFF7C3AED.toInt()       // Púrpura
            IncidentType.ACTIVIDAD_INUSUAL -> 0xFF2563EB.toInt() // Azul
        }
        paint.color = colorArgb
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - (2.5f * density), paint)

        // Emoji representativo en el centro
        paint.color = android.graphics.Color.WHITE
        paint.textSize = 17 * density
        paint.textAlign = Paint.Align.CENTER
        val emoji = when (incidentType) {
            IncidentType.ROBO -> "⚠️"
            IncidentType.INCENDIO -> "🔥"
            IncidentType.EMERGENCIA_MEDICA,
            IncidentType.MEDICO -> "🚑"
            IncidentType.ACCIDENTE -> "🚗"
            IncidentType.VANDALISMO -> "🔨"
            IncidentType.ACTIVIDAD_INUSUAL -> "👁️"
        }

        val fontMetrics = paint.fontMetrics
        val yOffset = (sizePx / 2f) - (fontMetrics.ascent + fontMetrics.descent) / 2f
        canvas.drawText(emoji, sizePx / 2f, yOffset, paint)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        Log.w("GOOGLE_MAPS", "No se pudo generar BitmapDescriptor para $incidentType: ${e.message}")
        null
    }
}

/**
 * Genera el BitmapDescriptor para el pin de usuario de forma segura contra NPE.
 */
private fun createUserPinBitmapDescriptor(context: Context): BitmapDescriptor? {
    return try {
        val density = context.resources.displayMetrics.density
        val sizePx = (36 * density).toInt().coerceAtLeast(1)
        val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
        val canvas = android.graphics.Canvas(bitmap)

        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.color = android.graphics.Color.WHITE
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)

        paint.color = 0xFF1D4ED8.toInt()
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - (3 * density), paint)

        paint.color = android.graphics.Color.WHITE
        canvas.drawCircle(sizePx / 2f, sizePx / 2f, 5 * density, paint)

        BitmapDescriptorFactory.fromBitmap(bitmap)
    } catch (e: Exception) {
        Log.w("GOOGLE_MAPS", "No se pudo generar user pin bitmap: ${e.message}")
        null
    }
}
