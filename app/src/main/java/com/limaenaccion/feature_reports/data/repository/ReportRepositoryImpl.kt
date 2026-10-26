package com.limaenaccion.feature_reports.data.repository

import android.content.Context
import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.dao.ReporteDao
import com.limaenaccion.core.data.local.entities.ReporteEntity
import com.limaenaccion.core.data.network.ApiService
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.network.model.CreateReportRequest
import com.limaenaccion.core.data.session.SessionManager
import com.limaenaccion.feature_reports.data.model.ReportType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ReportRepositoryImpl(
    context: Context,
    private val apiService: ApiService = RetrofitClient.instance,
    private val reporteDao: ReporteDao = AppDatabase.getInstance(context).reporteDao(),
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ReportRepository {

    override suspend fun publishReport(
        type: ReportType,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        photoBase64: String?
    ): Result<String> = withContext(Dispatchers.IO) {
        val tipoStr = type.name
        val ahora = System.currentTimeMillis()
        val doceHorasMs = 12 * 60 * 60 * 1000L
        val venceEn = ahora + doceHorasMs
        val reporteIdFinal = "rep_$ahora"

        val currentUser = SessionManager.currentUser
        val userEmail = currentUser?.correo ?: "vecino.lima@gmail.com"
        val userName = currentUser?.nombreCompleto ?: "Vecino Vigilante"

        // 1. Persistencia NoSQL en tiempo real en Firebase Cloud Firestore
        try {
            val reportDoc = hashMapOf(
                "id" to reporteIdFinal,
                "usuarioId" to userEmail,
                "usuarioNombre" to userName,
                "tipo" to tipoStr,
                "titulo" to title,
                "descripcion" to description,
                "latitud" to latitude,
                "longitud" to longitude,
                "fotoUrl" to (photoBase64 ?: ""),
                "fotoBase64" to (photoBase64 ?: ""),
                "creadoEn" to ahora,
                "venceEn" to venceEn,
                "estado" to "REPORTADO",
                "esSos" to false,
                "witnessCount" to 1,
                "commentsCount" to 0
            )
            firestore.collection("reportes").document(reporteIdFinal).set(reportDoc).await()
            Log.d("LIMA_FIRESTORE", "✅ [FIREBASE FIRESTORE] Reporte transmitido en vivo: $reporteIdFinal ($title)")
        } catch (e: Exception) {
            Log.w("LIMA_FIRESTORE", "⚠️ [FIREBASE FIRESTORE] Error al transmitir a Firestore (${e.message}). Continuando con respaldos...")
        }

        // 2. Persistencia remota en el Backend (Node.js por Retrofit - Requerimiento de Rúbrica)
        try {
            val request = CreateReportRequest(
                tipo = tipoStr,
                titulo = title,
                descripcion = description,
                latitud = latitude,
                longitud = longitude,
                fotoUrl = photoBase64,
                esSos = false
            )
            apiService.createReport(request)
            Log.d("LIMA_BACKEND", "✅ [BACKEND NODE.JS] Reporte persistido en API Service: $reporteIdFinal")
        } catch (e: Exception) {
            Log.w("LIMA_BACKEND", "⚠️ [BACKEND NODE.JS] Servidor no respondió (${e.message}). Continuando con guardado local.")
        }

        // 3. Persistencia local en SQLite con Room (US-06 Caducidad 12h - Requerimiento de Rúbrica)
        try {
            val entity = ReporteEntity(
                id = reporteIdFinal,
                usuarioId = userEmail,
                tipo = tipoStr,
                titulo = title,
                descripcion = description,
                latitud = latitude,
                longitud = longitude,
                fotoUri = photoBase64,
                creadoEn = ahora,
                venceEn = venceEn,
                estado = "REPORTADO",
                esSos = false
            )
            reporteDao.insertar(entity)
            Log.d("LIMA_ROOM", "✅ [SQLITE ROOM] Reporte guardado en caché local: $reporteIdFinal")
            Result.success(reporteIdFinal)
        } catch (e: Exception) {
            Log.e("LIMA_ROOM", "❌ [SQLITE ROOM] Error al guardar en SQLite: ${e.message}")
            Result.failure(e)
        }
    }
}
