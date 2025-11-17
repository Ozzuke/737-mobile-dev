package com.example.project.data.remote.mapper

import com.example.project.data.remote.dto.*
import com.example.project.domain.model.*

object AuthMapper {

    fun mapToUser(dto: UserDto): User {
        val safeId = dto.id ?: "" // tolerate missing id from backend
        return when (dto.role.lowercase()) {
            "patient" -> PatientProfile(
                id = safeId,
                username = dto.username,
                role = UserRole.PATIENT,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt,
                nickname = dto.nickname ?: "",
                birthYear = dto.birthYear ?: 0,
                diabetesDiagnosisMonth = dto.diabetesDiagnosisMonth ?: 0,
                diabetesDiagnosisYear = dto.diabetesDiagnosisYear ?: 0
            )
            "clinician" -> ClinicianProfile(
                id = safeId,
                username = dto.username,
                role = UserRole.CLINICIAN,
                createdAt = dto.createdAt,
                updatedAt = dto.updatedAt,
                fullName = dto.fullName ?: ""
            )
            else -> throw IllegalArgumentException("Unknown user role: ${dto.role}")
        }
    }

    fun mapToAuthTokens(dto: LoginResponseDto): AuthTokens {
        return AuthTokens(
            accessToken = dto.accessToken,
            refreshToken = dto.refreshToken,
            tokenType = dto.tokenType
        )
    }

    fun mapToConnectionCode(dto: ConnectionCodeResponseDto): ConnectionCode {
        return ConnectionCode(
            code = dto.connectionCode,
            expiresAt = dto.expiresAt
        )
    }

    fun mapToConnectedPatient(dto: ConnectedPatientDto): ConnectedPatient {
        return ConnectedPatient(
            id = dto.id,
            nickname = dto.nickname
        )
    }

    fun mapToConnectedClinician(dto: ConnectedClinicianDto): ConnectedClinician {
        return ConnectedClinician(
            id = dto.id,
            fullName = dto.fullName
        )
    }

    fun mapToConnectedPatientList(dtos: List<ConnectedPatientDto>): List<ConnectedPatient> {
        return dtos.map { mapToConnectedPatient(it) }
    }

    fun mapToConnectedClinicianList(dtos: List<ConnectedClinicianDto>): List<ConnectedClinician> {
        return dtos.map { mapToConnectedClinician(it) }
    }
}
