package com.limaenaccion.core.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.limaenaccion.feature_auth.data.model.Usuario
import kotlinx.coroutines.flow.Flow

@Dao
interface UsuarioDao {

    // 1. Obtener usuario en tiempo real (Flow)
    @Query("SELECT * FROM usuario_cache WHERE uid = :uid LIMIT 1")
    fun obtenerUsuarioFlow(uid: String): Flow<Usuario?>

    // 2. Obtener usuario síncrono
    @Query("SELECT * FROM usuario_cache WHERE uid = :uid LIMIT 1")
    fun obtenerUsuario(uid: String): Usuario?

    // 3. Guardar usuario retornando Long (evita el error 'signature V' de KSP)
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun guardarUsuario(usuario: Usuario): Long

    // 4. Limpiar caché retornando Int (filas eliminadas)
    @Query("DELETE FROM usuario_cache")
    fun limpiarSesion(): Int
}
