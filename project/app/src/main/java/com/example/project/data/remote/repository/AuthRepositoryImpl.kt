package com.example.project.data.remote.repository

import com.example.project.data.local.PreferencesRepository
import com.example.project.data.local.TokenManager
import com.example.project.data.remote.api.CgmApiService
import com.example.project.data.remote.dto.*
import com.example.project.data.remote.mapper.AuthMapper
import com.example.project.domain.model.*
import com.example.project.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.IOException

class AuthRepositoryImpl(
    private val apiService: CgmApiService,
    private val tokenManager: TokenManager,
    private val preferencesRepository: PreferencesRepository
) : AuthRepository {

    private var cachedUser: User? = null

    // ============ AUTHENTICATION ============

    override suspend fun registerPatient(
        username: String,
        password: String,
        nickname: String,
        birthYear: Int,
        diabetesDiagnosisMonth: Int,
        diabetesDiagnosisYear: Int
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterPatientRequestDto(
                username = username,
                password = password,
                nickname = nickname,
                birthYear = birthYear,
                diabetesDiagnosisMonth = diabetesDiagnosisMonth,
                diabetesDiagnosisYear = diabetesDiagnosisYear
            )
            val response = apiService.registerPatient(request)

            if (response.isSuccessful && response.body() != null) {
                val user = AuthMapper.mapToUser(response.body()!!.user)
                Result.success(user)
            } else {
                Result.failure(IOException("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun registerClinician(
        username: String,
        password: String,
        fullName: String
    ): Result<User> = withContext(Dispatchers.IO) {
        try {
            val request = RegisterClinicianRequestDto(
                username = username,
                password = password,
                fullName = fullName
            )
            val response = apiService.registerClinician(request)

            if (response.isSuccessful && response.body() != null) {
                val user = AuthMapper.mapToUser(response.body()!!.user)
                Result.success(user)
            } else {
                Result.failure(IOException("Registration failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun login(username: String, password: String): Result<Pair<User, AuthTokens>> =
        withContext(Dispatchers.IO) {
            try {
                val request = LoginRequestDto(username, password)
                val response = apiService.login(request)

                if (response.isSuccessful && response.body() != null) {
                    val loginResponse = response.body()!!
                    val tokens = AuthMapper.mapToAuthTokens(loginResponse)
                    val user = AuthMapper.mapToUser(loginResponse.user)

                    // Save tokens
                    tokenManager.saveTokens(tokens)
                    cachedUser = user

                    Result.success(Pair(user, tokens))
                } else {
                    Result.failure(IOException("Login failed: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            // Call logout endpoint (if token is still valid)
            if (tokenManager.isAuthenticated()) {
                apiService.logout()
            }

            // Clear local data regardless of API response
            clearLocalAuth()

            Result.success(Unit)
        } catch (e: Exception) {
            // Still clear local data even if API call fails
            clearLocalAuth()
            Result.success(Unit)
        }
    }

    override suspend fun refreshToken(): Result<String> = withContext(Dispatchers.IO) {
        try {
            val refreshToken = tokenManager.getRefreshToken()
                ?: return@withContext Result.failure(IOException("No refresh token available"))

            val request = RefreshRequestDto(refreshToken)
            val response = apiService.refreshToken(request)

            if (response.isSuccessful && response.body() != null) {
                val newAccessToken = response.body()!!.accessToken
                tokenManager.updateAccessToken(newAccessToken)
                Result.success(newAccessToken)
            } else {
                // If refresh fails, clear auth
                clearLocalAuth()
                Result.failure(IOException("Token refresh failed: ${response.message()}"))
            }
        } catch (e: Exception) {
            clearLocalAuth()
            Result.failure(e)
        }
    }

    // ============ PROFILE MANAGEMENT ============

    override suspend fun getProfile(): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getProfile()

            if (response.isSuccessful && response.body() != null) {
                val user = AuthMapper.mapToUser(response.body()!!)
                cachedUser = user
                Result.success(user)
            } else {
                Result.failure(IOException("Failed to get profile: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateProfile(nickname: String?, fullName: String?): Result<User> =
        withContext(Dispatchers.IO) {
            try {
                val request = UpdateProfileRequestDto(nickname, fullName)
                val response = apiService.updateProfile(request)

                if (response.isSuccessful && response.body() != null) {
                    val user = AuthMapper.mapToUser(response.body()!!)
                    cachedUser = user
                    Result.success(user)
                } else {
                    Result.failure(IOException("Failed to update profile: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun updatePassword(
        currentPassword: String,
        newPassword: String
    ): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val request = UpdatePasswordRequestDto(currentPassword, newPassword)
            val response = apiService.updatePassword(request)

            if (response.isSuccessful) {
                Result.success(Unit)
            } else {
                Result.failure(IOException("Failed to update password: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteAccount(): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteAccount()

            if (response.isSuccessful) {
                clearLocalAuth()
                Result.success(Unit)
            } else {
                Result.failure(IOException("Failed to delete account: ${response.message()}"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // ============ CONNECTION MANAGEMENT (PATIENT) ============

    override suspend fun generateConnectionCode(): Result<ConnectionCode> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.generateConnectionCode()

                if (response.isSuccessful && response.body() != null) {
                    val code = AuthMapper.mapToConnectionCode(response.body()!!)
                    Result.success(code)
                } else {
                    Result.failure(IOException("Failed to generate code: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getConnectedClinicians(): Result<List<ConnectedClinician>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getConnectedClinicians()

                if (response.isSuccessful && response.body() != null) {
                    val clinicians = AuthMapper.mapToConnectedClinicianList(response.body()!!)
                    Result.success(clinicians)
                } else {
                    Result.failure(IOException("Failed to get clinicians: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun disconnectClinician(clinicianId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.disconnectClinician(clinicianId)

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("Failed to disconnect: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ============ CONNECTION MANAGEMENT (CLINICIAN) ============

    override suspend fun connectToPatient(connectionCode: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val request = ConnectionCodeRequestDto(connectionCode)
                val response = apiService.connectToPatient(request)

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("Failed to connect: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun getConnectedPatients(): Result<List<ConnectedPatient>> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.getConnectedPatients()

                if (response.isSuccessful && response.body() != null) {
                    val patients = AuthMapper.mapToConnectedPatientList(response.body()!!)
                    Result.success(patients)
                } else {
                    Result.failure(IOException("Failed to get patients: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    override suspend fun disconnectPatient(patientId: String): Result<Unit> =
        withContext(Dispatchers.IO) {
            try {
                val response = apiService.disconnectPatient(patientId)

                if (response.isSuccessful) {
                    Result.success(Unit)
                } else {
                    Result.failure(IOException("Failed to disconnect: ${response.message()}"))
                }
            } catch (e: Exception) {
                Result.failure(e)
            }
        }

    // ============ LOCAL TOKEN MANAGEMENT ============

    override fun isAuthenticated(): Boolean {
        return tokenManager.isAuthenticated()
    }

    override fun getCurrentUser(): User? {
        return cachedUser
    }

    override suspend fun clearLocalAuth() {
        tokenManager.clearTokens()
        cachedUser = null
        preferencesRepository.clearActiveDatasetId()
    }
}
