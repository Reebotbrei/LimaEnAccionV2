package com.limaenaccion.feature_reports.data.repository

import com.limaenaccion.feature_reports.data.model.Reporte

interface ReportRepository {
    suspend fun crearReporte(reporte: Reporte): Result<String>
    suspend fun obtenerReportes(): Result<List<Reporte>>
}