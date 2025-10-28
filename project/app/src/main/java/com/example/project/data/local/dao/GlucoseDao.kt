package com.example.project.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.project.data.local.entity.GlucoseReadingEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucoseDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<GlucoseReadingEntity>)
    
    @Query("SELECT * FROM glucose_readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<GlucoseReadingEntity>>
    
    @Query("SELECT * FROM glucose_readings ORDER BY timestamp DESC LIMIT 1")
    fun getLatestReading(): Flow<GlucoseReadingEntity?>
    
    @Query("DELETE FROM glucose_readings")
    suspend fun deleteAll()
}
