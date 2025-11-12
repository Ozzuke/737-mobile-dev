package com.example.project.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

// ============ AUTHENTICATION REQUEST DTOs ============

@JsonClass(generateAdapter = true)
data class RegisterPatientRequestDto(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String,
    @Json(name = "nickname") val nickname: String,
    @Json(name = "birth_year") val birthYear: Int,
    @Json(name = "diabetes_diagnosis_month") val diabetesDiagnosisMonth: Int,
    @Json(name = "diabetes_diagnosis_year") val diabetesDiagnosisYear: Int
)

@JsonClass(generateAdapter = true)
data class RegisterClinicianRequestDto(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String,
    @Json(name = "full_name") val fullName: String
)

@JsonClass(generateAdapter = true)
data class LoginRequestDto(
    @Json(name = "username") val username: String,
    @Json(name = "password") val password: String
)

@JsonClass(generateAdapter = true)
data class RefreshRequestDto(
    @Json(name = "refresh_token") val refreshToken: String
)

@JsonClass(generateAdapter = true)
data class UpdateProfileRequestDto(
    @Json(name = "nickname") val nickname: String? = null,
    @Json(name = "full_name") val fullName: String? = null
)

@JsonClass(generateAdapter = true)
data class UpdatePasswordRequestDto(
    @Json(name = "current_password") val currentPassword: String,
    @Json(name = "new_password") val newPassword: String
)

// ============ AUTHENTICATION RESPONSE DTOs ============

@JsonClass(generateAdapter = true)
data class LoginResponseDto(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "refresh_token") val refreshToken: String,
    @Json(name = "token_type") val tokenType: String,
    @Json(name = "user") val user: UserDto
)

@JsonClass(generateAdapter = true)
data class RefreshResponseDto(
    @Json(name = "access_token") val accessToken: String,
    @Json(name = "token_type") val tokenType: String
)

@JsonClass(generateAdapter = true)
data class RegisterResponseDto(
    @Json(name = "user") val user: UserDto,
    @Json(name = "message") val message: String
)

// ============ USER PROFILE DTOs ============

@JsonClass(generateAdapter = true)
data class UserDto(
    @Json(name = "id") val id: String,
    @Json(name = "username") val username: String,
    @Json(name = "role") val role: String,
    @Json(name = "created_at") val createdAt: String,
    @Json(name = "updated_at") val updatedAt: String,
    @Json(name = "nickname") val nickname: String? = null,
    @Json(name = "birth_year") val birthYear: Int? = null,
    @Json(name = "diabetes_diagnosis_month") val diabetesDiagnosisMonth: Int? = null,
    @Json(name = "diabetes_diagnosis_year") val diabetesDiagnosisYear: Int? = null,
    @Json(name = "full_name") val fullName: String? = null
)

// ============ CONNECTION DTOs ============

@JsonClass(generateAdapter = true)
data class ConnectionCodeRequestDto(
    @Json(name = "connection_code") val connectionCode: String
)

@JsonClass(generateAdapter = true)
data class ConnectionCodeResponseDto(
    @Json(name = "connection_code") val connectionCode: String,
    @Json(name = "expires_at") val expiresAt: String
)

@JsonClass(generateAdapter = true)
data class ConnectedPatientDto(
    @Json(name = "id") val id: String,
    @Json(name = "nickname") val nickname: String
)

@JsonClass(generateAdapter = true)
data class ConnectedClinicianDto(
    @Json(name = "id") val id: String,
    @Json(name = "full_name") val fullName: String
)

@JsonClass(generateAdapter = true)
data class MessageResponseDto(
    @Json(name = "message") val message: String
)
