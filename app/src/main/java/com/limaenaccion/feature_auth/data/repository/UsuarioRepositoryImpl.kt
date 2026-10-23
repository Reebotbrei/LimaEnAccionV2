package com.limaenaccion.feature_auth.data.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.limaenaccion.core.data.local.dao.UsuarioDao
import com.limaenaccion.feature_auth.data.model.Usuario
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await

class UsuarioRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val usuarioDao: UsuarioDao
) : UsuarioRepository {

    private val coleccionUsuarios = firestore.collection("usuarios")

    override suspend fun registrarUsuario(usuario: Usuario): Result<Unit> {
        return runCatching {
            // 1. Guardar en la Nube (Firebase Firestore)
            coleccionUsuarios.document(usuario.uid)
                .set(usuario)
                .await()

            // 2. Guardar en Caché Local (Room)
            usuarioDao.guardarUsuario(usuario)
        }
    }

    override suspend fun obtenerUsuario(uid: String): Result<Usuario?> {
        return runCatching {
            try {
                // Intento 1: Traer de Firebase Firestore (datos frescos de la nube)
                val snapshot = coleccionUsuarios.document(uid).get().await()
                val usuarioRemoto = snapshot.toObject(Usuario::class.java)

                if (usuarioRemoto != null) {
                    // Actualizamos la caché local
                    usuarioDao.guardarUsuario(usuarioRemoto)
                    return@runCatching usuarioRemoto
                }
            } catch (e: Exception) {
                // Si falla (ej. sin internet), no hacemos crash y pasamos a la caché
            }

            // Intento 2: Si no hay internet o falló Firestore, sacamos de Room (Caché)
            usuarioDao.obtenerUsuario(uid)
        }
    }

    override fun observarUsuarioLocal(uid: String): Flow<Usuario?> {
        return usuarioDao.obtenerUsuarioFlow(uid)
    }

    override suspend fun cerrarSesion() {
        usuarioDao.limpiarSesion()
    }
}
