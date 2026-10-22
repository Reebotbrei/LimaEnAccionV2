package com.limaenaccion.core.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.limaenaccion.core.data.local.dao.ContactoDao
import com.limaenaccion.core.data.local.dao.ReporteDao
import com.limaenaccion.core.data.local.entities.ContactoEntity
import com.limaenaccion.core.data.local.entities.ReporteEntity

@Database(
    entities = [ReporteEntity::class, ContactoEntity::class],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun reporteDao(): ReporteDao
    abstract fun contactoDao(): ContactoDao

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
