package com.example.project.data.repository

import com.example.project.data.local.dao.GlucoseDao
import com.example.project.domain.model.GlucoseReading
import kotlinx.coroutines.flow.Flow

class GlucoseDatabaseRepository(private val glucoseDao: GlucoseDao) {
    
    val allReadings: Flow<List<GlucoseReading>> = glucoseDao.getAllReadings()
    
    val latestReading: Flow<GlucoseReading?> = glucoseDao.getLatestReading()
    
    suspend fun insertReadings(readings: List<GlucoseReading>) {
        glucoseDao.insertAll(readings)
    }
    
    suspend fun deleteAllReadings() {
        glucoseDao.deleteAll()
    }
}
