package com.example.project.data.repository

import com.example.project.data.local.dao.GlucoseDao
import com.example.project.data.local.mapper.toDomainModel
import com.example.project.data.local.mapper.toEntity
import com.example.project.domain.model.GlucoseReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class GlucoseDatabaseRepository(private val glucoseDao: GlucoseDao) {

    val allReadings: Flow<List<GlucoseReading>> = glucoseDao.getAllReadings().map {
        it.map { entity -> entity.toDomainModel() }
    }

    val latestReading: Flow<GlucoseReading?> = glucoseDao.getLatestReading().map {
        it?.toDomainModel()
    }

    suspend fun insertReadings(readings: List<GlucoseReading>) {
        glucoseDao.insertAll(readings.map { it.toEntity() })
    }

    suspend fun deleteAllReadings() {
        glucoseDao.deleteAll()
    }
}
