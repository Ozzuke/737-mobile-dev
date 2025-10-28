package com.example.project.data.remote.interceptor

import okhttp3.Interceptor
import okhttp3.Response

/**
 * Interceptor that adds the API key to all requests
 * The API key is added as a header: X-API-Key
 */
class ApiKeyInterceptor(private val apiKey: String) : Interceptor {
    override fun intercept(chain: Interceptor.Chain): Response {
        val originalRequest = chain.request()
        val requestWithApiKey = originalRequest.newBuilder()
            .addHeader("X-API-Key", apiKey)
            .build()
        return chain.proceed(requestWithApiKey)
    }
}
