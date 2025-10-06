package com.example.project.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.example.project.data.local.dao.GlucoseDao
import com.example.project.domain.model.GlucoseReading

@Database(
    entities = [GlucoseReading::class],
    version = 1,
    exportSchema = false
)
abstract class GlucoseDatabase : RoomDatabase() {
    
    abstract fun glucoseDao(): GlucoseDao
    
    companion object {
        @Volatile
        private var INSTANCE: GlucoseDatabase? = null
        
        fun getInstance(context: Context): GlucoseDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GlucoseDatabase::class.java,
                    "glucose_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
}
