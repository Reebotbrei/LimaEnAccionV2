package com.limaenaccion.core.data.local

import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import com.limaenaccion.core.data.local.entities.UsuarioEntity

/**
 * Gestor SQLite nativo ([SQLiteOpenHelper]) para almacenamiento local de usuarios registrados.
 *IMPORTANTE: Cumple con la regla de "SQLite, no recordar sesión" — esta base de datos almacena
 * el catálogo local de cuentas registradas, pero NUNCA persiste sesiones activas ni tokens de auto-login.
 */
class SqliteDatabaseHelper(context: Context) :
    SQLiteOpenHelper(context.applicationContext, DATABASE_NAME, null, DATABASE_VERSION) {

    companion object {
        const val DATABASE_NAME = "lima_en_accion_sqlite.db"
        const val DATABASE_VERSION = 1

        const val TABLE_USUARIOS = "usuarios"
        const val COL_CORREO = "correo"
        const val COL_DNI = "dni"
        const val COL_NOMBRE = "nombre_completo"
        const val COL_GENERO = "genero"
        const val COL_TELEFONO = "telefono"
        const val COL_CONTRASENA = "contrasena"
        const val COL_DISTRITO = "distrito"
        const val COL_URBANIZACION = "urbanizacion"
        const val COL_FECHA_REGISTRO = "fecha_registro"

        @Volatile
        private var INSTANCE: SqliteDatabaseHelper? = null

        fun getInstance(context: Context): SqliteDatabaseHelper {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: SqliteDatabaseHelper(context).also { INSTANCE = it }
            }
        }
    }

    override fun onCreate(db: SQLiteDatabase) {
        val createTableSql = """
            CREATE TABLE IF NOT EXISTS $TABLE_USUARIOS (
                $COL_CORREO TEXT PRIMARY KEY,
                $COL_DNI TEXT NOT NULL,
                $COL_NOMBRE TEXT NOT NULL,
                $COL_GENERO TEXT NOT NULL,
                $COL_TELEFONO TEXT NOT NULL,
                $COL_CONTRASENA TEXT NOT NULL,
                $COL_DISTRITO TEXT NOT NULL,
                $COL_URBANIZACION TEXT NOT NULL,
                $COL_FECHA_REGISTRO INTEGER NOT NULL
            )
        """.trimIndent()
        db.execSQL(createTableSql)
    }

    override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int) {
        db.execSQL("DROP TABLE IF EXISTS $TABLE_USUARIOS")
        onCreate(db)
    }

    fun guardarUsuario(usuario: UsuarioEntity): Long {
        val db = writableDatabase
        val values = ContentValues().apply {
            put(COL_CORREO, usuario.correo.lowercase().trim())
            put(COL_DNI, usuario.dni)
            put(COL_NOMBRE, usuario.nombreCompleto)
            put(COL_GENERO, usuario.genero)
            put(COL_TELEFONO, usuario.telefono)
            put(COL_CONTRASENA, usuario.contrasena)
            put(COL_DISTRITO, usuario.distrito)
            put(COL_URBANIZACION, usuario.urbanizacion)
            put(COL_FECHA_REGISTRO, usuario.fechaRegistro)
        }
        return db.insertWithOnConflict(
            TABLE_USUARIOS,
            null,
            values,
            SQLiteDatabase.CONFLICT_REPLACE
        )
    }

    fun eliminarUsuario(correo: String): Int {
        val db = writableDatabase
        return db.delete(
            TABLE_USUARIOS,
            "LOWER($COL_CORREO) = LOWER(?)",
            arrayOf(correo.trim())
        )
    }

    fun obtenerPorCorreo(correo: String): UsuarioEntity? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USUARIOS,
            null,
            "LOWER($COL_CORREO) = LOWER(?)",
            arrayOf(correo.trim()),
            null,
            null,
            null,
            "1"
        )
        return cursor.use {
            if (it.moveToFirst()) {
                UsuarioEntity(
                    correo = it.getString(it.getColumnIndexOrThrow(COL_CORREO)),
                    dni = it.getString(it.getColumnIndexOrThrow(COL_DNI)),
                    nombreCompleto = it.getString(it.getColumnIndexOrThrow(COL_NOMBRE)),
                    genero = it.getString(it.getColumnIndexOrThrow(COL_GENERO)),
                    telefono = it.getString(it.getColumnIndexOrThrow(COL_TELEFONO)),
                    contrasena = it.getString(it.getColumnIndexOrThrow(COL_CONTRASENA)),
                    distrito = it.getString(it.getColumnIndexOrThrow(COL_DISTRITO)),
                    urbanizacion = it.getString(it.getColumnIndexOrThrow(COL_URBANIZACION)),
                    fechaRegistro = it.getLong(it.getColumnIndexOrThrow(COL_FECHA_REGISTRO))
                )
            } else {
                null
            }
        }
    }

    fun autenticarUsuario(correo: String, contrasena: String): UsuarioEntity? {
        val db = readableDatabase
        val cursor = db.query(
            TABLE_USUARIOS,
            null,
            "LOWER($COL_CORREO) = LOWER(?) AND $COL_CONTRASENA = ?",
            arrayOf(correo.trim(), contrasena),
            null,
            null,
            null,
            "1"
        )
        return cursor.use {
            if (it.moveToFirst()) {
                UsuarioEntity(
                    correo = it.getString(it.getColumnIndexOrThrow(COL_CORREO)),
                    dni = it.getString(it.getColumnIndexOrThrow(COL_DNI)),
                    nombreCompleto = it.getString(it.getColumnIndexOrThrow(COL_NOMBRE)),
                    genero = it.getString(it.getColumnIndexOrThrow(COL_GENERO)),
                    telefono = it.getString(it.getColumnIndexOrThrow(COL_TELEFONO)),
                    contrasena = it.getString(it.getColumnIndexOrThrow(COL_CONTRASENA)),
                    distrito = it.getString(it.getColumnIndexOrThrow(COL_DISTRITO)),
                    urbanizacion = it.getString(it.getColumnIndexOrThrow(COL_URBANIZACION)),
                    fechaRegistro = it.getLong(it.getColumnIndexOrThrow(COL_FECHA_REGISTRO))
                )
            } else {
                null
            }
        }
    }
}
