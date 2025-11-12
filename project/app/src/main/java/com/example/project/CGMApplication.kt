package com.example.project

import android.app.Application
import com.example.project.data.local.GlucoseDatabase
import com.example.project.data.local.PreferencesRepository
import com.example.project.data.local.TokenManager
import com.example.project.data.remote.api.RetrofitClient
import com.example.project.data.remote.repository.AuthRepositoryImpl
import com.example.project.data.remote.repository.CgmApiRepositoryImpl
import com.example.project.data.repository.GlucoseCsvRepository
import com.example.project.data.repository.GlucoseDatabaseRepository
import com.example.project.domain.repository.AuthRepository
import com.example.project.domain.repository.CgmApiRepository
import com.example.project.domain.repository.GlucoseRepository
import kotlinx.coroutines.runBlocking

/**
 * Application class for dependency injection
 * Provides singleton instances of repositories with JWT authentication support
 */
class CGMApplication : Application() {

    // Token Manager for secure JWT storage
    val tokenManager: TokenManager by lazy {
        TokenManager(applicationContext)
    }

    // Preferences Repository (DataStore)
    val preferencesRepository: PreferencesRepository by lazy {
        PreferencesRepository(applicationContext)
    }

    // Retrofit Client with authentication
    private val retrofitClient: RetrofitClient by lazy {
        RetrofitClient(
            tokenManager = tokenManager,
            onTokenRefreshNeeded = {
                // Attempt to refresh the token
                runBlocking {
                    authRepository.refreshToken().isSuccess
                }
            }
        )
    }

    // Auth Repository
    val authRepository: AuthRepository by lazy {
        AuthRepositoryImpl(
            apiService = retrofitClient.apiService,
            tokenManager = tokenManager,
            preferencesRepository = preferencesRepository
        )
    }

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
        CgmApiRepositoryImpl(retrofitClient.apiService, applicationContext)
    }
}
