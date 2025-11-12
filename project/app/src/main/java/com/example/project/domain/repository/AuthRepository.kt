package com.example.project.domain.repository

import com.example.project.domain.model.*

/**
 * Repository interface for authentication operations
 */
interface AuthRepository {

    // Authentication
    suspend fun registerPatient(
        username: String,
        password: String,
        nickname: String,
        birthYear: Int,
        diabetesDiagnosisMonth: Int,
        diabetesDiagnosisYear: Int
    ): Result<User>

    suspend fun registerClinician(
        username: String,
        password: String,
        fullName: String
    ): Result<User>

    suspend fun login(username: String, password: String): Result<Pair<User, AuthTokens>>

    suspend fun logout(): Result<Unit>

    suspend fun refreshToken(): Result<String>

    // Profile Management
    suspend fun getProfile(): Result<User>

    suspend fun updateProfile(nickname: String?, fullName: String?): Result<User>

    suspend fun updatePassword(currentPassword: String, newPassword: String): Result<Unit>

    suspend fun deleteAccount(): Result<Unit>

    // Connection Management (Patient)
    suspend fun generateConnectionCode(): Result<ConnectionCode>

    suspend fun getConnectedClinicians(): Result<List<ConnectedClinician>>

    suspend fun disconnectClinician(clinicianId: String): Result<Unit>

    // Connection Management (Clinician)
    suspend fun connectToPatient(connectionCode: String): Result<Unit>

    suspend fun getConnectedPatients(): Result<List<ConnectedPatient>>

    suspend fun disconnectPatient(patientId: String): Result<Unit>

    // Local Token Management
    fun isAuthenticated(): Boolean

    fun getCurrentUser(): User?

    suspend fun clearLocalAuth()
}
