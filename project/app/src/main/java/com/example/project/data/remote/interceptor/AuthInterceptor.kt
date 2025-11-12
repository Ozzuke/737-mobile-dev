package com.example.project.data.remote.interceptor

import com.example.project.data.local.TokenManager
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response
import java.net.HttpURLConnection

/**
 * Interceptor that adds JWT Bearer token to all authenticated requests
 * and handles token refresh on 401 responses
 */
class AuthInterceptor(
    private val tokenManager: TokenManager,
    private val onTokenRefreshNeeded: suspend () -> Boolean
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val request = chain.request()

        // Skip authentication for public endpoints
        if (isPublicEndpoint(request.url.encodedPath)) {
            return chain.proceed(request)
        }

        // Add Bearer token to request
        val accessToken = tokenManager.getAccessToken()
        val authenticatedRequest = if (accessToken != null) {
            request.newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            request
        }

        // Proceed with the request
        var response = chain.proceed(authenticatedRequest)

        // Handle 401 Unauthorized - try to refresh token
        if (response.code == HttpURLConnection.HTTP_UNAUTHORIZED && accessToken != null) {
            response.close()

            // Try to refresh the token
            val refreshSuccessful = runBlocking {
                onTokenRefreshNeeded()
            }

            // If refresh successful, retry the original request with new token
            if (refreshSuccessful) {
                val newAccessToken = tokenManager.getAccessToken()
                val newRequest = request.newBuilder()
                    .addHeader("Authorization", "Bearer $newAccessToken")
                    .build()
                response = chain.proceed(newRequest)
            }
        }

        return response
    }

    /**
     * Check if the endpoint is public (doesn't require authentication)
     */
    private fun isPublicEndpoint(path: String): Boolean {
        val publicEndpoints = listOf(
            "/api/v1/healthz",
            "/api/v1/auth/register/patient",
            "/api/v1/auth/register/clinician",
            "/api/v1/auth/login",
            "/api/v1/auth/refresh"
        )
        return publicEndpoints.any { path.contains(it) }
    }
}
