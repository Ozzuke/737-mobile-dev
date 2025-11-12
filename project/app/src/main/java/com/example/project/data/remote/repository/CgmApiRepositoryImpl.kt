package com.example.project.data.remote.repository

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import com.example.project.data.remote.api.AnalyzeRequest
import com.example.project.data.remote.api.CgmApiService
import com.example.project.data.remote.api.ExplainRequest
import com.example.project.domain.model.AnalysisResult
import com.example.project.domain.model.DatasetData
import com.example.project.domain.model.DatasetSummary
import com.example.project.domain.repository.CgmApiRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import retrofit2.Response
import java.io.File
import java.io.FileOutputStream

/**
 * Implementation of CgmApiRepository for new API spec
 * Handles API calls and converts responses to domain models
 */
class CgmApiRepositoryImpl(
    private val apiService: CgmApiService,
    private val context: Context
) : CgmApiRepository {

    override suspend fun checkHealth(): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getHealth()
            if (response.isSuccessful && response.body()?.status == "healthy") {
                Result.success(true)
            } else {
                Result.failure(Exception("Health check failed"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get dataset metadata (single dataset per user in new API)
     * @param patientId Optional patient ID for clinicians viewing patient data
     */
    override suspend fun getDatasets(patientId: String?): Result<List<DatasetSummary>> = withContext(Dispatchers.IO) {
        try {
            // Note: New API returns single dataset via /data endpoint
            // Returning as list for compatibility with existing UI
            val response = apiService.getDatasetMetadata(patientId = patientId)
            if (response.isSuccessful && response.body() != null) {
                val dataset = response.body()!!
                val summary = DatasetSummary(
                    datasetId = dataset.id,
                    nickname = "", // No nickname in new API
                    createdAt = dataset.createdAt,
                    rowCount = dataset.totalReadings,
                    startDate = dataset.dateRange.start,
                    endDate = dataset.dateRange.end,
                    unit = dataset.unit,
                    samplingIntervalMin = 0 // Not provided in new API
                )
                Result.success(listOf(summary))
            } else if (response.code() == 404) {
                // No dataset found
                Result.success(emptyList())
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get dataset data overlay for visualization
     * @param datasetId Not used in new API (user-specific)
     * @param preset Time period (24h, 7d, 14d)
     * @param patientId Optional patient ID for clinicians
     */
    override suspend fun getDatasetData(
        datasetId: String,
        preset: String,
        patientId: String?
    ): Result<DatasetData> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.getDataOverlay(
                preset = preset,
                patientId = patientId
            )
            if (response.isSuccessful && response.body() != null) {
                val overlay = response.body()!!
                // Convert to DatasetData format
                val datasetData = DatasetData(
                    datasetId = datasetId,
                    nickname = "",
                    unit = overlay.unit,
                    requestedPreset = overlay.preset,
                    availableDays = overlay.overlay.days.size,
                    coveragePercent = overlay.coveragePercent,
                    resolutionMin = 15, // Assume 15-minute resolution
                    warnings = emptyList(),
                    overlayDays = overlay.overlay.days.map { day ->
                        com.example.project.domain.model.OverlayDay(
                            date = day.date,
                            points = day.points.map { point ->
                                com.example.project.domain.model.GlucosePoint(
                                    minute = point.minute,
                                    glucose = point.glucose
                                )
                            }
                        )
                    }
                )
                Result.success(datasetData)
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Analyze dataset
     * @param datasetId Not used in new API
     * @param preset Time period
     * @param lang Language code
     * @param patientId Optional patient ID for clinicians
     */
    override suspend fun analyzeDataset(
        datasetId: String,
        preset: String,
        lang: String,
        patientId: String?
    ): Result<AnalysisResult> = withContext(Dispatchers.IO) {
        try {
            val request = AnalyzeRequest(
                preset = preset,
                lang = lang,
                patientAge = null,
                patientId = patientId
            )
            val response = apiService.analyze(request)
            if (response.isSuccessful && response.body() != null) {
                val analysisDto = response.body()!!
                // Convert to domain model
                val analysis = AnalysisResult(
                    unit = analysisDto.unit,
                    requestedPreset = preset,
                    availableDays = analysisDto.meta.daysCount,
                    coveragePercent = analysisDto.meta.coveragePercent,
                    overallRating = com.example.project.domain.model.OverallRating(
                        category = when (analysisDto.overall.category.uppercase()) {
                            "GOOD" -> com.example.project.domain.model.RatingCategory.GOOD
                            "ATTENTION" -> com.example.project.domain.model.RatingCategory.ATTENTION
                            "URGENT" -> com.example.project.domain.model.RatingCategory.URGENT
                            else -> com.example.project.domain.model.RatingCategory.UNKNOWN
                        },
                        score = analysisDto.overall.score,
                        reasons = analysisDto.overall.reasons
                    ),
                    trends = analysisDto.annotations.trends.map { trend ->
                        com.example.project.domain.model.TrendAnnotation(
                            startMinute = trend.startMinute,
                            endMinute = trend.endMinute,
                            slopeMmolLPerHour = trend.slopeMmolLPerHour,
                            direction = when (trend.direction.uppercase()) {
                                "UP" -> com.example.project.domain.model.TrendDirection.UP
                                "DOWN" -> com.example.project.domain.model.TrendDirection.DOWN
                                else -> com.example.project.domain.model.TrendDirection.UNKNOWN
                            },
                            exampleSpan = trend.exampleSpan
                        )
                    },
                    extrema = analysisDto.annotations.extrema.map { extrema ->
                        com.example.project.domain.model.ExtremaAnnotation(
                            minute = extrema.minute,
                            value = extrema.value,
                            kind = when (extrema.kind.uppercase()) {
                                "MAX" -> com.example.project.domain.model.ExtremaKind.MAX
                                "MIN" -> com.example.project.domain.model.ExtremaKind.MIN
                                else -> com.example.project.domain.model.ExtremaKind.UNKNOWN
                            }
                        )
                    },
                    patterns = analysisDto.patterns.map { pattern ->
                        com.example.project.domain.model.Pattern(
                            key = pattern.key,
                            name = pattern.name,
                            severity = when (pattern.severity.uppercase()) {
                                "INFO" -> com.example.project.domain.model.PatternSeverity.INFO
                                "MODERATE" -> com.example.project.domain.model.PatternSeverity.MODERATE
                                "HIGH" -> com.example.project.domain.model.PatternSeverity.HIGH
                                else -> com.example.project.domain.model.PatternSeverity.UNKNOWN
                            },
                            summary = pattern.summary,
                            instances = pattern.instances.map { instance ->
                                com.example.project.domain.model.PatternInstance(
                                    date = instance.date,
                                    startMinute = instance.startMinute,
                                    endMinute = instance.endMinute
                                )
                            }
                        )
                    },
                    summary = analysisDto.text.summary,
                    interpretation = analysisDto.text.interpretation,
                    warnings = emptyList()
                )
                Result.success(analysis)
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Get LLM explanation
     * @param datasetId Not used in new API
     * @param preset Time period
     * @param lang Language code
     * @param style Explanation style
     * @param patientId Optional patient ID for clinicians
     */
    override suspend fun explainDataset(
        datasetId: String,
        preset: String,
        lang: String,
        style: String,
        patientId: String?
    ): Result<com.example.project.domain.model.LLMExplanation> = withContext(Dispatchers.IO) {
        try {
            val request = ExplainRequest(
                preset = preset,
                lang = lang,
                style = style,
                patientAge = null,
                patientId = patientId
            )
            val response = apiService.explain(request)
            if (response.isSuccessful && response.body() != null) {
                val explainDto = response.body()!!
                val explanation = com.example.project.domain.model.LLMExplanation(
                    summary = explainDto.explanation,
                    interpretation = explainDto.explanation,
                    recommendations = emptyList(), // Not separate in new API
                    coveragePercent = explainDto.meta.coveragePercent,
                    preset = explainDto.meta.preset
                )
                Result.success(explanation)
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Upload dataset
     * @param fileUri URI of CSV file
     * @param nickname Not used in new API
     * @param unit Optional unit specification
     * @param patientId Optional patient ID for clinicians uploading for patients
     */
    override suspend fun uploadDataset(
        fileUri: Uri,
        nickname: String?,
        unit: String?,
        patientId: String?
    ): Result<DatasetSummary> = withContext(Dispatchers.IO) {
        try {
            val tempFile = createTempFileFromUri(fileUri)
                ?: return@withContext Result.failure(Exception("Failed to create temporary file from URI"))

            // Create the file part with proper content type
            val requestFile = tempFile.asRequestBody("text/csv".toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("cgm_csv", tempFile.name, requestFile)

            // Optional unit part
            val unitPart = unit?.let {
                it.toRequestBody("text/plain".toMediaTypeOrNull())
            }

            // Patient ID part (for clinicians)
            val patientIdPart = patientId?.let {
                it.toRequestBody("text/plain".toMediaTypeOrNull())
            }

            val response = apiService.uploadData(filePart, unitPart, patientIdPart)

            tempFile.delete()

            if (response.isSuccessful && response.body() != null) {
                val uploadResponse = response.body()!!
                // Create a DatasetSummary from upload response
                val summary = DatasetSummary(
                    datasetId = uploadResponse.datasetId,
                    nickname = "",
                    createdAt = "", // Not in upload response
                    rowCount = uploadResponse.validation.rowCount,
                    startDate = uploadResponse.validation.timeRange.start,
                    endDate = uploadResponse.validation.timeRange.end,
                    unit = uploadResponse.validation.detectedUnit,
                    samplingIntervalMin = 0
                )
                Result.success(summary)
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createTempFileFromUri(uri: Uri): File? {
        return try {
            val stream = context.contentResolver.openInputStream(uri)
            val fileName = getFileName(uri)
            val tempFile = File(context.cacheDir, fileName)
            tempFile.createNewFile()
            stream?.use { input ->
                FileOutputStream(tempFile).use { output ->
                    input.copyTo(output)
                }
            }
            tempFile
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun getFileName(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            context.contentResolver.query(uri, null, null, null, null)?.use { cursor ->
                if (cursor.moveToFirst()) {
                    val colIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (colIndex >= 0) {
                        result = cursor.getString(colIndex)
                    }
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1 && cut != null) {
                result = result?.substring(cut + 1)
            }
        }
        return result ?: "upload.csv"
    }

    /**
     * Delete dataset
     * In new API, this deletes all CGM data for the user
     */
    override suspend fun deleteDataset(datasetId: String): Result<Boolean> = withContext(Dispatchers.IO) {
        try {
            val response = apiService.deleteData()
            if (response.isSuccessful || response.code() == 204) {
                Result.success(true)
            } else {
                Result.failure(Exception(getErrorMessage(response)))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    /**
     * Extract error message from HTTP response
     */
    private fun <T> getErrorMessage(response: Response<T>): String {
        return when (response.code()) {
            401 -> "Authentication failed. Please login again."
            403 -> "Access denied. You don't have permission for this action."
            404 -> "Resource not found."
            422 -> {
                val errorBody = response.errorBody()?.string()
                "Validation error: ${errorBody ?: response.message()}"
            }
            500 -> "Server error. Please try again later."
            503 -> "Service unavailable. Please try again later."
            else -> "Error ${response.code()}: ${response.message()}"
        }
    }
}
