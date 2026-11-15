package com.limaenaccion.core.util

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.util.Log
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.limaenaccion.core.data.session.SessionManager
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.Locale

data class DeviceLocationResult(
    val latitude: Double,
    val longitude: Double,
    val address: String,
    val isExactGps: Boolean
)

object DeviceLocationProvider {

    private const val TAG = "LOCATION_PROVIDER"

    // Coordenadas por defecto (Centro de Lima Metropolitana) solo como último recurso
    const val DEFAULT_LIMA_LAT = -12.046374
    const val DEFAULT_LIMA_LNG = -77.042793

    fun hasLocationPermission(context: Context): Boolean {
        val fine = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(
            context, Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    /**
     * Obtiene la ubicación precisa del dispositivo físico usando FusedLocationProviderClient
     * con fallback automático a última ubicación, LocationManager y centroide de distrito de usuario.
     */
    @SuppressLint("MissingPermission")
    suspend fun getBestLocation(context: Context): DeviceLocationResult = withContext(Dispatchers.IO) {
        if (!hasLocationPermission(context)) {
            Log.w(TAG, "No se tienen permisos de ubicación otorgados")
            return@withContext fallbackDistrictLocation(context)
        }

        var foundLocation: Location? = null

        // 1. Intentar FusedLocationProviderClient con alta precisión activa (satelital/red)
        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            // Esperar hasta 4.5 segundos por una lectura satelital fresca
            foundLocation = withTimeoutOrNull(4500L) {
                fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await()
            }

            if (foundLocation != null && isValidCoord(foundLocation.latitude, foundLocation.longitude)) {
                Log.d(TAG, "✅ Ubicación en tiempo real obtenida con getCurrentLocation: ${foundLocation.latitude}, ${foundLocation.longitude}")
            } else {
                // Fallback a lastLocation de Google Play Services
                foundLocation = fusedClient.lastLocation.await()
                if (foundLocation != null && isValidCoord(foundLocation.latitude, foundLocation.longitude)) {
                    Log.d(TAG, "✅ Ubicación obtenida desde fusedClient.lastLocation: ${foundLocation.latitude}, ${foundLocation.longitude}")
                }
            }
        } catch (e: Exception) {
            Log.w(TAG, "Aviso FusedLocationProviderClient: ${e.message}")
        }

        // 2. Si falló Fused, intentar LocationManager nativo del sistema operativo
        if (foundLocation == null || !isValidCoord(foundLocation.latitude, foundLocation.longitude)) {
            try {
                val lm = context.getSystemService(Context.LOCATION_SERVICE) as? LocationManager
                val gpsLoc = try { lm?.getLastKnownLocation(LocationManager.GPS_PROVIDER) } catch (_: Exception) { null }
                val netLoc = try { lm?.getLastKnownLocation(LocationManager.NETWORK_PROVIDER) } catch (_: Exception) { null }

                foundLocation = when {
                    gpsLoc != null && netLoc != null -> if (gpsLoc.time >= netLoc.time) gpsLoc else netLoc
                    gpsLoc != null -> gpsLoc
                    else -> netLoc
                }
                if (foundLocation != null) {
                    Log.d(TAG, "✅ Ubicación obtenida desde LocationManager clásico: ${foundLocation.latitude}, ${foundLocation.longitude}")
                }
            } catch (e: Exception) {
                Log.w(TAG, "Error LocationManager: ${e.message}")
            }
        }

        // 3. Si se obtuvo una ubicación válida de hardware real
        if (foundLocation != null && isValidCoord(foundLocation.latitude, foundLocation.longitude)) {
            val lat = foundLocation.latitude
            val lng = foundLocation.longitude
            val address = reverseGeocode(context, lat, lng)
            return@withContext DeviceLocationResult(
                latitude = lat,
                longitude = lng,
                address = address,
                isExactGps = true
            )
        }

        // 4. Último recurso: usar el distrito configurado en el perfil del usuario activo
        fallbackDistrictLocation(context)
    }

    /**
     * Resuelve una dirección legible a partir de coordenadas GPS usando Geocoder
     */
    fun reverseGeocode(context: Context, latitude: Double, longitude: Double): String {
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            @Suppress("DEPRECATION")
            val addresses = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val calle = addr.thoroughfare ?: addr.featureName ?: ""
                val distrito = addr.subLocality ?: addr.locality ?: ""
                val ciudad = addr.adminArea ?: "Lima"

                val parts = mutableListOf<String>()
                if (calle.isNotBlank()) parts.add(calle)
                if (distrito.isNotBlank()) parts.add(distrito)
                if (parts.isEmpty()) parts.add(ciudad)

                return parts.joinToString(", ")
            }
        } catch (e: Exception) {
            Log.w(TAG, "Geocoder no disponible: ${e.message}")
        }

        // Si Geocoder falla (sin internet), calcular el distrito más cercano en Lima
        val closestDistrict = findClosestDistrict(latitude, longitude)
        return if (closestDistrict != null) {
            "$closestDistrict, Lima (GPS: ${String.format(Locale.US, "%.4f, %.4f", latitude, longitude)})"
        } else {
            "Ubicación GPS (${String.format(Locale.US, "%.4f, %.4f", latitude, longitude)})"
        }
    }

    /**
     * Convierte una dirección de texto a coordenadas sin amarrar distritos
     */
    suspend fun forwardGeocode(context: Context, addressQuery: String): Pair<Double, Double>? = withContext(Dispatchers.IO) {
        if (addressQuery.trim().length < 3) return@withContext null
        try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val cleanQuery = if (addressQuery.contains("Lima", ignoreCase = true) || addressQuery.contains("Perú", ignoreCase = true)) {
                addressQuery
            } else {
                "$addressQuery, Lima, Perú"
            }

            @Suppress("DEPRECATION")
            val results = geocoder.getFromLocationName(cleanQuery, 1)
            if (!results.isNullOrEmpty()) {
                val match = results[0]
                return@withContext Pair(match.latitude, match.longitude)
            }
        } catch (e: Exception) {
            Log.w(TAG, "Error forward geocoding: ${e.message}")
        }
        null
    }

    private fun fallbackDistrictLocation(context: Context): DeviceLocationResult {
        val userDistrict = SessionManager.currentUser.value?.distrito?.trim()
        val districtData = LimaLocations.DISTRICTS_DATA.firstOrNull {
            it.name.equals(userDistrict, ignoreCase = true)
        }

        return if (districtData != null) {
            DeviceLocationResult(
                latitude = districtData.latitude,
                longitude = districtData.longitude,
                address = "${districtData.name}, Lima",
                isExactGps = false
            )
        } else {
            DeviceLocationResult(
                latitude = DEFAULT_LIMA_LAT,
                longitude = DEFAULT_LIMA_LNG,
                address = "Lima Metropolitana, Lima",
                isExactGps = false
            )
        }
    }

    private fun isValidCoord(lat: Double, lng: Double): Boolean {
        return lat != 0.0 && lng != 0.0 && lat in -90.0..90.0 && lng in -180.0..180.0
    }

    private fun findClosestDistrict(lat: Double, lng: Double): String? {
        var minDistance = Double.MAX_VALUE
        var bestDistrict: String? = null

        for (d in LimaLocations.DISTRICTS_DATA) {
            val dLat = d.latitude - lat
            val dLng = d.longitude - lng
            val dist = (dLat * dLat) + (dLng * dLng)
            if (dist < minDistance) {
                minDistance = dist
                bestDistrict = d.name
            }
        }
        return bestDistrict
    }
}
