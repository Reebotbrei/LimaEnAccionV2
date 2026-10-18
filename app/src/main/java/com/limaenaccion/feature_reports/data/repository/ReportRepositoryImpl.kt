package com.limaenaccion.feature_reports.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.limaenaccion.feature_reports.data.model.Reporte
import kotlinx.coroutines.tasks.await

class ReportRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ReportRepository {

    override suspend fun crearReporte(reporte: Reporte): Result<String> {
        return try {
            val docRef = firestore.collection("reportes").document()
            val reporteConId = reporte.copy(id = docRef.id)
            docRef.set(reporteConId).await()
            Result.success(docRef.id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun obtenerReportes(): Result<List<Reporte>> {
        return try {
            val snapshot = firestore.collection("reportes").get().await()
            val lista = snapshot.toObjects(Reporte::class.java)
            Result.success(lista)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}