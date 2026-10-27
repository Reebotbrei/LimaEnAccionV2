package com.limaenaccion.feature_map.presentation.components

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.Path
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.Drawable
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.os.Bundle
import android.preference.PreferenceManager
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
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.content.ContextCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.compose.ui.platform.LocalLifecycleOwner
import com.limaenaccion.feature_alerts.data.model.Incident
import com.limaenaccion.feature_alerts.data.model.IncidentType
import org.osmdroid.config.Configuration
import org.osmdroid.tileprovider.tilesource.TileSourceFactory
import org.osmdroid.util.GeoPoint
import org.osmdroid.views.CustomZoomButtonsController
import org.osmdroid.views.MapView
import org.osmdroid.views.overlay.Marker

@Composable
fun RealOsmMap(
    incidents: List<Incident>,
    onPinClick: (String) -> Unit,
    modifier: Modifier = Modifier,
    defaultLatitude: Double = -12.019542,
    defaultLongitude: Double = -77.014231
) {
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current

    // Coordenada actual del usuario (comienza con la ubicación por defecto de SJL)
    var userLocation by remember { mutableStateOf(GeoPoint(defaultLatitude, defaultLongitude)) }
    var hasExactGps by remember { mutableStateOf(false) }
    var mapViewRef by remember { mutableStateOf<MapView?>(null) }
    var userMarkerRef by remember { mutableStateOf<Marker?>(null) }

    // Inicializar configuración de OpenStreetMap
    LaunchedEffect(Unit) {
        Configuration.getInstance().userAgentValue = context.packageName
        Configuration.getInstance().load(context, PreferenceManager.getDefaultSharedPreferences(context))
    }

    // Función para obtener la ubicación real de GPS del dispositivo
    @SuppressLint("MissingPermission")
    fun updateLocationFromDevice() {
        val hasFinePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val hasCoarsePermission = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (!hasFinePermission && !hasCoarsePermission) return

        try {
            val locationManager = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
            val lastGps = locationManager?.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            val lastNet = locationManager?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER)

            val bestLoc: Location? = when {
                lastGps != null && lastNet != null -> if (lastGps.time >= lastNet.time) lastGps else lastNet
                lastGps != null -> lastGps
                else -> lastNet
            }

            if (bestLoc != null) {
                val newPoint = GeoPoint(bestLoc.latitude, bestLoc.longitude)
                userLocation = newPoint
                hasExactGps = true
                userMarkerRef?.position = newPoint
                mapViewRef?.invalidate()
            }

            // Registrar escucha en tiempo real
            val listener = object : LocationListener {
                override fun onLocationChanged(location: Location) {
                    val pt = GeoPoint(location.latitude, location.longitude)
                    userLocation = pt
                    hasExactGps = true
                    userMarkerRef?.position = pt
                    mapViewRef?.invalidate()
                }
                @Deprecated("Deprecated in Java")
                override fun onStatusChanged(provider: String?, status: Int, extras: Bundle?) {}
                override fun onProviderEnabled(provider: String) {}
                override fun onProviderDisabled(provider: String) {}
            }

            locationManager?.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                4000L,
                3f,
                listener
            )
            locationManager?.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                4000L,
                3f,
                listener
            )
        } catch (e: Exception) {
            Log.w("REAL_OSM_MAP", "Error al consultar GPS del dispositivo: ${e.message}")
        }
    }

    // Solicitar permiso de ubicación si aún no está otorgado
    val permissionLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestMultiplePermissions()
    ) { permissions ->
        val granted = permissions[Manifest.permission.ACCESS_FINE_LOCATION] == true ||
                permissions[Manifest.permission.ACCESS_COARSE_LOCATION] == true
        if (granted) {
            updateLocationFromDevice()
        }
    }

    LaunchedEffect(Unit) {
        val hasFine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (hasFine) {
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

    // Ciclo de vida para evitar fugas de memoria
    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            when (event) {
                Lifecycle.Event.ON_RESUME -> {
                    mapViewRef?.onResume()
                    updateLocationFromDevice()
                }
                Lifecycle.Event.ON_PAUSE -> mapViewRef?.onPause()
                Lifecycle.Event.ON_DESTROY -> mapViewRef?.onDetach()
                else -> Unit
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose {
            lifecycleOwner.lifecycle.removeObserver(observer)
            mapViewRef?.onDetach()
        }
    }

    Box(modifier = modifier.fillMaxSize()) {
        AndroidView(
            modifier = Modifier.fillMaxSize(),
            factory = { ctx ->
                MapView(ctx).apply {
                    setTileSource(TileSourceFactory.MAPNIK)
                    setMultiTouchControls(true)
                    zoomController.setVisibility(CustomZoomButtonsController.Visibility.NEVER)
                    minZoomLevel = 10.0
                    maxZoomLevel = 19.0
                    controller.setZoom(16.0)
                    controller.setCenter(userLocation)
                    mapViewRef = this
                }
            },
            update = { mapView ->
                mapView.overlays.clear()

                // 1. PIN DE TU UBICACIÓN EXACTA (Ícono distintivo de Chincheta/Pin GPS con punta inferior)
                val userMarker = Marker(mapView).apply {
                    position = userLocation
                    title = "📍 Tu ubicación exacta"
                    snippet = if (hasExactGps) "Ubicación GPS en tiempo real" else "San Juan de Lurigancho, Lima"
                    icon = createUserLocationPinDrawable(context)
                    // El ancla se fija en la punta inferior de la chincheta para señalar la coordenada exacta
                    setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_BOTTOM)
                }
                userMarkerRef = userMarker
                mapView.overlays.add(userMarker)

                // 2. Marcadores de los reportes reales de Firebase Firestore
                incidents.forEach { incident ->
                    val incidentMarker = Marker(mapView).apply {
                        position = GeoPoint(incident.latitude, incident.longitude)
                        title = incident.title
                        snippet = incident.description
                        icon = createIncidentMarkerDrawable(context, incident.type)
                        setAnchor(Marker.ANCHOR_CENTER, Marker.ANCHOR_CENTER)
                        setOnMarkerClickListener { _, _ ->
                            onPinClick(incident.id)
                            true
                        }
                    }
                    mapView.overlays.add(incidentMarker)
                }

                mapView.invalidate()
            }
        )

        // Controles flotantes interactivos (Zoom y recentrar en tu ubicación)
        Column(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .padding(end = 16.dp, bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            SmallFloatingActionButton(
                onClick = {
                    mapViewRef?.controller?.zoomIn()
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
                    mapViewRef?.controller?.zoomOut()
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
                    updateLocationFromDevice()
                    mapViewRef?.controller?.animateTo(userLocation)
                    mapViewRef?.controller?.setZoom(16.5)
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
 * Genera el PIN oficial de "Tu ubicación exacta" (Chincheta de mapa con punta inferior).
 * Esto evita confusiones con los íconos de reportes de incidentes.
 */
private fun createUserLocationPinDrawable(context: Context): Drawable {
    val density = context.resources.displayMetrics.density
    val widthPx = (34 * density).toInt()
    val heightPx = (48 * density).toInt()
    val bitmap = Bitmap.createBitmap(widthPx, heightPx, Bitmap.Config.ARGB_8888)
    val canvas = android.graphics.Canvas(bitmap)

    val centerX = widthPx / 2f
    val headRadius = (widthPx / 2f) - (2.5f * density)
    val headCenterY = headRadius + (2.5f * density)
    val tipY = heightPx - (2.5f * density)

    val path = Path().apply {
        // Arco superior del pin
        arcTo(
            centerX - headRadius,
            headCenterY - headRadius,
            centerX + headRadius,
            headCenterY + headRadius,
            -180f,
            180f,
            false
        )
        // Línea hacia la punta inferior
        lineTo(centerX, tipY)
        close()
    }

    val paint = Paint(Paint.ANTI_ALIAS_FLAG)

    // 1. Sombra / Borde blanco grueso para alto contraste
    paint.style = Paint.Style.STROKE
    paint.strokeWidth = 3f * density
    paint.color = android.graphics.Color.WHITE
    canvas.drawPath(path, paint)

    // 2. Relleno azul Google Maps / Material
    paint.style = Paint.Style.FILL
    paint.color = 0xFF1D4ED8.toInt() // Azul intenso
    canvas.drawPath(path, paint)

    // 3. Círculo blanco central en la cabeza del pin
    paint.color = android.graphics.Color.WHITE
    canvas.drawCircle(centerX, headCenterY, headRadius * 0.45f, paint)

    // 4. Núcleo azul cyan interior
    paint.color = 0xFF0284C7.toInt()
    canvas.drawCircle(centerX, headCenterY, headRadius * 0.22f, paint)

    return BitmapDrawable(context.resources, bitmap)
}

/**
 * Genera un pin circular moderno con el color oficial y emoji del tipo de incidente.
 */
private fun createIncidentMarkerDrawable(context: Context, incidentType: IncidentType): Drawable {
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

    return BitmapDrawable(context.resources, bitmap)
}
