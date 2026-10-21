package com.limaenaccion.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.limaenaccion.core.data.local.entities.ContactoEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ContactoDao {

    @Query("SELECT * FROM contacto_auxilio WHERE esPrincipal = 1 LIMIT 1")
    fun obtenerContactoPrincipal(): Flow<ContactoEntity?>

    @Query("SELECT * FROM contacto_auxilio ORDER BY id DESC")
    fun obtenerTodos(): Flow<List<ContactoEntity>>

    @Query("SELECT * FROM contacto_auxilio ORDER BY id DESC")
    fun obtenerListaTodos(): List<ContactoEntity>

    @Query("SELECT COUNT(*) FROM contacto_auxilio")
    fun contarContactos(): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarContacto(contacto: ContactoEntity): Long

    @Query("UPDATE contacto_auxilio SET esPrincipal = 0 WHERE esPrincipal = 1")
    fun desmarcarPrincipales(): Int

    @Query("DELETE FROM contacto_auxilio WHERE id = :id")
    fun eliminarPorId(id: Long): Int

    @Query("DELETE FROM contacto_auxilio")
    fun eliminarTodos(): Int
}
