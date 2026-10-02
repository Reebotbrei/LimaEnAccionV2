package com.limaenaccion.feature_emergency.data.repository

import com.limaenaccion.feature_emergency.data.model.EmergencyContact

interface EmergencyRepository {
    suspend fun getEmergencyDirectory(): List<EmergencyContact>
}