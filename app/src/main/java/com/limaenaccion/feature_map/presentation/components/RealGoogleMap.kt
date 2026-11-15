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
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.CameraPositionState
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.limaenaccion.feature_alerts.data.model.Incident
import com.limaenaccion.feature_alerts.data.model.IncidentType
import kotlinx.coroutines.launch

/**
 * Componente oficial de Google Maps para Jetpack Compose.
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
    var hasExactGps by remember { mutableStateOf(false) }
    var hasCenteredOnUser by remember { mutableStateOf(false) }

    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(
                context, Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(defaultLatLng, 15.5f)
    }

    // Cache de íconos BitmapDescriptor para rendimiento óptimo
    val markerIcons = remember(context) {
        IncidentType.entries.associateWith { type ->
            createIncidentBitmapDescriptor(context, type)
        }
    }
    val userPinIcon = remember(context) {
        createUserPinBitmapDescriptor(context)
    }

    // Función para obtener la ubicación real con FusedLocation / LocationManager
    @SuppressLint("MissingPermission")
    fun updateLocationFromDevice(forceAnimate: Boolean = false) {
        if (!hasLocationPermission) return

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            fusedClient.lastLocation.addOnSuccessListener { loc: Location? ->
                if (loc != null) {
                    val pt = LatLng(loc.latitude, loc.longitude)
                    userLatLng = pt
                    hasExactGps = true
                    if (!hasCenteredOnUser || forceAnimate) {
                        hasCenteredOnUser = true
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(pt, 16.5f),
                                800
                            )
                        }
                    }
                }
            }.addOnFailureListener {
                // Fallback a LocationManager clásico
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val lastGps = lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                val lastNet = lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)
                val best = when {
                    lastGps != null && lastNet != null -> if (lastGps.time >= lastNet.time) lastGps else lastNet
                    lastGps != null -> lastGps
                    else -> lastNet
                }
                if (best != null) {
                    val pt = LatLng(best.latitude, best.longitude)
                    userLatLng = pt
                    hasExactGps = true
                    if (!hasCenteredOnUser || forceAnimate) {
                        hasCenteredOnUser = true
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(pt, 16.5f),
                                800
                            )
                        }
                    }
                }
            }

            // Escucha periódica de GPS
            val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val pt = LatLng(location.latitude, location.longitude)
                    userLatLng = pt
                    hasExactGps = true
                    if (!hasCenteredOnUser) {
                        hasCenteredOnUser = true
                        coroutineScope.launch {
                            cameraPositionState.animate(
                                CameraUpdateFactory.newLatLngZoom(pt, 16.5f),
                                800
                            )
                        }
                    }
                }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }
            lm?.requestLocationUpdates(LocationManager.GPS_PROVIDER, 5000L, 5f, listener)
            lm?.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 5000L, 5f, listener)
        } catch (e: Exception) {
            Log.w("GOOGLE_MAPS", "Error obteniendo ubicación: ${e.message}")
        }
    }

    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        hasLocationPermission = granted
        if (granted) {
            updateLocationFromDevice(forceAnimate = true)
        }
    }

    LaunchedEffect(Unit) {
        if (hasLocationPermission) {
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

    val uiSettings = remember(hasLocationPermission) {
        MapUiSettings(
            zoomControlsEnabled = false,
            myLocationButtonEnabled = false, // Usamos nuestro botón flotante estilizado
            compassEnabled = true,
            mapToolbarEnabled = false
        )
    }

    val mapProperties = remember(hasLocationPermission) {
        MapProperties(
            isMyLocationEnabled = hasLocationPermission
        )
    }

    Box(modifier = modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = cameraPositionState,
            properties = mapProperties,
            uiSettings = uiSettings
        ) {
            // 1. PIN de ubicación del usuario (solo si el servicio nativo de punto azul no estuviese disponible)
            if (!hasLocationPermission) {
                Marker(
                    state = rememberMarkerState(position = userLatLng),
                    title = "📍 Tu ubicación seleccionada",
                    snippet = "Ubicación inicial de San Juan de Lurigancho",
                    icon = userPinIcon
                )
            }

            // 2. Marcadores de los reportes en tiempo real de Firebase Firestore
            incidents.forEach { incident ->
                val pos = LatLng(incident.latitude, incident.longitude)
                val icon = markerIcons[incident.type] ?: userPinIcon

                Marker(
                    state = rememberMarkerState(key = incident.id, position = pos),
                    title = incident.title,
                    snippet = incident.description,
                    icon = icon,
                    onClick = {
                        onPinClick(incident.id)
                        true // Consumir evento para desplegar el BottomSheet en DashboardScreen
                    }
                )
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
                        cameraPositionState.animate(CameraUpdateFactory.zoomIn(), 300)
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
                        cameraPositionState.animate(CameraUpdateFactory.zoomOut(), 300)
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
                    if (hasLocationPermission) {
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
 * Genera el BitmapDescriptor para los pines de incidentes de Google Maps
 * con color por categoría y su respectivo emoji cívico.
 */
private fun createIncidentBitmapDescriptor(context: Context, incidentType: IncidentType): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val sizePx = (38 * density).toInt()
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

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}

/**
 * Genera el BitmapDescriptor para el pin de usuario
 */
private fun createUserPinBitmapDescriptor(context: Context): BitmapDescriptor {
    val density = context.resources.displayMetrics.density
    val sizePx = (36 * density).toInt()
    val bitmap = Bitmap.createBitmap(sizePx, sizePx, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f, paint)

    paint.color = 0xFF1D4ED8.toInt()
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, sizePx / 2f - (3 * density), paint)

    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(sizePx / 2f, sizePx / 2f, 5 * density, paint)

    return BitmapDescriptorFactory.fromBitmap(bitmap)
}
