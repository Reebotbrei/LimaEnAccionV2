package com.limaenaccion.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.limaenaccion.core.data.local.entities.UsuarioEntity

@Dao
interface UsuarioDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarUsuario(usuario: UsuarioEntity): Long

    @Query("SELECT * FROM usuarios WHERE LOWER(correo) = LOWER(:correo) LIMIT 1")
    fun obtenerPorCorreo(correo: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios WHERE LOWER(correo) = LOWER(:correo) AND contrasena = :contrasena LIMIT 1")
    fun autenticarLocal(correo: String, contrasena: String): UsuarioEntity?

    @Query("SELECT * FROM usuarios ORDER BY fechaRegistro DESC")
    fun obtenerTodos(): List<UsuarioEntity>

    @Query("DELETE FROM usuarios WHERE LOWER(correo) = LOWER(:correo)")
    fun eliminarPorCorreo(correo: String): Int
}
