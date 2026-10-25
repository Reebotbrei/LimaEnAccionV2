package com.limaenaccion.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.limaenaccion.core.data.local.dao.ContactoDao
import com.limaenaccion.core.data.local.dao.ReporteDao
import com.limaenaccion.core.data.local.dao.UsuarioDao
import com.limaenaccion.core.data.local.entities.ContactoEntity
import com.limaenaccion.core.data.local.entities.ReporteEntity
import com.limaenaccion.core.data.local.entities.UsuarioEntity

@Database(
    entities = [
        ReporteEntity::class,
        ContactoEntity::class,
        UsuarioEntity::class
    ],
    version = 3,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reporteDao(): ReporteDao
    abstract fun contactoDao(): ContactoDao
    abstract fun usuarioDao(): UsuarioDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "lima_en_accion_database"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
