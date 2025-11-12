package com.example.project.domain.model

// ============ USER ROLES ============

enum class UserRole {
    PATIENT,
    CLINICIAN,
    UNKNOWN
}

// ============ USER MODELS ============

sealed class User {
    abstract val id: String
    abstract val username: String
    abstract val role: UserRole
    abstract val createdAt: String
    abstract val updatedAt: String
}

data class PatientProfile(
    override val id: String,
    override val username: String,
    override val role: UserRole = UserRole.PATIENT,
    override val createdAt: String,
    override val updatedAt: String,
    val nickname: String,
    val birthYear: Int,
    val diabetesDiagnosisMonth: Int,
    val diabetesDiagnosisYear: Int
) : User()

data class ClinicianProfile(
    override val id: String,
    override val username: String,
    override val role: UserRole = UserRole.CLINICIAN,
    override val createdAt: String,
    override val updatedAt: String,
    val fullName: String
) : User()

// ============ AUTHENTICATION TOKENS ============

data class AuthTokens(
    val accessToken: String,
    val refreshToken: String,
    val tokenType: String = "Bearer"
)

// ============ AUTHENTICATION STATE ============

data class AuthState(
    val isAuthenticated: Boolean,
    val user: User?,
    val tokens: AuthTokens?
)

// ============ CONNECTION MODELS ============

data class ConnectionCode(
    val code: String,
    val expiresAt: String
)

data class ConnectedPatient(
    val id: String,
    val nickname: String
)

data class ConnectedClinician(
    val id: String,
    val fullName: String
)
