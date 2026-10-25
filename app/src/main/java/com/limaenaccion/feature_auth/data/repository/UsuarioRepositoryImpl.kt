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
                    Log.d("LOGIN_FIREBASE", "✅ Contraseña coincide con Firestore")
                    usuarioDao.guardarUsuario(usuario)
                    return@runCatching usuario
                } else {
                    Log.w("LOGIN_FIREBASE", "❌ Contraseña no coincide en Firestore")
                    return@runCatching null
                }
            }

            // Si el documento en Firestore no tiene contraseña guardada,
            // verificar si en SQLite local coincide la contraseña para este correo
            val localUser = usuarioDao.autenticarLocal(emailLimpio, password)
            if (localUser != null) {
                Log.d("LOGIN_FIREBASE", "✅ Contraseña validada contra SQLite local")
                usuarioDao.guardarUsuario(usuario)
                return@runCatching usuario
            }

            // Si no hay contraseña verificable ni en Firestore ni en SQLite, rechazar el ingreso
            Log.w("LOGIN_FIREBASE", "❌ No se pudo validar contraseña para '$emailLimpio'. Login rechazado.")
            null
        }
    }

    override suspend fun actualizarUsuario(usuario: Usuario): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            try {
                val colecciones = listOf(coleccionUsuarios, coleccionUsers)
                val updates = mutableMapOf<String, Any>()
                if (usuario.fullName.isNotBlank()) {
                    updates["fullName"] = usuario.fullName
                    updates["nombreCompleto"] = usuario.fullName
                }
                if (usuario.phoneNumber.isNotBlank()) {
                    updates["phoneNumber"] = usuario.phoneNumber
                    updates["telefono"] = usuario.phoneNumber
                }
                if (usuario.gender.isNotBlank()) {
                    updates["gender"] = usuario.gender
                    updates["genero"] = usuario.gender
                }
                if (usuario.district.isNotBlank()) {
                    updates["district"] = usuario.district
                    updates["distrito"] = usuario.district
                }
                if (usuario.neighborhood.isNotBlank()) {
                    updates["neighborhood"] = usuario.neighborhood
                    updates["urbanizacion"] = usuario.neighborhood
                }

                var actualizado = false
                val emailLimpio = usuario.email.trim().lowercase()

                for (col in colecciones) {
                    if (emailLimpio.isNotBlank()) {
                        val queryEmail = col.whereEqualTo("email", emailLimpio).get().await()
                        for (doc in queryEmail.documents) {
                            doc.reference.update(updates).await()
                            actualizado = true
                            Log.d("UPDATE_FIREBASE", "✅ Firestore actualizado por email en ${col.id}: ${doc.id}")
                        }
                        val queryCorreo = col.whereEqualTo("correo", emailLimpio).get().await()
                        for (doc in queryCorreo.documents) {
                            doc.reference.update(updates).await()
                            actualizado = true
                            Log.d("UPDATE_FIREBASE", "✅ Firestore actualizado por correo en ${col.id}: ${doc.id}")
                        }
                    }

                    if (!actualizado && usuario.dni.isNotBlank()) {
                        val queryDni = col.whereEqualTo("dni", usuario.dni.trim()).get().await()
                        for (doc in queryDni.documents) {
                            doc.reference.update(updates).await()
                            actualizado = true
                            Log.d("UPDATE_FIREBASE", "✅ Firestore actualizado por DNI en ${col.id}: ${doc.id}")
                        }
                    }

                    if (!actualizado && usuario.uid.isNotBlank() && !usuario.uid.contains("@")) {
                        try {
                            col.document(usuario.uid).update(updates).await()
                            actualizado = true
                            Log.d("UPDATE_FIREBASE", "✅ Firestore actualizado por UID en ${col.id}: ${usuario.uid}")
                        } catch (_: Exception) {}
                    }
                }
            } catch (e: Exception) {
                Log.e("UPDATE_FIREBASE", "Error al actualizar en Firestore: ${e.message}", e)
            }
            usuarioDao.guardarUsuario(usuario)
            Unit
        }
    }

    override suspend fun eliminarCuenta(email: String, dni: String): Result<Unit> = withContext(Dispatchers.IO) {
        runCatching {
            val emailLimpio = email.trim().lowercase()
            val dniLimpio = dni.trim()
            val colecciones = listOf(coleccionUsuarios, coleccionUsers)

            try {
                for (col in colecciones) {
                    if (emailLimpio.isNotBlank()) {
                        val qEmail = col.whereEqualTo("email", emailLimpio).get().await()
                        for (doc in qEmail.documents) {
                            doc.reference.delete().await()
                            Log.d("DELETE_FIREBASE", "🗑️ Doc ${doc.id} eliminado de ${col.id} por email")
                        }
                        val qCorreo = col.whereEqualTo("correo", emailLimpio).get().await()
                        for (doc in qCorreo.documents) {
                            doc.reference.delete().await()
                            Log.d("DELETE_FIREBASE", "🗑️ Doc ${doc.id} eliminado de ${col.id} por correo")
                        }
                    }
                    if (dniLimpio.isNotBlank()) {
                        val qDni = col.whereEqualTo("dni", dniLimpio).get().await()
                        for (doc in qDni.documents) {
                            doc.reference.delete().await()
                            Log.d("DELETE_FIREBASE", "🗑️ Doc ${doc.id} eliminado por DNI de ${col.id}")
                        }
                    }
                }
            } catch (e: Exception) {
                Log.w("DELETE_FIREBASE", "Error al eliminar en Firestore remoto (${e.message})")
            }

            usuarioDao.eliminarPorCorreo(emailLimpio)
            usuarioDao.limpiarSesion()
            Unit
        }
    }
}
