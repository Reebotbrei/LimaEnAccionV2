package com.limaenaccion.feature_alerts.data.repository

import android.app.Application
import android.util.Log
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.dao.ReporteDao
import com.limaenaccion.core.data.local.entities.ReporteEntity
import com.limaenaccion.core.data.network.ApiService
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.feature_alerts.data.model.Incident
import com.limaenaccion.feature_alerts.data.model.IncidentStatus
import com.limaenaccion.feature_alerts.data.model.IncidentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class IncidentRepositoryImpl(
    application: Application,
    private val apiService: ApiService = RetrofitClient.instance,
    private val reporteDao: ReporteDao = AppDatabase.getInstance(application).reporteDao(),
    private val mockRepository: IncidentRepository = MockIncidentRepository()
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

    override suspend fun getIncidents(): List<Incident> = withContext(Dispatchers.IO) {
        val baseMocks = mockRepository.getIncidents()

        // 1. Intentar sincronizar desde el backend Node.js
        try {
            val response = apiService.getReports()
            val remoteReports = response.data
            val entities = remoteReports.map { dto ->
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
            Log.d("INCIDENT_REPO", "✅ Sincronizados ${entities.size} reportes de Node.js a SQLite")
        } catch (e: Exception) {
            Log.w("INCIDENT_REPO", "⚠️ No se pudo conectar con el servidor, leyendo caché SQLite: ${e.message}")
        }

        // 2. Leer todos los reportes de la base de datos SQLite Room
        val reportesLocales = try {
            reporteDao.obtenerListaTodos()
        } catch (e: Exception) {
            Log.e("INCIDENT_REPO", "Error al leer de SQLite: ${e.message}")
            emptyList()
        }

        // 3. Mapear entidades de SQLite a Incidentes para la UI
        val realIncidents = reportesLocales.map { entity ->
            val type = parseIncidentType(entity.tipo)

            Incident(
                id = entity.id,
                type = type,
                title = entity.titulo,
                description = if (!entity.descripcion.isNullOrBlank()) entity.descripcion else "Reporte de ${type.label} registrado en la zona.",
                reportedBy = "Tú (Vecino)",
                handle = "@vecino_sjl",
                isOfficial = false,
                location = "Caja de Agua, SJL",
                distanceMeters = 80,
                reportedAt = "ahora",
                status = "Reportado a Serenazgo · En revisión",
                mapX = 0.5f,
                mapY = 0.5f,
                photoBase64 = entity.fotoUri,
                incidentStatus = parseIncidentStatus(entity.estado),
                witnessCount = 1,
                commentsCount = 0,
                isVerifiedByAuthority = false
            )
        }

        // Devolver primero los reportes reales creados seguidos de los de prueba
        realIncidents + baseMocks
    }

    override suspend fun getIncidentById(id: String): Incident? = withContext(Dispatchers.IO) {
        val entity = reporteDao.obtenerPorId(id)
        if (entity != null) {
            val type = parseIncidentType(entity.tipo)
            Incident(
                id = entity.id,
                type = type,
                title = entity.titulo,
                description = if (!entity.descripcion.isNullOrBlank()) entity.descripcion else "Reporte de ${type.label} registrado en la zona.",
                reportedBy = "Tú (Vecino)",
                handle = "@vecino_sjl",
                isOfficial = false,
                location = "Caja de Agua, SJL",
                distanceMeters = 80,
                reportedAt = "ahora",
                status = "Reportado a Serenazgo · En revisión",
                mapX = 0.5f,
                mapY = 0.5f,
                photoBase64 = entity.fotoUri,
                incidentStatus = parseIncidentStatus(entity.estado),
                witnessCount = 1,
                commentsCount = 0,
                isVerifiedByAuthority = false
            )
        } else {
            mockRepository.getIncidentById(id)
        }
    }
}
