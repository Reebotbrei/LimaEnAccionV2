package com.limaenaccion.feature_alerts.data.repository

import com.limaenaccion.feature_alerts.data.model.Incident
import kotlinx.coroutines.flow.Flow

interface IncidentRepository {
    suspend fun getIncidents(): List<Incident>
    suspend fun getIncidentById(id: String): Incident?
    fun observeIncidents(): Flow<List<Incident>>
}
