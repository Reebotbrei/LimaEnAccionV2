package com.limaenaccion.feature_alerts.data.repository

import android.app.Application
import android.util.Log
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.dao.ReporteDao
import com.limaenaccion.core.data.local.entities.ReporteEntity
import com.limaenaccion.core.data.network.ApiService
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.session.SessionManager
import com.limaenaccion.feature_alerts.data.model.Incident
import com.limaenaccion.feature_alerts.data.model.IncidentStatus
import com.limaenaccion.feature_alerts.data.model.IncidentType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class IncidentRepositoryImpl(
    application: Application,
    private val apiService: ApiService = RetrofitClient.instance,
    private val reporteDao: ReporteDao = AppDatabase.getInstance(application).reporteDao(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : IncidentRepository {

    private fun parseIncidentType(tipo: String): IncidentType {
        val clean = tipo.trim().uppercase()
        return when {
            clean.contains("VANDALISMO") -> IncidentType.VANDALISMO
            clean.contains("MEDIC") -> IncidentType.EMERGENCIA_MEDICA
            clean.contains("ACTIVIDAD") || clean.contains("INUSUAL") || clean.contains("SOSPECH") -> IncidentType.ACTIVIDAD_INUSUAL
            clean.contains("INCENDIO") -> IncidentType.INCENDIO
            clean.contains("ACCIDENT") -> IncidentType.ACCIDENTE
            clean.contains("ROBO") -> IncidentType.ROBO
            else -> try {
                IncidentType.valueOf(clean)
            } catch (e: Exception) {
                IncidentType.ROBO
            }
        }
    }

    private fun parseIncidentStatus(estado: String): IncidentStatus {
        val clean = estado.trim().uppercase()
        return when {
            clean.contains("RESUELTO") || clean.contains("ATENDIDO") -> IncidentStatus.RESUELTO
            clean.contains("ATENCION") || clean.contains("CURSO") || clean.contains("PROCESO") -> IncidentStatus.EN_ATENCION
            else -> IncidentStatus.REPORTADO
        }
    }

    private fun calcularPosicionMapa(lat: Double, lng: Double): Pair<Float, Float> {
        val minLat = -12.08
        val maxLat = -11.96
        val minLng = -77.06
        val maxLng = -76.96

        val normY = ((maxLat - lat) / (maxLat - minLat)).toFloat().coerceIn(0.15f, 0.85f)
        val normX = ((lng - minLng) / (maxLng - minLng)).toFloat().coerceIn(0.15f, 0.85f)
        return Pair(normX, normY)
    }

    private fun calcularTiempoRelativo(creadoEn: Long): String {
        val diffMs = System.currentTimeMillis() - creadoEn
        val minutos = (diffMs / (60 * 1000)).toInt()
        return when {
            minutos <= 1 -> "ahora"
            minutos < 60 -> "$minutos min"
            minutos < 1440 -> "${minutos / 60} h"
            else -> "${minutos / 1440} d"
        }
    }

    private fun calcularDistanciaMetros(lat: Double, lng: Double): Int {
        val dist = ((Math.abs(lat - (-12.019542)) + Math.abs(lng - (-77.014231))) * 10000).toInt()
        return (dist % 750) + 60
    }

    private fun entityToIncident(entity: ReporteEntity): Incident {
        val type = parseIncidentType(entity.tipo)
        val (mapX, mapY) = calcularPosicionMapa(entity.latitud, entity.longitud)
        val currentUser = SessionManager.currentUser.value
        val isMine = entity.usuarioId == currentUser?.correo
        val authorName = if (isMine) (currentUser?.nombreCompleto?.takeIf { it.isNotBlank() } ?: "Vecino Ciudadano") else "Vecino Vigilante"
        val handle = if (isMine) "@${authorName.lowercase().replace(" ", "_").take(15)}" else "@vecino_sjl"

        return Incident(
            id = entity.id,
            type = type,
            title = entity.titulo,
            description = if (!entity.descripcion.isNullOrBlank()) entity.descripcion else "Reporte de ${type.label} registrado en la zona.",
            reportedBy = authorName,
            handle = handle,
            isOfficial = false,
            location = "San Juan de Lurigancho, Lima",
            distanceMeters = calcularDistanciaMetros(entity.latitud, entity.longitud),
            reportedAt = calcularTiempoRelativo(entity.creadoEn),
            status = "Reportado a Serenazgo · En atención",
            mapX = mapX,
            mapY = mapY,
            photoBase64 = entity.fotoUri,
            incidentStatus = parseIncidentStatus(entity.estado),
            witnessCount = 0,
            commentsCount = 0,
            isVerifiedByAuthority = false,
            isReportedAsFalse = false,
            falseReportsCount = 0,
            latitude = entity.latitud,
            longitude = entity.longitud
        )
    }

    override fun observeIncidents(): Flow<List<Incident>> = callbackFlow {
        // 1. Emisión inmediata desde SQLite Room (Offline-first de reportes vigentes)
        CoroutineScope(Dispatchers.IO).launch {
            try {
                val ahora = System.currentTimeMillis()
                val locales = reporteDao.obtenerListaVigentes(ahora).map { entityToIncident(it) }
                trySend(locales)
            } catch (e: Exception) {
                Log.e("INCIDENT_REPO", "Error al leer caché inicial SQLite: ${e.message}")
            }
        }

        // 2. Suscripción en vivo a Firebase Cloud Firestore (Tiempo Real)
        val ahora = System.currentTimeMillis()
        val listener = firestore.collection("reportes")
            .whereGreaterThan("venceEn", ahora)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.w("INCIDENT_REPO", "⚠️ Firestore snapshot listener error: ${error.message}")
                    return@addSnapshotListener
                }

                if (snapshot != null) {
                    val remoteEntities = mutableListOf<ReporteEntity>()
                    val realIncidents = mutableListOf<Incident>()

                    for (doc in snapshot.documents) {
                        try {
                            val id = doc.getString("id") ?: doc.id
                            val tipo = doc.getString("tipo") ?: "ROBO"
                            val titulo = doc.getString("titulo") ?: "Incidente reportado"
                            val descripcion = doc.getString("descripcion") ?: ""
                            val latitud = doc.getDouble("latitud") ?: -12.019542
                            val longitud = doc.getDouble("longitud") ?: -77.014231
                            val foto = doc.getString("fotoBase64") ?: doc.getString("fotoUrl")
                            val creadoEn = doc.getLong("creadoEn") ?: System.currentTimeMillis()
                            val venceEn = doc.getLong("venceEn") ?: (creadoEn + 12 * 60 * 60 * 1000L)
                            val estado = doc.getString("estado") ?: "REPORTADO"
                            val esSos = doc.getBoolean("esSos") ?: false
                            val esAnonimo = doc.getBoolean("esAnonimo") ?: false
                            val rawNombre = doc.getString("usuarioNombre")
                            val currentUser = SessionManager.currentUser.value
                            val isMine = doc.getString("usuarioId") == currentUser?.correo
                            val authorName = if (esAnonimo) {
                                "Vecino Anónimo"
                            } else if (!rawNombre.isNullOrBlank() && rawNombre != "Vecino Vigilante") {
                                rawNombre
                            } else if (isMine) {
                                currentUser?.nombreCompleto?.takeIf { it.isNotBlank() } ?: "Vecino Ciudadano"
                            } else {
                                "Vecino Vigilante"
                            }
                            val handle = if (esAnonimo) {
                                "@vecino_anonimo"
                            } else {
                                "@${authorName.lowercase().replace(" ", "_").replace(".", "").take(14)}"
                            }
                            val witnessCount = doc.getLong("witnessCount")?.toInt() ?: 0
                            val falseReportsCount = doc.getLong("falseReportsCount")?.toInt() ?: 0

                            val entity = ReporteEntity(
                                id = id,
                                usuarioId = doc.getString("usuarioId") ?: "usr_vecino_local",
                                tipo = tipo,
                                titulo = titulo,
                                descripcion = descripcion,
                                latitud = latitud,
                                longitud = longitud,
                                fotoUri = foto,
                                creadoEn = creadoEn,
                                venceEn = venceEn,
                                estado = estado,
                                esSos = esSos
                            )
                            remoteEntities.add(entity)

                            val type = parseIncidentType(tipo)
                            val (mapX, mapY) = calcularPosicionMapa(latitud, longitud)
                            val incident = Incident(
                                id = id,
                                type = type,
                                title = titulo,
                                description = if (descripcion.isNotBlank()) descripcion else "Reporte de ${type.label} registrado en la zona.",
                                reportedBy = authorName,
                                handle = handle,
                                isOfficial = false,
                                location = "San Juan de Lurigancho, Lima",
                                distanceMeters = calcularDistanciaMetros(latitud, longitud),
                                reportedAt = calcularTiempoRelativo(creadoEn),
                                status = "Reportado a Serenazgo · En atención",
                                mapX = mapX,
                                mapY = mapY,
                                photoBase64 = foto,
                                incidentStatus = parseIncidentStatus(estado),
                                witnessCount = witnessCount,
                                commentsCount = 0,
                                isVerifiedByAuthority = false,
                                isReportedAsFalse = falseReportsCount >= 3,
                                falseReportsCount = falseReportsCount,
                                latitude = latitud,
                                longitude = longitud
                            )
                            realIncidents.add(incident)
                        } catch (e: Exception) {
                            Log.w("INCIDENT_REPO", "Error procesando reporte Firestore: ${e.message}")
                        }
                    }

                    // Sincronizar en SQLite Room en segundo plano
                    CoroutineScope(Dispatchers.IO).launch {
                        try {
                            if (remoteEntities.isNotEmpty()) {
                                reporteDao.insertarTodos(remoteEntities)
                            }
                        } catch (e: Exception) {
                            Log.e("INCIDENT_REPO", "Error al guardar en Room: ${e.message}")
                        }
                    }

                    Log.d("INCIDENT_REPO", "🔥 [TIEMPO REAL] Emitiendo ${realIncidents.size} reportes desde Firebase Firestore")
                    trySend(realIncidents)
                }
            }

        awaitClose {
            listener.remove()
        }
    }

    override suspend fun getIncidents(): List<Incident> = withContext(Dispatchers.IO) {
        // 1. Sincronizar desde Firebase Firestore
        try {
            val snapshot = firestore.collection("reportes")
                .whereGreaterThan("venceEn", System.currentTimeMillis())
                .get()
                .await()

            val remoteEntities = snapshot.documents.mapNotNull { doc ->
                try {
                    val id = doc.getString("id") ?: doc.id
                    val tipo = doc.getString("tipo") ?: "ROBO"
                    val titulo = doc.getString("titulo") ?: "Incidente reportado"
                    val descripcion = doc.getString("descripcion") ?: ""
                    val latitud = doc.getDouble("latitud") ?: -12.019542
                    val longitud = doc.getDouble("longitud") ?: -77.014231
                    val foto = doc.getString("fotoBase64") ?: doc.getString("fotoUrl")
                    val creadoEn = doc.getLong("creadoEn") ?: System.currentTimeMillis()
                    val venceEn = doc.getLong("venceEn") ?: (creadoEn + 12 * 60 * 60 * 1000L)
                    val estado = doc.getString("estado") ?: "REPORTADO"
                    val esSos = doc.getBoolean("esSos") ?: false

                    ReporteEntity(
                        id = id,
                        usuarioId = doc.getString("usuarioId") ?: "usr_vecino_local",
                        tipo = tipo,
                        titulo = titulo,
                        descripcion = descripcion,
                        latitud = latitud,
                        longitud = longitud,
                        fotoUri = foto,
                        creadoEn = creadoEn,
                        venceEn = venceEn,
                        estado = estado,
                        esSos = esSos
                    )
                } catch (e: Exception) { null }
            }

            if (remoteEntities.isNotEmpty()) {
                reporteDao.insertarTodos(remoteEntities)
                Log.d("INCIDENT_REPO", "✅ Sincronizados ${remoteEntities.size} reportes de Firestore a SQLite")
            }
        } catch (e: Exception) {
            Log.w("INCIDENT_REPO", "⚠️ No se pudo consultar Firestore, intentando Node.js: ${e.message}")
            try {
                val response = apiService.getReports()
                val entities = response.data.map { dto ->
                    ReporteEntity(
                        id = dto.id,
                        usuarioId = dto.usuarioId,
                        tipo = dto.tipo,
                        titulo = dto.titulo,
                        descripcion = dto.descripcion,
                        latitud = dto.latitud,
                        longitud = dto.longitud,
                        fotoUri = dto.fotoUrl,
                        creadoEn = System.currentTimeMillis(),
                        venceEn = System.currentTimeMillis() + 12 * 60 * 60 * 1000L,
                        estado = dto.estado,
                        esSos = dto.esSos
                    )
                }
                reporteDao.insertarTodos(entities)
            } catch (err: Exception) {
                Log.w("INCIDENT_REPO", "⚠️ Sin red hacia Node.js, usando caché SQLite local.")
            }
        }

        // 2. Leer de SQLite Room local (solo vigentes)
        val reportesLocales = try {
            reporteDao.obtenerListaVigentes(System.currentTimeMillis())
        } catch (e: Exception) {
            emptyList()
        }

        val realIncidents = reportesLocales.map { entityToIncident(it) }
        realIncidents
    }

    override suspend fun getIncidentById(id: String): Incident? = withContext(Dispatchers.IO) {
        // 1. Buscar en SQLite local
        val local = reporteDao.obtenerPorId(id)
        if (local != null) {
            return@withContext entityToIncident(local)
        }

        // 2. Buscar en Firebase Firestore
        try {
            val doc = firestore.collection("reportes").document(id).get().await()
            if (doc.exists()) {
                val tipo = doc.getString("tipo") ?: "ROBO"
                val titulo = doc.getString("titulo") ?: "Incidente reportado"
                val descripcion = doc.getString("descripcion") ?: ""
                val latitud = doc.getDouble("latitud") ?: -12.019542
                val longitud = doc.getDouble("longitud") ?: -77.014231
                val foto = doc.getString("fotoBase64") ?: doc.getString("fotoUrl")
                val creadoEn = doc.getLong("creadoEn") ?: System.currentTimeMillis()
                val estado = doc.getString("estado") ?: "REPORTADO"
                val usuarioNombre = doc.getString("usuarioNombre") ?: "Vecino SJL"
                val type = parseIncidentType(tipo)
                val (mapX, mapY) = calcularPosicionMapa(latitud, longitud)

                return@withContext Incident(
                    id = id,
                    type = type,
                    title = titulo,
                    description = if (descripcion.isNotBlank()) descripcion else "Reporte de ${type.label} registrado en la zona.",
                    reportedBy = usuarioNombre,
                    handle = "@vecino_lima",
                    isOfficial = false,
                    location = "San Juan de Lurigancho, Lima",
                    distanceMeters = calcularDistanciaMetros(latitud, longitud),
                    reportedAt = calcularTiempoRelativo(creadoEn),
                    status = "Reportado a Serenazgo · En atención",
                    mapX = mapX,
                    mapY = mapY,
                    photoBase64 = foto,
                    incidentStatus = parseIncidentStatus(estado),
                    witnessCount = doc.getLong("witnessCount")?.toInt() ?: 1,
                    commentsCount = 0,
                    isVerifiedByAuthority = false,
                    latitude = latitud,
                    longitude = longitud
                )
            }
        } catch (e: Exception) {
            Log.w("INCIDENT_REPO", "No se encontró en Firestore: ${e.message}")
        }

        null
    }

    override suspend fun toggleWitness(incidentId: String, hasWitnessed: Boolean): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val delta = if (hasWitnessed) -1L else 1L
            firestore.collection("reportes").document(incidentId)
                .update("witnessCount", FieldValue.increment(delta))
                .await()
            Log.d("INCIDENT_REPO", "✅ Respaldo actualizado en Firestore para reporte $incidentId (delta: $delta)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("INCIDENT_REPO", "Error actualizando respaldo en Firestore: ${e.message}")
            Result.failure(e)
        }
    }

    override suspend fun reportFalsehood(incidentId: String, reason: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val reportRef = firestore.collection("reportes").document(incidentId)
            reportRef.update("falseReportsCount", FieldValue.increment(1L)).await()

            // Registrar motivo en subcolección para auditoría
            val moderacionDoc = hashMapOf(
                "motivo" to reason,
                "reportadoEn" to System.currentTimeMillis(),
                "usuarioCorreo" to (SessionManager.currentUser.value?.correo ?: "anonimo")
            )
            reportRef.collection("moderacion_falsedad").add(moderacionDoc).await()

            Log.d("INCIDENT_REPO", "🚩 Reporte de falsedad registrado en Firestore: $incidentId ($reason)")
            Result.success(Unit)
        } catch (e: Exception) {
            Log.e("INCIDENT_REPO", "Error reportando falsedad en Firestore: ${e.message}")
            Result.failure(e)
        }
    }
}
