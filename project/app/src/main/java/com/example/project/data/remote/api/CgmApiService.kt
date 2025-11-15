package com.example.project.data.remote.api

import com.example.project.data.remote.dto.*
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Response
import retrofit2.http.*

/**
 * Retrofit API service interface for the CGM Analyzer API
 * Base URL: /api/v1
 */
interface CgmApiService {

    // ============ SYSTEM ============

    /**
     * Health check endpoint
     * GET /healthz
     */
    @GET("healthz")
    suspend fun getHealth(): Response<HealthResponseDto>

    // ============ AUTHENTICATION ============

    /**
     * Register new patient account
     * POST /auth/register/patient
     */
    @POST("auth/register/patient")
    suspend fun registerPatient(
        @Body request: RegisterPatientRequestDto
    ): Response<RegisterResponseDto>

    /**
     * Register new clinician account
     * POST /auth/register/clinician
     */
    @POST("auth/register/clinician")
    suspend fun registerClinician(
        @Body request: RegisterClinicianRequestDto
    ): Response<RegisterResponseDto>

    /**
     * Login and receive JWT tokens
     * POST /auth/login
     */
    @POST("auth/login")
    suspend fun login(
        @Body request: LoginRequestDto
    ): Response<LoginResponseDto>

    /**
     * Logout (invalidate tokens)
     * POST /auth/logout
     */
    @POST("auth/logout")
    suspend fun logout(): Response<MessageResponseDto>

    /**
     * Refresh access token
     * POST /auth/refresh
     */
    @POST("auth/refresh")
    suspend fun refreshToken(
        @Body request: RefreshRequestDto
    ): Response<RefreshResponseDto>

    // ============ USER PROFILE ============

    /**
     * Get current user profile
     * GET /profile
     */
    @GET("profile")
    suspend fun getProfile(): Response<UserDto>

    /**
     * Update user profile
     * PUT /profile
     */
    @PUT("profile")
    suspend fun updateProfile(
        @Body request: UpdateProfileRequestDto
    ): Response<UserDto>

    /**
     * Delete user account
     * DELETE /profile
     */
    @DELETE("profile")
    suspend fun deleteAccount(): Response<Unit>

    /**
     * Change password
     * PUT /profile/password
     */
    @PUT("profile/password")
    suspend fun updatePassword(
        @Body request: UpdatePasswordRequestDto
    ): Response<MessageResponseDto>

    // ============ CONNECTION MANAGEMENT ============

    /**
     * Generate connection code (Patient only)
     * GET /connection-code
     */
    @GET("connection-code")
    suspend fun generateConnectionCode(): Response<ConnectionCodeResponseDto>

    /**
     * Connect to a patient (Clinician only)
     * POST /connections
     */
    @POST("connections")
    suspend fun connectToPatient(
        @Body request: ConnectionCodeRequestDto
    ): Response<MessageResponseDto>

    /**
     * Get connected patients (Clinician only)
     * GET /patients
     */
    @GET("patients")
    suspend fun getConnectedPatients(): Response<List<ConnectedPatientDto>>

    /**
     * Get connected clinicians (Patient only)
     * GET /clinicians
     */
    @GET("clinicians")
    suspend fun getConnectedClinicians(): Response<List<ConnectedClinicianDto>>

    /**
     * Disconnect a patient (Clinician only)
     * DELETE /patients/{patient_id}
     */
    @DELETE("patients/{patient_id}")
    suspend fun disconnectPatient(
        @Path("patient_id") patientId: String
    ): Response<Unit>

    /**
     * Disconnect a clinician (Patient only)
     * DELETE /clinicians/{clinician_id}
     */
    @DELETE("clinicians/{clinician_id}")
    suspend fun disconnectClinician(
        @Path("clinician_id") clinicianId: String
    ): Response<Unit>

    // ============ DATA MANAGEMENT ============

    /**
     * Upload CGM data
     * POST /data
     * @param file The CSV file to upload (cgm_csv)
     * @param unit Optional unit (mmol/L or mg/dL)
     * @param patientId Required when clinician uploads for a patient
     */
    @Multipart
    @POST("data")
    suspend fun uploadData(
        @Part file: MultipartBody.Part,
        @Part("unit") unit: RequestBody? = null,
        @Part("patient_id") patientId: RequestBody? = null
    ): Response<UploadDataResponseDto>

    /**
     * Get dataset metadata
     * GET /data?patient_id={patient_id}
     * @param patientId Required for clinicians to view patient data
     */
    @GET("data")
    suspend fun getDatasetMetadata(
        @Query("patient_id") patientId: String? = null
    ): Response<DatasetItem>

    /**
     * Delete all CGM data
     * DELETE /data
     */
    @DELETE("data")
    suspend fun deleteData(): Response<Unit>

    /**
     * Get data overlay (visualization data)
     * GET /data/overlay?preset={preset}&patient_id={patient_id}
     * @param preset One of: 24h, 7d, 14d
     * @param patientId Required for clinicians
     */
    @GET("data/overlay")
    suspend fun getDataOverlay(
        @Query("preset") preset: String,
        @Query("patient_id") patientId: String? = null
    ): Response<DataOverlayResponse>

    // ============ ANALYSIS ============

    /**
     * Analyze CGM data
     * POST /analyze
     * @param request The analysis request with preset, lang, patient_age, patient_id
     */
    @POST("analyze")
    suspend fun analyze(
        @Body request: AnalyzeRequest
    ): Response<AnalyzeResponse>

    /**
     * Generate natural language explanation (with LLM)
     * POST /analyze/explain
     * @param request The explanation request with preset, lang, style, patient_age, patient_id
     */
    @POST("analyze/explain")
    suspend fun explain(
        @Body request: ExplainRequest
    ): Response<ExplainResponse>
}

// New DTOs for updated endpoints
@com.squareup.moshi.JsonClass(generateAdapter = true)
data class DatasetItem(
    @com.squareup.moshi.Json(name = "id") val id: String,
    @com.squareup.moshi.Json(name = "patient_id") val patientId: String,
    @com.squareup.moshi.Json(name = "unit") val unit: String,
    @com.squareup.moshi.Json(name = "total_readings") val totalReadings: Int,
    @com.squareup.moshi.Json(name = "date_range") val dateRange: DateRangeDto,
    @com.squareup.moshi.Json(name = "created_at") val createdAt: String,
    @com.squareup.moshi.Json(name = "updated_at") val updatedAt: String
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class DateRangeDto(
    @com.squareup.moshi.Json(name = "start") val start: String,
    @com.squareup.moshi.Json(name = "end") val end: String
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class DataOverlayResponse(
    @com.squareup.moshi.Json(name = "unit") val unit: String,
    @com.squareup.moshi.Json(name = "preset") val preset: String,
    @com.squareup.moshi.Json(name = "coverage_percent") val coveragePercent: Double,
    @com.squareup.moshi.Json(name = "overlay") val overlay: OverlayDataDto
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class OverlayDataDto(
    @com.squareup.moshi.Json(name = "days") val days: List<OverlayDayDto>
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class AnalyzeRequest(
    @com.squareup.moshi.Json(name = "preset") val preset: String,
    @com.squareup.moshi.Json(name = "lang") val lang: String = "en",
    @com.squareup.moshi.Json(name = "patient_age") val patientAge: Int? = null,
    @com.squareup.moshi.Json(name = "patient_id") val patientId: String? = null
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class AnalyzeResponse(
    @com.squareup.moshi.Json(name = "unit") val unit: String,
    @com.squareup.moshi.Json(name = "meta") val meta: AnalysisMetaDto,
    @com.squareup.moshi.Json(name = "overall") val overall: OverallDto,
    @com.squareup.moshi.Json(name = "annotations") val annotations: AnnotationsDto,
    @com.squareup.moshi.Json(name = "patterns") val patterns: List<PatternDto>,
    @com.squareup.moshi.Json(name = "text") val text: AnalysisTextDto
)

// Overall rating schema matching authentication-swagger.yaml
@com.squareup.moshi.JsonClass(generateAdapter = true)
data class OverallDto(
    @com.squareup.moshi.Json(name = "rating") val rating: String,
    @com.squareup.moshi.Json(name = "summary") val summary: String
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class AnalysisMetaDto(
    @com.squareup.moshi.Json(name = "coverage_percent") val coveragePercent: Double,
    @com.squareup.moshi.Json(name = "resolution_min") val resolutionMin: Int,
    @com.squareup.moshi.Json(name = "days_count") val daysCount: Int
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ExplainRequest(
    @com.squareup.moshi.Json(name = "preset") val preset: String,
    @com.squareup.moshi.Json(name = "lang") val lang: String = "en",
    @com.squareup.moshi.Json(name = "style") val style: String = "detailed",
    @com.squareup.moshi.Json(name = "patient_age") val patientAge: Int? = null,
    @com.squareup.moshi.Json(name = "patient_id") val patientId: String? = null
)

@com.squareup.moshi.JsonClass(generateAdapter = true)
data class ExplainResponse(
    @com.squareup.moshi.Json(name = "explanation") val explanation: String,
    @com.squareup.moshi.Json(name = "meta") val meta: ExplainMetaDto
)
