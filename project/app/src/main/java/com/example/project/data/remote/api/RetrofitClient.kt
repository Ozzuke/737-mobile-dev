package com.example.project.data.remote.api

import com.example.project.BuildConfig
import com.example.project.data.local.TokenManager
import com.example.project.data.remote.interceptor.AuthInterceptor
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.util.concurrent.TimeUnit

/**
 * Retrofit client builder for the CGM API with JWT authentication
 */
class RetrofitClient(
    private val tokenManager: TokenManager,
    private val onTokenRefreshNeeded: suspend () -> Boolean
) {
    companion object {
        private const val BASE_URL = "http://cgm.cloud.ut.ee/api/v1/"
    }

    // Moshi instance for JSON parsing
    private val moshi: Moshi = Moshi.Builder()
        .add(KotlinJsonAdapterFactory())
        .build()

    // OkHttp client with interceptors
    private val okHttpClient: OkHttpClient by lazy {
        val builder = OkHttpClient.Builder()
            .addInterceptor(AuthInterceptor(tokenManager, onTokenRefreshNeeded))
            .connectTimeout(30, TimeUnit.SECONDS)
            .readTimeout(30, TimeUnit.SECONDS)
            .writeTimeout(30, TimeUnit.SECONDS)

        // Only add logging in debug builds
        if (BuildConfig.DEBUG) {
            val loggingInterceptor = HttpLoggingInterceptor().apply {
                level = HttpLoggingInterceptor.Level.BODY
            }
            builder.addInterceptor(loggingInterceptor)
        }

        builder.build()
    }

    // Retrofit instance
    private val retrofit: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(MoshiConverterFactory.create(moshi))
            .build()
    }

    // API service instance
    val apiService: CgmApiService by lazy {
        retrofit.create(CgmApiService::class.java)
    }
}
