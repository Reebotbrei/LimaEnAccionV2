package com.limaenaccion.feature_alerts.data.repository

import com.limaenaccion.feature_alerts.data.model.Incident
import kotlinx.coroutines.flow.Flow

interface IncidentRepository {
    suspend fun getIncidents(): List<Incident>
    suspend fun getIncidentById(id: String): Incident?
    fun observeIncidents(): Flow<List<Incident>>
    suspend fun toggleWitness(incidentId: String, hasWitnessed: Boolean): Result<Unit>
    suspend fun reportFalsehood(incidentId: String, reason: String): Result<Unit>
}
