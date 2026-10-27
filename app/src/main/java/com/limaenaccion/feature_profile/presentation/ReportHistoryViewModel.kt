package com.limaenaccion.feature_profile.presentation

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.firestore.FirebaseFirestore
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.dao.ReporteDao
import com.limaenaccion.core.data.local.entities.ReporteEntity
import com.limaenaccion.core.data.session.SessionManager
import com.limaenaccion.feature_alerts.data.model.IncidentType
import com.limaenaccion.feature_profile.data.model.ReportHistoryItem
import com.limaenaccion.feature_profile.data.model.ReportReviewStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class ReportHistoryViewModel @JvmOverloads constructor(
    application: Application,
    private val reporteDao: ReporteDao = AppDatabase.getInstance(application).reporteDao(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : AndroidViewModel(application) {

    private val _uiState = MutableStateFlow<List<ReportHistoryItem>>(emptyList())
    val uiState: StateFlow<List<ReportHistoryItem>> = _uiState.asStateFlow()

    init {
        loadUserReports()
    }

    private fun parseIncidentType(tipo: String): IncidentType {
        val clean = tipo.trim().uppercase()
        return try {
            IncidentType.valueOf(clean)
        } catch (e: Exception) {
            when {
                clean.contains("ROBO") || clean.contains("ASALTO") -> IncidentType.ROBO
                clean.contains("FUEGO") || clean.contains("INCENDIO") -> IncidentType.INCENDIO
                clean.contains("MEDIC") || clean.contains("SALUD") || clean.contains("AMBULANCIA") -> IncidentType.EMERGENCIA_MEDICA
                clean.contains("ACCIDENTE") || clean.contains("CHOQUE") -> IncidentType.ACCIDENTE
                clean.contains("VANDAL") -> IncidentType.VANDALISMO
                else -> IncidentType.ACTIVIDAD_INUSUAL
            }
        }
    }

    private fun parseReportStatus(estado: String): ReportReviewStatus {
        val clean = estado.trim().uppercase()
        return when {
            clean.contains("RESUELTO") || clean.contains("CERRADO") -> ReportReviewStatus.RESUELTO
            clean.contains("ATENCION") || clean.contains("CURSO") || clean.contains("VERIFICADO") -> ReportReviewStatus.VERIFICADO
            else -> ReportReviewStatus.EN_REVISION
        }
    }

    private fun formatDate(timestamp: Long): String {
        return try {
            val sdf = SimpleDateFormat("dd MMM yyyy, hh:mm a", Locale("es", "PE"))
            sdf.format(Date(timestamp))
        } catch (e: Exception) {
            "Reciente"
        }
    }

    private fun loadUserReports() {
        val currentUser = SessionManager.currentUser.value
        val userEmail = currentUser?.correo ?: "vecino.lima@gmail.com"

        // 1. Cargar inmediatamente desde SQLite Room (Offline-first)
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val cached = reporteDao.obtenerListaPorUsuario(userEmail)
                val items = cached.map { entity ->
                    ReportHistoryItem(
                        id = entity.id,
                        type = parseIncidentType(entity.tipo),
                        title = entity.titulo,
                        dateLabel = formatDate(entity.creadoEn),
                        status = parseReportStatus(entity.estado)
                    )
                }
                _uiState.value = items
            } catch (e: Exception) {
                Log.e("REPORT_HISTORY_VM", "Error al leer caché local de reportes: ${e.message}")
            }
        }

        // 2. Suscribirse en tiempo real a Firebase Cloud Firestore filtrando por usuarioId
        try {
            firestore.collection("reportes")
                .whereEqualTo("usuarioId", userEmail)
                .addSnapshotListener { snapshot, error ->
                    if (error != null) {
                        Log.w("REPORT_HISTORY_VM", "Error en listener de Firestore: ${error.message}")
                        return@addSnapshotListener
                    }

                    if (snapshot != null) {
                        val historyList = mutableListOf<ReportHistoryItem>()
                        val entitiesToCache = mutableListOf<ReporteEntity>()

                        for (doc in snapshot.documents) {
                            try {
                                val id = doc.getString("id") ?: doc.id
                                val tipoStr = doc.getString("tipo") ?: "ROBO"
                                val titulo = doc.getString("titulo") ?: "Reporte vecinal"
                                val descripcion = doc.getString("descripcion") ?: ""
                                val latitud = doc.getDouble("latitud") ?: -12.019542
                                val longitud = doc.getDouble("longitud") ?: -77.014231
                                val foto = doc.getString("fotoBase64") ?: doc.getString("fotoUrl")
                                val creadoEn = doc.getLong("creadoEn") ?: System.currentTimeMillis()
                                val venceEn = doc.getLong("venceEn") ?: (creadoEn + 12 * 60 * 60 * 1000L)
                                val estado = doc.getString("estado") ?: "REPORTADO"
                                val esSos = doc.getBoolean("esSos") ?: false

                                historyList.add(
                                    ReportHistoryItem(
                                        id = id,
                                        type = parseIncidentType(tipoStr),
                                        title = titulo,
                                        dateLabel = formatDate(creadoEn),
                                        status = parseReportStatus(estado)
                                    )
                                )

                                entitiesToCache.add(
                                    ReporteEntity(
                                        id = id,
                                        usuarioId = userEmail,
                                        tipo = tipoStr,
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
                                )
                            } catch (e: Exception) {
                                Log.w("REPORT_HISTORY_VM", "Error procesando item de historial: ${e.message}")
                            }
                        }

                        // Ordenar por fecha descendente (más recientes primero)
                        _uiState.value = historyList.sortedByDescending { it.id }

                        // Actualizar en SQLite en segundo plano
                        viewModelScope.launch(Dispatchers.IO) {
                            if (entitiesToCache.isNotEmpty()) {
                                reporteDao.insertarTodos(entitiesToCache)
                            }
                        }
                    }
                }
        } catch (e: Exception) {
            Log.e("REPORT_HISTORY_VM", "No se pudo conectar a Firestore: ${e.message}")
        }
    }
}
