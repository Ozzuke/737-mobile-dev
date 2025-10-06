package com.example.project.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.project.domain.model.GlucoseReading
import kotlinx.coroutines.flow.Flow

@Dao
interface GlucoseDao {
    
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAll(readings: List<GlucoseReading>)
    
    @Query("SELECT * FROM glucose_readings ORDER BY timestamp DESC")
    fun getAllReadings(): Flow<List<GlucoseReading>>
    
    @Query("SELECT * FROM glucose_readings ORDER BY timestamp DESC LIMIT 1")
    fun getLatestReading(): Flow<GlucoseReading?>
    
    @Query("DELETE FROM glucose_readings")
    suspend fun deleteAll()
}
