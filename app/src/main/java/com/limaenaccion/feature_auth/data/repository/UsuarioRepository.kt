package com.limaenaccion.feature_auth.data.repository

import com.limaenaccion.feature_auth.data.model.Usuario
import kotlinx.coroutines.flow.Flow

interface UsuarioRepository {
    // 1. Guarda en Firestore (nube) y luego en Room (caché)
    suspend fun registrarUsuario(usuario: Usuario): Result<Unit>

    // 2. Trae de Firestore si hay internet, o de Room si está offline
    suspend fun obtenerUsuario(uid: String): Result<Usuario?>

    // 3. Observa los datos del usuario en tiempo real desde la caché local (para el Perfil)
    fun observarUsuarioLocal(uid: String): Flow<Usuario?>

    // 4. Limpia la caché local al cerrar sesión
    suspend fun cerrarSesion()

    // 5. Verifica credenciales contra Firestore (fallback cuando el backend está caído o sin red)
    suspend fun loginConFirestore(email: String, password: String): Result<Usuario?>

    // 6. Actualiza los datos del usuario en Firestore (nube) y en Room (caché local)
    suspend fun actualizarUsuario(usuario: Usuario): Result<Unit>
}
