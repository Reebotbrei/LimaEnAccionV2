package com.limaenaccion.feature_alerts.data.repository

import com.limaenaccion.feature_alerts.data.model.Incident

interface IncidentRepository {
    suspend fun getIncidents(): List<Incident>
    suspend fun getIncidentById(id: String): Incident?
}