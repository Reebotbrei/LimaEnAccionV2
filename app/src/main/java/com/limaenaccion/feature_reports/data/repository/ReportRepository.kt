package com.limaenaccion.feature_reports.data.repository

import com.limaenaccion.feature_reports.data.model.ReportType

interface ReportRepository {
    suspend fun publishReport(
        type: ReportType,
        title: String,
        description: String,
        latitude: Double,
        longitude: Double,
        photoBase64: String?,
        isAnonymous: Boolean = false
    ): Result<String>
}
