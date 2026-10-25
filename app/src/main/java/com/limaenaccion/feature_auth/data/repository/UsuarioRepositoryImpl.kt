package com.limaenaccion.feature_auth.data.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.limaenaccion.core.data.local.dao.UsuarioDao
import com.limaenaccion.feature_auth.data.model.Usuario
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class UsuarioRepositoryImpl(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance(),
    private val usuarioDao: UsuarioDao
) : UsuarioRepository {

    private val coleccionUsuarios = firestore.collection("usuarios")
    private val coleccionUsers = firestore.collection("users")

    override suspend fun registrarUsuario(usuario: Usuario): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            coleccionUsuarios.document(usuario.uid)
                .set(usuario)
                .await()
            usuarioDao.guardarUsuario(usuario)
            Unit
        }
    }

    override suspend fun obtenerUsuario(uid: String): Result<Usuario?> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                val snapshot = coleccionUsuarios.document(uid).get().await()
                val usuarioRemoto = snapshot.toObject(Usuario::class.java)
                if (usuarioRemoto != null) {
                    usuarioDao.guardarUsuario(usuarioRemoto)
                    return@runCatching usuarioRemoto
                }
            } catch (e: Exception) {
                Log.w("LOGIN_FIREBASE", "Fallo obtener usuario remoto: ${e.message}")
            }
            usuarioDao.obtenerUsuario(uid)
        }
    }

    override fun observarUsuarioLocal(uid: String): Flow<Usuario?> {
        return usuarioDao.obtenerUsuarioFlow(uid)
    }

    override suspend fun cerrarSesion(): Unit = withContext(Dispatchers.IO) {
        usuarioDao.limpiarSesion()
        Unit
    }

    override suspend fun loginConFirestore(email: String, password: String): Result<Usuario?> = withContext(Dispatchers.IO) {
        runCatching {
            val emailLimpio = email.trim()
            Log.d("LOGIN_FIREBASE", "Buscando en Firestore por email: '$emailLimpio'")

            val colecciones = listOf(coleccionUsuarios, coleccionUsers)
            var documentoEncontrado: com.google.firebase.firestore.DocumentSnapshot? = null

            for (col in colecciones) {
                val queries = listOf(
                    col.whereEqualTo("email", emailLimpio.lowercase()),
                    col.whereEqualTo("email", emailLimpio),
                    col.whereEqualTo("correo", emailLimpio.lowercase()),
                    col.whereEqualTo("correo", emailLimpio)
                )

                for (query in queries) {
                    try {
                        val snapshot = query.get().await()
                        if (!snapshot.isEmpty) {
                            documentoEncontrado = snapshot.documents.first()
                            Log.d("LOGIN_FIREBASE", "Documento encontrado en '${col.id}': ${documentoEncontrado.id}")
                            break
                        }
                    } catch (e: Exception) {
                        Log.e("LOGIN_FIREBASE", "Error query Firestore en '${col.id}': ${e.message}", e)
                    }
                }
                if (documentoEncontrado != null) break
            }

            if (documentoEncontrado == null) {
                Log.w("LOGIN_FIREBASE", "Usuario '$emailLimpio' no encontrado en Firestore")
                return@runCatching null
            }

            val doc = documentoEncontrado
            val usuario = doc.toObject(Usuario::class.java) ?: Usuario(
                uid = doc.id,
                fullName = doc.getString("fullName") ?: doc.getString("nombre") ?: doc.getString("nombreCompleto") ?: "",
                email = doc.getString("email") ?: doc.getString("correo") ?: emailLimpio,
                dni = doc.getString("dni") ?: "",
                phoneNumber = doc.getString("phoneNumber") ?: doc.getString("telefono") ?: "",
                district = doc.getString("district") ?: doc.getString("distrito") ?: "",
                neighborhood = doc.getString("neighborhood") ?: doc.getString("urbanizacion") ?: doc.getString("vecindario") ?: ""
            )

            val passDoc = doc.getString("password")
                ?: doc.getString("contrasena")
                ?: doc.getString("clave")

            if (passDoc != null) {
                if (passDoc == password) {
                    Log.d("LOGIN_FIREBASE", "Contraseña coincide con Firestore")
                    usuarioDao.guardarUsuario(usuario)
                    return@runCatching usuario
                } else {
                    Log.w("LOGIN_FIREBASE", "Contraseña no coincide en Firestore")
                    return@runCatching null
                }
            }

            // Si no tiene campo de contraseña en texto plano
            Log.d("LOGIN_FIREBASE", "Usuario autenticado por existencia en Firestore")
            usuarioDao.guardarUsuario(usuario)
            usuario
        }
    }
}
