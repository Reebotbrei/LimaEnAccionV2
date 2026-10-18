package com.limaenaccion.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.limaenaccion.core.data.local.entities.ReporteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ReporteDao {

    // US-06: Caducidad de pines a las 12 horas en SQLite (reactivo con Flow)
    @Query("SELECT * FROM reporte_cache WHERE venceEn > :ahora ORDER BY creadoEn DESC")
    fun observarVigentes(ahora: Long): Flow<List<ReporteEntity>>

    @Query("SELECT * FROM reporte_cache ORDER BY creadoEn DESC")
    fun observarTodos(): Flow<List<ReporteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertar(reporte: ReporteEntity): Long

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertarTodos(reportes: List<ReporteEntity>)

    @Query("SELECT * FROM reporte_cache WHERE id = :id")
    fun obtenerPorId(id: String): ReporteEntity?

    @Query("DELETE FROM reporte_cache WHERE id = :id")
    fun eliminarPorId(id: String): Int
}
