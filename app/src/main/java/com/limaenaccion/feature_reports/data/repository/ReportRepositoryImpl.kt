package com.limaenaccion.feature_reports.data.repository

import android.content.Context
import android.util.Log
import com.limaenaccion.core.data.local.AppDatabase
import com.limaenaccion.core.data.local.dao.ReporteDao
import com.limaenaccion.core.data.local.entities.ReporteEntity
import com.limaenaccion.core.data.network.ApiService
import com.limaenaccion.core.data.network.RetrofitClient
import com.limaenaccion.core.data.network.model.CreateReportRequest
import com.limaenaccion.feature_reports.data.model.ReportType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class ReportRepositoryImpl(
    context: Context,
    private val apiService: ApiService = RetrofitClient.instance,
    private val reporteDao: ReporteDao = AppDatabase.getInstance(context).reporteDao()
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
        var reporteIdFinal = "rep_${System.currentTimeMillis()}"

        // 1. Persistencia remota en el Backend (Node.js por Retrofit)
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
            val response = apiService.createReport(request)
            reporteIdFinal = response.data.id
            Log.d("LIMA_BACKEND", "✅ [BACKEND NODE.JS] Reporte persistido en el servidor: $reporteIdFinal")
        } catch (e: Exception) {
            Log.w("LIMA_BACKEND", "⚠️ [BACKEND NODE.JS] Servidor no respondió o sin red. Se procederá con guardado local: ${e.message}")
        }

        // 2. Persistencia local en SQLite con Room (US-06 Caducidad 12h)
        try {
            val ahora = System.currentTimeMillis()
            val doceHorasMs = 12 * 60 * 60 * 1000L
            val entity = ReporteEntity(
                id = reporteIdFinal,
                usuarioId = "usr_vecino_local",
                tipo = tipoStr,
                titulo = title,
                descripcion = description,
                latitud = latitude,
                longitud = longitude,
                fotoUri = null,
                creadoEn = ahora,
                venceEn = ahora + doceHorasMs,
                estado = "EN_REVISION",
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
