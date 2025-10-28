package com.example.project

import android.app.Application
import com.example.project.data.local.GlucoseDatabase
import com.example.project.data.remote.api.RetrofitClient
import com.example.project.data.remote.repository.CgmApiRepositoryImpl
import com.example.project.data.repository.GlucoseCsvRepository
import com.example.project.data.repository.GlucoseDatabaseRepository
import com.example.project.domain.repository.CgmApiRepository
import com.example.project.domain.repository.GlucoseRepository

/**
 * Application class for dependency injection
 * Provides singleton instances of repositories
 */
class CGMApplication : Application() {

    // Database instance
    private val database by lazy {
        GlucoseDatabase.getInstance(applicationContext)
    }

    // Glucose Repository (local database)
    val glucoseRepository: GlucoseRepository by lazy {
        GlucoseDatabaseRepository(database.glucoseDao())
    }

    // CSV Repository
    val csvRepository: com.example.project.domain.repository.GlucoseCsvRepository by lazy {
        GlucoseCsvRepository()
    }

    // CGM API Repository (remote API)
    val cgmApiRepository: CgmApiRepository by lazy {
        CgmApiRepositoryImpl(RetrofitClient.apiService, applicationContext)
    }
}
