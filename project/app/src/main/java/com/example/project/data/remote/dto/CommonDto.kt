package com.example.project.data.remote.dto

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Common DTOs used across the API
 */

@JsonClass(generateAdapter = true)
data class HealthResponseDto(
    @Json(name = "status")
    val status: String
)

@JsonClass(generateAdapter = true)
data class ApiErrorDto(
    @Json(name = "detail")
    val detail: String
)

@JsonClass(generateAdapter = true)
data class MessageResponseDto(
    @Json(name = "message")
    val message: String
)
