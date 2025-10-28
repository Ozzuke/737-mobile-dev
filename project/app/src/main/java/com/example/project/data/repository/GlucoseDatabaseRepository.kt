package com.example.project.data.repository

import com.example.project.data.local.dao.GlucoseDao
import com.example.project.data.local.mapper.toDomainModel
import com.example.project.data.local.mapper.toEntity
import com.example.project.domain.model.GlucoseReading
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

import com.example.project.domain.repository.GlucoseRepository

class GlucoseDatabaseRepository(private val glucoseDao: GlucoseDao) : GlucoseRepository {

    override val allReadings: Flow<List<GlucoseReading>> = glucoseDao.getAllReadings().map {
        it.map { entity -> entity.toDomainModel() }
    }

    override val latestReading: Flow<GlucoseReading?> = glucoseDao.getLatestReading().map {
        it?.toDomainModel()
    }

    override suspend fun insertReadings(readings: List<GlucoseReading>) {
        glucoseDao.insertAll(readings.map { it.toEntity() })
    }

    override suspend fun deleteAllReadings() {
        glucoseDao.deleteAll()
    }
}
